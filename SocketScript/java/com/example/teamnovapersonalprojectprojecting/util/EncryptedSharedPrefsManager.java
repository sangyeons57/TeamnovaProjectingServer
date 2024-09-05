package com.example.teamnovapersonalprojectprojecting.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedSharedPrefsManager {
    public static String LOGIN = "encrypted_sharedPrefs_login";

    private static SharedPreferences sharedPrefs;

    public static void init(Context context, String fileName) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPrefs = EncryptedSharedPreferences.create(
                    context,
                    fileName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            sharedPrefs = null;
        }
    }

    public static void putString(String key, String value) {
        if (sharedPrefs != null) {
            sharedPrefs.edit().putString(key, value).apply();
        }
    }

    public static String getString(String key, String defaultValue) {
        if (sharedPrefs != null) {
            return sharedPrefs.getString(key, defaultValue);
        }
        return defaultValue;
    }

    public static void removeData(String key){
        if(sharedPrefs != null){
            sharedPrefs.edit().remove(key).apply();
        }
    }
    public static void clearFileData(){
        sharedPrefs.edit().clear().apply();
    }

    public static  boolean hasKey(String key, boolean defaultValue) {
        if(sharedPrefs != null) {
            return sharedPrefs.contains(key);
        }
        return defaultValue;
    }
}