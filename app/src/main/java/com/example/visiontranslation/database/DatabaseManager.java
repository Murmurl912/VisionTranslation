package com.example.visiontranslation.database;

import android.content.Context;

import com.example.visiontranslation.VisionTranslationApplication;

public class DatabaseManager {
    private static TranslationCache translationCache;

    public static TranslationCache getTranslationCache() {
        if(translationCache == null) {
            translationCache = new TranslationCache(
                    VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
            );
        }
        return translationCache;
    }

}
