package com.example.credcheck.model;

public class EventModel {
    private final String event;
    private final String actor;
    private final String timestamp;
    private final String details;
    private boolean expanded;

    public EventModel(String event, String actor, String timestamp, String details) {
        this.event = event;
        this.actor = actor;
        this.timestamp = timestamp;
        this.details = details;
        this.expanded = false;
    }

    public String getEvent() { return event; }
    public String getActor() { return actor; }
    public String getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}

