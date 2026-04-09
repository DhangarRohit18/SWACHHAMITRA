package com.example.civsetu.model;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String text;
    private long timestamp;

    public Comment() {
    }

    public Comment(String userId, String userName, String text, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestampFromObject(Object timestamp) {
        if (timestamp instanceof Long) {
            this.timestamp = (Long) timestamp;
        } else if (timestamp instanceof Double) {
            this.timestamp = ((Double) timestamp).longValue();
        } else if (timestamp instanceof String) {
            try {
                this.timestamp = Long.parseLong((String) timestamp);
            } catch (NumberFormatException e) {
                this.timestamp = 0;
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
