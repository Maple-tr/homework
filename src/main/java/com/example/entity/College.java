package com.example.entity;

import com.example.annotation.Column;
import com.example.annotation.Table;

@Table("college")
public class College {
    @Column(value = "id", id = true, autoIncrement = true)
    private Integer id;
    @Column("name")
    private String name;
    @Column("code")
    private String code;

    public College() {}

    public College(Integer id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @Override
    public String toString() {
        return "College{id=" + id + ", name='" + name + '\'' + ", code='" + code + '\'' + '}';
    }
}