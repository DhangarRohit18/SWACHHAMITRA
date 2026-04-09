package com.example.civsetu.utils;

import android.content.Context;
import com.example.civsetu.R;

public class CategoryUtils {
    public static String getLocalizedCategory(Context context, String englishCategory) {
        if (englishCategory == null) return "Other";
        
        String[] englishCats = { 
            "Potholes", "Water Supply", "Garbage", "Street Lights", "Drainage", 
            "Roads", "Electricity", "Waste Management", "Public Health", "Other" 
        };
        String[] localCats = context.getResources().getStringArray(R.array.categories_array);
        
        for (int i = 0; i < englishCats.length; i++) {
            if (englishCats[i].equalsIgnoreCase(englishCategory)) {
                return (i < localCats.length) ? localCats[i] : englishCategory;
            }
        }
        return englishCategory;
    }

    public static String getEnglishCategory(Context context, String localizedCategory) {
        String[] englishCats = { 
            "Potholes", "Water Supply", "Garbage", "Street Lights", "Drainage", 
            "Roads", "Electricity", "Waste Management", "Public Health", "Other" 
        };
        String[] localCats = context.getResources().getStringArray(R.array.categories_array);
        
        for (int i = 0; i < localCats.length; i++) {
            if (localCats[i].equalsIgnoreCase(localizedCategory)) {
                return (i < englishCats.length) ? englishCats[i] : localizedCategory;
            }
        }
        return localizedCategory;
    }
}
