package com.example.civsetu.utils;

public class AICategorizer {
    public static String suggestCategory(String title, String description) {
        String combined = (title + " " + description).toLowerCase();
        
        if (combined.contains("pothole")) {
            return "Potholes";
        } else if (combined.contains("water") || combined.contains("leak") || combined.contains("pipe")) {
            return "Water Supply";
        } else if (combined.contains("garbage") || combined.contains("waste") || combined.contains("trash")) {
            return "Garbage";
        } else if (combined.contains("light") || combined.contains("lamp")) {
            return "Street Lights";
        } else if (combined.contains("drain") || combined.contains("sewer") || combined.contains("drainage")) {
            return "Drainage";
        } else if (combined.contains("road") || combined.contains("pavement")) {
            return "Roads";
        } else if (combined.contains("electricity") || combined.contains("power") || combined.contains("wire")) {
            return "Electricity";
        } else if (combined.contains("clean") || combined.contains("sanitation")) {
            return "Waste Management";
        } else if (combined.contains("health") || combined.contains("hospital") || combined.contains("mosquito")) {
            return "Public Health";
        }
        
        return "Other";
    }
}
