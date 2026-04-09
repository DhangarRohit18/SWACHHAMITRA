package com.example.civsetu.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.example.civsetu.R;

public class LanguageUtils {
    public static void showLanguageDialog(Activity activity) {
        String[] languages = {
            "English", "Hindi", "Marathi", "Bengali", "French", 
            "German", "Gujarati", "Kannada", "Malayalam", 
            "Punjabi (Gurmukhi)", "Spanish", "Tamil", "Telugu", "Urdu"
        };
        String[] codes = {
            "en", "hi", "mr", "bn", "fr", 
            "de", "gu", "kn", "ml", 
            "pa", "es", "ta", "te", "ur"
        };
        
        new android.app.AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.switch_language))
            .setItems(languages, (dialog, which) -> {
                String targetLang = codes[which];
                String targetLabel = languages[which];
                
                PreferenceManager.getInstance(activity).saveLanguage(targetLang);
                LocaleHelper.setLocale(activity, targetLang);
                
                Toast.makeText(activity, "App language changed to " + targetLabel, Toast.LENGTH_SHORT).show();
                
                // Restart activity to apply changes
                Intent intent = activity.getIntent();
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            })
            .show();
    }
}
