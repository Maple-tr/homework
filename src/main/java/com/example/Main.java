package com.example;

import com.example.entity.College;
import com.example.entity.Student;
import com.example.util.DBUtil;
import com.example.util.JDBCTool;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            System.out.println("数据库连接成功！");

            // ========== 学院表操作 ==========
            System.out.println("\n========== 学院表操作 ==========");
            College college = new College();
            college.setName("计算机学院");
            college.setCode("CS001");
            int saveCollege = JDBCTool.save(college, conn);
            System.out.println("插入学院影响行数：" + saveCollege + "，自增ID：" + college.getId());

            College queryCollege = JDBCTool.getOneById(String.valueOf(college.getId()), College.class, conn);
            System.out.println("查询学院：" + queryCollege);

            queryCollege.setName("软件学院");
            int updateCollege = JDBCTool.update(queryCollege, conn);
            System.out.println("更新学院影响行数：" + updateCollege);

            List<College> collegeList = JDBCTool.resultSetToList(conn.createStatement().executeQuery("SELECT * FROM college"), College.class);
            System.out.println("所有学院：");
            collegeList.forEach(System.out::println);

            // ========== 学生表操作 ==========
            System.out.println("\n========== 学生表操作 ==========");
            Student student = new Student();
            student.setName("张三");
            student.setMajor("计算机科学与技术");
            student.setAge(20);
            student.setEnrollmentDate(new Date());
            student.setGraduated(false);
            student.setTuition(5000.0);
            int saveStudent = JDBCTool.save(student, conn);
            System.out.println("插入学生影响行数：" + saveStudent + "，自增ID：" + student.getId());

            Student queryStudent = JDBCTool.getOneById(String.valueOf(student.getId()), Student.class, conn);
            System.out.println("查询学生：" + queryStudent);

            queryStudent.setAge(21);
            queryStudent.setGraduated(true);
            int updateStudent = JDBCTool.update(queryStudent, conn);
            System.out.println("更新学生影响行数：" + updateStudent);

            List<Student> studentList = JDBCTool.resultSetToList(conn.createStatement().executeQuery("SELECT * FROM student"), Student.class);
            System.out.println("所有学生：");
            studentList.forEach(System.out::println);

            int deleteStudent = JDBCTool.delete(queryStudent, conn);
            System.out.println("删除学生影响行数：" + deleteStudent);

            int deleteCollege = JDBCTool.delete(queryCollege, conn);
            System.out.println("删除学院影响行数：" + deleteCollege);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn);
        }
    }
}