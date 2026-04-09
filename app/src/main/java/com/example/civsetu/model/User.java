package com.example.civsetu.model;

public class User {
    private String uid;
    private String name;
    private String email;
    private String role; // "Citizen" or "Official"
    private int impactPoints;
    private int issuesReported;
    private int issuesResolved;

    public User() {
    }

    public User(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.impactPoints = 0;
        this.issuesReported = 0;
        this.issuesResolved = 0;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getImpactPoints() {
        return impactPoints;
    }

    public void setImpactPoints(int impactPoints) {
        this.impactPoints = impactPoints;
    }

    public int getIssuesReported() {
        return issuesReported;
    }

    public void setIssuesReported(int issuesReported) {
        this.issuesReported = issuesReported;
    }

    public int getIssuesResolved() {
        return issuesResolved;
    }

    public void setIssuesResolved(int issuesResolved) {
        this.issuesResolved = issuesResolved;
    }
}
