package com.unibuc.management.dto;

import java.time.OffsetDateTime;

public class ScheduleEntry {

    private String title;
    private OffsetDateTime from;
    private OffsetDateTime to;
    private Object entry;

    public ScheduleEntry(String title, OffsetDateTime from, OffsetDateTime to, Object entry) {
        this.title = title;
        this.from = from;
        this.to = to;
        this.entry = entry;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OffsetDateTime getFrom() {
        return from;
    }

    public void setFrom(OffsetDateTime from) {
        this.from = from;
    }

    public OffsetDateTime getTo() {
        return to;
    }

    public void setTo(OffsetDateTime to) {
        this.to = to;
    }

    public Object getEntry() {
        return entry;
    }

    public void setEntry(Object entry) {
        this.entry = entry;
    }
}
