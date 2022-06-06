package com.springboot.domains;

public enum CsvType {

    CLEAR_QUEST(ClearQuest.class), USER(User.class);

    private final Class<?> clazz;

    CsvType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
