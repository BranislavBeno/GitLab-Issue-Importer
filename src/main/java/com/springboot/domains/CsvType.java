package com.springboot.domains;

public enum CsvType {

    CLEAR_QUEST(ClearQuest.class), USER(User.class);

    private final Class<? extends CsvRow> clazz;

    CsvType(Class<? extends CsvRow> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends CsvRow> getClazz() {
        return clazz;
    }
}
