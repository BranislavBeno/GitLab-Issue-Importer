package com.springboot.domains;

public interface CsvRow {

    String provideTitle();

    String provideDescription();

    default String composeTitle(String headline, String id) {
        return "%s (Origin: %s)".formatted(headline, id);
    }
}
