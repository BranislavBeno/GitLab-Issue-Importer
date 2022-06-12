package com.issue.importer.domain;

public enum CsvType {

    CLEAR_QUEST("ClearQuest", ClearQuest.class), USER("User", User.class);

    private final String label;
    private final Class<? extends CsvRow> clazz;

    CsvType(String label, Class<? extends CsvRow> clazz) {
        this.label = label;
        this.clazz = clazz;
    }

    public String getLabel() {
        return label;
    }

    public Class<? extends CsvRow> getClazz() {
        return clazz;
    }
}