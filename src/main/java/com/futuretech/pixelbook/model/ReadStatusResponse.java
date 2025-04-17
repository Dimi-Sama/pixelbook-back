package com.futuretech.pixelbook.model;

import java.util.Date;

public class ReadStatusResponse {
    private boolean read;
    private Date readAt;

    public ReadStatusResponse(boolean read, Date readAt) {
        this.read = read;
        this.readAt = readAt;
    }

    // Getters et setters
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Date getReadAt() {
        return readAt;
    }

    public void setReadAt(Date readAt) {
        this.readAt = readAt;
    }
} 