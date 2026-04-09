package com.example.civsetu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class PreferenceManager {
    private static final String PREF_NAME = "CivSetuPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_DEPT = "user_dept";
    private static final String KEY_EMAIL = "user_email";
    private static PreferenceManager instance;
    private SharedPreferences sharedPreferences;

    private PreferenceManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Fallback to regular prefs if something goes wrong (not ideal for production
            // but prevents crash)
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void saveRole(String role) {
        sharedPreferences.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public void saveDepartment(String dept) {
        sharedPreferences.edit().putString(KEY_DEPT, dept).apply();
    }

    public String getDepartment() {
        return sharedPreferences.getString(KEY_DEPT, null);
    }

    public void saveEmail(String email) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public void saveLanguage(String lang) {
        sharedPreferences.edit().putString("app_lang", lang).apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString("app_lang", "en");
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
