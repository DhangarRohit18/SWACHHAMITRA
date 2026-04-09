package com.example.civsetu.utils;

import android.graphics.Color;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SLAUtils {

    public static final String STATUS_ON_TRACK = "On Track";
    public static final String STATUS_AT_RISK = "At Risk";
    public static final String STATUS_BREACHED = "Breached";

    public static String getSLAStatus(Date createdAt, String priority) {
        if (createdAt == null || priority == null)
            return STATUS_ON_TRACK;

        long diffInMs = new Date().getTime() - createdAt.getTime();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs);
        long slaDays = getSLADuration(priority);

        if (diffInDays >= slaDays) {
            return STATUS_BREACHED;
        } else if (diffInDays >= slaDays * 0.75) {
            return STATUS_AT_RISK;
        } else {
            return STATUS_ON_TRACK;
        }
    }

    public static int getSLADuration(String priority) {
        switch (priority.toLowerCase()) {
            case "critical":
                return 1;
            case "high":
                return 3;
            case "medium":
                return 7;
            case "low":
            default:
                return 14;
        }
    }

    public static int getSLAStatusColor(String status) {
        switch (status) {
            case STATUS_BREACHED:
                return Color.parseColor("#BA1A1A"); // Material Error
            case STATUS_AT_RISK:
                return Color.parseColor("#E6A700"); // Amber
            case STATUS_ON_TRACK:
            default:
                return Color.parseColor("#26B09E"); // Teal/Green
        }
    }
}
