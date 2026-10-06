package com.cms.complaints.dto;

public class TrendDataDto {
    private String date;
    private long created;
    private long resolved;

    public TrendDataDto() {}

    public TrendDataDto(String date, long created, long resolved) {
        this.date = date;
        this.created = created;
        this.resolved = resolved;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getCreated() { return created; }
    public void setCreated(long created) { this.created = created; }

    public long getResolved() { return resolved; }
    public void setResolved(long resolved) { this.resolved = resolved; }
}
