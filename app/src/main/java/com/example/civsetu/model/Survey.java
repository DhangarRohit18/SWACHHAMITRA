package com.example.civsetu.model;

import java.util.List;

public class Survey {
    private String id;
    private String title;
    private String description;
    private List<String> options;
    private long expiresAt;
    private double latitude;
    private double longitude;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getOptions() { return options; }
    public long getExpiresAt() { return expiresAt; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
