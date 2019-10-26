package com.example.visiontranslation.database;

import com.example.visiontranslation.VisionTranslationApplication;

public class DatabaseManager {
    private static TranslationCache translationCache;
    private static TextTranslationHistory textTranslationHistory;

    public static TranslationCache getTranslationCache() {
        if(translationCache == null) {
            translationCache = new TranslationCache(
                    VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
            );
        }
        return translationCache;
    }

    public static TextTranslationHistory getTextTranslationHistory() {
        if(textTranslationHistory == null) {
            textTranslationHistory = new TextTranslationHistory(VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext());
        }
        return textTranslationHistory;
    }

    public static void close() {
        if(translationCache != null){
            translationCache.close();
        }

        if(textTranslationHistory != null) {
            textTranslationHistory.close();
        }
    }

}
