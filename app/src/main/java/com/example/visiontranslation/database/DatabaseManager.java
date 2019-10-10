package com.example.visiontranslation.database;

import android.content.Context;

import com.example.visiontranslation.VisionTranslationApplication;

public class DatabaseManager {
    private static TranslationCache translationCache;
    private static DatabaseManager manager;
    private Context context;

    private DatabaseManager(Context context) {
        this.context = context;
    }

    public static DatabaseManager getInstance() {
        if(manager == null) {
            manager = new DatabaseManager(
                    VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
            );
        }
        return manager;

    }

    public TranslationCache getTranslationCache() {
        if(translationCache == null) {
            translationCache = new TranslationCache(
                    VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
            );
        }
        return translationCache;
    }

}
