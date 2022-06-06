package com.springboot.domains;

import com.opencsv.bean.CsvBindByName;

public class ClearQuest {

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

    public ClearQuest() {
    }

    public ClearQuest(String headLine,
                      String cfxId,
                      String systemStructure,
                      String description,
                      String ccbNotesLog,
                      String notesLog,
                      String attachments) {
        this.headLine = headLine;
        this.cfxId = cfxId;
        this.systemStructure = systemStructure;
        this.description = description;
        this.ccbNotesLog = ccbNotesLog;
        this.notesLog = notesLog;
        this.attachments = attachments;
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
