package com.springboot.domain;

import java.util.stream.Collectors;

public interface CsvRow {

    String provideTitle();

    String provideDescription();

    default String composeTitle(String headline, String id) {
        return "%s (Origin: %s)".formatted(headline, id);
    }

    default String convertToMarkDown(String text) {
        return text.lines()
                .filter(l -> !l.isBlank())
                .map(String::trim)
                .collect(Collectors.joining("  \n"));
    }

}
