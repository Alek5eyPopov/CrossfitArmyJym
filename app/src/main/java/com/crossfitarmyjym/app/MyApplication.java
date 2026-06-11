package com.crossfitarmyjym.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.jakewharton.threetenabp.AndroidThreeTen;


/**
 * Основной класс приложения.
 * Инициализирует глобальные компоненты:
 * - ThreeTenABP для работы с датами на API < 26
 * - EncryptedSharedPreferences для безопасного хранения токенов
 */
public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private static final String ENCRYPTED_PREFS_FILE = "encrypted_prefs";

    // Singleton instance
    private static MyApplication instance;

    // Encrypted SharedPreferences for secure token storage
    private SharedPreferences encryptedSharedPreferences;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "Application onCreate() called");
        
        // Инициализация ThreeTenABP для работы с датами на старых API
        AndroidThreeTen.init(this);
        Log.d(TAG, "ThreeTenABP initialized");
        
        // Инициализация EncryptedSharedPreferences
        initEncryptedSharedPreferences();
        Log.d(TAG, "EncryptedSharedPreferences initialized");
    }

    /**
     * Инициализация EncryptedSharedPreferences для безопасного хранения
     * JWT-токенов и других чувствительных данных.
     */
    private void initEncryptedSharedPreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedSharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    ENCRYPTED_PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            
            Log.d(TAG, "EncryptedSharedPreferences created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences", e);
            // Fallback to regular SharedPreferences (less secure)
            encryptedSharedPreferences = getSharedPreferences(
                    ENCRYPTED_PREFS_FILE, 
                    Context.MODE_PRIVATE
            );
            Log.w(TAG, "Falling back to regular SharedPreferences");
        }
    }

    /**
     * Получение singleton instance приложения.
     * @return MyApplication instance
     */
    @NonNull
    public static MyApplication getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Application not initialized yet");
        }
        return instance;
    }

    /**
     * Получение EncryptedSharedPreferences.
     * @return EncryptedSharedPreferences instance
     */
    @NonNull
    public SharedPreferences getEncryptedSharedPreferences() {
        if (encryptedSharedPreferences == null) {
            throw new IllegalStateException("EncryptedSharedPreferences not initialized");
        }
        return encryptedSharedPreferences;
    }

    /**
     * Очистка всех сохраненных данных (используется при выходе).
     */
    public void clearAllData() {
        if (encryptedSharedPreferences != null) {
            encryptedSharedPreferences.edit().clear().apply();
            Log.d(TAG, "All encrypted preferences cleared");
        }
    }
}