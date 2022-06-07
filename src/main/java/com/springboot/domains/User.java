package com.springboot.domains;

import com.opencsv.bean.CsvBindByName;

public class User implements CsvRow {

    @CsvBindByName
    private long id;
    @CsvBindByName
    private String name;
    @CsvBindByName
    private String email;
    @CsvBindByName(column = "country")
    private String countryCode;
    @CsvBindByName
    private int age;

    @CsvBindByName
    private String job;

    @Override
    public String provideTitle() {
        return "%s (ID: %s)".formatted(name, id);
    }

    @Override
    public String provideDescription() {
        return """
                User {
                    "id=%s",
                    "name=%s",
                    "email=%s",
                    "countryCode=%s",
                    "age=%s",
                    "job=%s",
                }"""
                .formatted(id, name, email, countryCode, age, job);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}

