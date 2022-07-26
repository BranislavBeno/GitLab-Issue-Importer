package com.issue.importer.domain;

import java.util.Arrays;
import java.util.List;

public enum CsvType {

    CLEAR_QUEST("ClearQuest", ClearQuest.class, true), USER("User", User.class, false);

    private final String label;
    private final Class<? extends CsvRow> clazz;
    private final boolean forProduction;

    CsvType(String label, Class<? extends CsvRow> clazz, boolean forProduction) {
        this.label = label;
        this.clazz = clazz;
        this.forProduction = forProduction;
    }

    public String getLabel() {
        return label;
    }

    public Class<? extends CsvRow> getClazz() {
        return clazz;
    }

    public boolean isForProduction() {
        return forProduction;
    }

    public static final List<CsvType> FOR_PRODUCTION = Arrays.stream(values())
            .filter(CsvType::isForProduction)
            .toList();
}