package com.studyhub.userservice.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Course must not exceed 100 characters")
    private String course;

    private Integer year;

    @Size(max = 500, message = "Modules must not exceed 500 characters")
    private String modules;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getModules() { return modules; }
    public void setModules(String modules) { this.modules = modules; }
}