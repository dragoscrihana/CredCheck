package com.example.credcheck.model;

import java.util.List;

public class HistoryEntry {
    private final String transactionId;
    private final List<String> eventDetails;
    private boolean expanded;

    public HistoryEntry(String transactionId, List<String> eventDetails) {
        this.transactionId = transactionId;
        this.eventDetails = eventDetails;
        this.expanded = false;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<String> getEventDetails() {
        return eventDetails;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }
}
