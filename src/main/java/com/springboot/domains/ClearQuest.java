package com.springboot.domains;

import com.opencsv.bean.CsvBindByName;

public class ClearQuest implements CsvRow {

    @CsvBindByName(column = "\uFEFFHeadline")
    private String headLine;

    @CsvBindByName(column = "CFXID")
    private String cfxId;

    @CsvBindByName(column = "SystemStructure")
    private String systemStructure;

    @CsvBindByName(column = "Description")
    private String description;

    @CsvBindByName(column = "CCBNotesLog")
    private String ccbNotesLog;

    @CsvBindByName(column = "NotesLog")
    private String notesLog;

    @CsvBindByName(column = "Attachments")
    private String attachments;

    @Override
    public String provideTitle() {
        return composeTitle(headLine, cfxId);
    }

    @Override
    public String provideDescription() {
        return """
                ## Description
                %s

                ## System structure
                %s

                ## CCB Notes
                %s

                ## Notes
                %s

                ## Attachments
                %s
                }"""
                .formatted(convertToMarkDown(description),
                        convertToMarkDown(systemStructure),
                        convertToMarkDown(ccbNotesLog),
                        convertToMarkDown(notesLog),
                        convertToMarkDown(attachments));
    }

    public String getHeadLine() {
        return headLine;
    }

    public void setHeadLine(String headLine) {
        this.headLine = headLine;
    }

    public String getCfxId() {
        return cfxId;
    }

    public void setCfxId(String cfxId) {
        this.cfxId = cfxId;
    }

    public String getSystemStructure() {
        return systemStructure;
    }

    public void setSystemStructure(String systemStructure) {
        this.systemStructure = systemStructure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCcbNotesLog() {
        return ccbNotesLog;
    }

    public void setCcbNotesLog(String ccbNotesLog) {
        this.ccbNotesLog = ccbNotesLog;
    }

    public String getNotesLog() {
        return notesLog;
    }

    public void setNotesLog(String notesLog) {
        this.notesLog = notesLog;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }
}
