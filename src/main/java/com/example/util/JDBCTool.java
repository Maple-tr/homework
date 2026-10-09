package com.example.util;

import com.example.annotation.Column;
import com.example.annotation.Table;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCTool {

    // 方法1：ResultSet 转 List
    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);
                Field field = getFieldByColumnName(clazz, columnLabel);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(obj, convertValue(value, field.getType()));
                }
            }
            list.add(obj);
        }
        return list;
    }

    private static Field getFieldByColumnName(Class<?> clazz, String columnName) {
        for (Field field : getAllFields(clazz)) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.value().equalsIgnoreCase(columnName)) {
                return field;
            }
            if (column == null) {
                String fieldName = field.getName();
                String camel = columnName.toLowerCase().replace("_", "");
                if (fieldName.equalsIgnoreCase(camel)) {
                    return field;
                }
            }
        }
        return null;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType == String.class) return value.toString();
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString());
        }
        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            return Double.parseDouble(value.toString());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) return value;
            if (value instanceof Number) return ((Number) value).intValue() != 0;
            return Boolean.parseBoolean(value.toString());
        }
        if (targetType == java.util.Date.class) {
            if (value instanceof java.sql.Date) {
                return new java.util.Date(((java.sql.Date) value).getTime());
            }
            if (value instanceof Timestamp) {
                return new java.util.Date(((Timestamp) value).getTime());
            }
        }
        return value;
    }

    private static String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        return camelToUnderline(clazz.getSimpleName());
    }

    private static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            return column.value();
        }
        return camelToUnderline(field.getName());
    }

    private static String camelToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Field getIdField(Class<?> clazz) {
        for (Field field : getAllFields(clazz)) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.id()) {
                return field;
            }
        }
        return null;
    }

    // 方法2：保存
    public static <T> int save(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        List<Field> fields = new ArrayList<>();
        for (Field field : getAllFields(clazz)) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.autoIncrement()) {
                continue;
            }
            field.setAccessible(true);
            fields.add(field);
        }
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            sql.append(getColumnName(field));
            placeholders.append("?");
            if (i < fields.size() - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }
        sql.append(") VALUES (").append(placeholders).append(")");
        PreparedStatement ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < fields.size(); i++) {
            ps.setObject(i + 1, fields.get(i).get(obj));
        }
        int rows = ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            Field idField = getIdField(clazz);
            if (idField != null) {
                idField.setAccessible(true);
                idField.set(obj, rs.getInt(1));
            }
        }
        rs.close();
        ps.close();
        return rows;
    }

    // 方法3：更新
    public static <T> int update(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        Field idField = getIdField(clazz);
        if (idField == null) throw new RuntimeException("未找到主键字段");
        idField.setAccessible(true);
        Object idValue = idField.get(obj);
        if (idValue == null) throw new RuntimeException("主键值为空");

        List<Field> fields = new ArrayList<>();
        for (Field field : getAllFields(clazz)) {
            if (field.equals(idField)) continue;
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.autoIncrement()) continue;
            field.setAccessible(true);
            fields.add(field);
        }
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        for (int i = 0; i < fields.size(); i++) {
            sql.append(getColumnName(fields.get(i))).append(" = ?");
            if (i < fields.size() - 1) sql.append(", ");
        }
        sql.append(" WHERE ").append(getColumnName(idField)).append(" = ?");
        PreparedStatement ps = connection.prepareStatement(sql.toString());
        int index = 1;
        for (Field field : fields) {
            ps.setObject(index++, field.get(obj));
        }
        ps.setObject(index, idValue);
        int rows = ps.executeUpdate();
        ps.close();
        return rows;
    }

    // 方法4：删除
    public static <T> int delete(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        Field idField = getIdField(clazz);
        if (idField == null) throw new RuntimeException("未找到主键字段");
        idField.setAccessible(true);
        Object idValue = idField.get(obj);
        if (idValue == null) throw new RuntimeException("主键值为空");
        String sql = "DELETE FROM " + tableName + " WHERE " + getColumnName(idField) + " = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1, idValue);
        int rows = ps.executeUpdate();
        ps.close();
        return rows;
    }

    // 方法5：根据 ID 查询单个对象
    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) throws Exception {
        String tableName = getTableName(clazz);
        Field idField = getIdField(clazz);
        if (idField == null) throw new RuntimeException("未找到主键字段");
        String sql = "SELECT * FROM " + tableName + " WHERE " + getColumnName(idField) + " = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        Object idValue = id;
        if (idField.getType() == Integer.class || idField.getType() == int.class) {
            idValue = Integer.parseInt(id);
        }
        ps.setObject(1, idValue);
        ResultSet rs = ps.executeQuery();
        List<T> list = resultSetToList(rs, clazz);
        rs.close();
        ps.close();
        return list.isEmpty() ? null : list.get(0);
    }
}