package com.example.visiontranslation;

import android.app.Application;

public class VisionTranslationApplication extends Application {

    private static VisionTranslationApplication application;

    public VisionTranslationApplication() {
        super();
        application = this;
    }

    public static void getDatabaseManager() {

    }

    public static void getSourceLanguage() {

    }

    public static VisionTranslationApplication getVisionTranslationApplication() {
        return application;
    }
}
