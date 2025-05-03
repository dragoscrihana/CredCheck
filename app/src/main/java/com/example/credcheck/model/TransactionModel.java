package com.example.credcheck.model;

import java.util.List;

public class TransactionModel {
    private final String transactionId;
    private final String lastUpdated;
    private final List<EventModel> events;
    private boolean expanded;

    public TransactionModel(String transactionId, String lastUpdated, List<EventModel> events) {
        this.transactionId = transactionId;
        this.lastUpdated = lastUpdated;
        this.events = events;
        this.expanded = false;
    }

    public String getTransactionId() { return transactionId; }
    public String getLastUpdated() { return lastUpdated; }
    public List<EventModel> getEvents() { return events; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}
