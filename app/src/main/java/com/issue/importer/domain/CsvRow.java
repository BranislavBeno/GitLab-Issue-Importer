package com.issue.importer.domain;

import java.util.stream.Collectors;

public interface CsvRow {

    String provideTitle();

    String provideDescription();

    default String composeTitle(String headline, String id) {
        return "%s (Origin: %s)".formatted(headline, id);
    }

    default String convertToMarkDown(String text) {
        String lines = text.lines()
                .filter(l -> !l.isBlank())
                .map(String::trim)
                .collect(Collectors.joining("\n"));

        return replaceMdSpecifics(lines);
    }

    private static String replaceMdSpecifics(String text) {
        return text.lines()
                .map(CsvRow::replaceMdBoldText)
                .collect(Collectors.joining("  \n"));
    }

    private static String replaceMdBoldText(String l) {
        if (l.startsWith("===")) {
            return l.replaceAll("(^===.*$)", "\n$1");
        }
        return l;
    }

}
