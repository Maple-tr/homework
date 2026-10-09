package com.example.entity;

import com.example.annotation.Column;
import com.example.annotation.Table;
import java.util.Date;

@Table("student")
public class Student {
    @Column(value = "id", id = true, autoIncrement = true)
    private Integer id;
    @Column("name")
    private String name;
    @Column("major")
    private String major;
    @Column("age")
    private Integer age;
    @Column("enrollment_date")
    private Date enrollmentDate;
    @Column("graduated")
    private Boolean graduated;
    @Column("tuition")
    private Double tuition;

    public Student() {}

    public Student(Integer id, String name, String major, Integer age, Date enrollmentDate, Boolean graduated, Double tuition) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.age = age;
        this.enrollmentDate = enrollmentDate;
        this.graduated = graduated;
        this.tuition = tuition;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public Boolean getGraduated() { return graduated; }
    public void setGraduated(Boolean graduated) { this.graduated = graduated; }
    public Double getTuition() { return tuition; }
    public void setTuition(Double tuition) { this.tuition = tuition; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + '\'' + ", major='" + major + '\'' +
                ", age=" + age + ", enrollmentDate=" + enrollmentDate +
                ", graduated=" + graduated + ", tuition=" + tuition + '}';
    }
}