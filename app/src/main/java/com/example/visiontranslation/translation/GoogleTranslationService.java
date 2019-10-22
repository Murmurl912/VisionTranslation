package com.example.visiontranslation.translation;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

//ToDo: Add model manager
//ToDo: Add dialog to show download info
//ToDo: Fix translation fail at first attempt bug

public class GoogleTranslationService {

    public static String[] languages = new String[] {
            "Auto",
            "Chinese",
            "Japanese",
            "English",
            "Korean",
            "French",
            "Spanish",
            "Russian",
            "German",
            "Italian"
    };

    public static final int CHINESE = FirebaseTranslateLanguage.ZH;
    public static final int JAPANESE = FirebaseTranslateLanguage.JA;
    public static final int ENGLISH = FirebaseTranslateLanguage.EN;
    public static final int KOREAN = FirebaseTranslateLanguage.KO;
    public static final int FRENCH = FirebaseTranslateLanguage.FR;
    public static final int SPANISH = FirebaseTranslateLanguage.ES;
    public static final int RUSSIAN = FirebaseTranslateLanguage.RU;
    public static final int GERMAN = FirebaseTranslateLanguage.DE;
    public static final int ITALIAN = FirebaseTranslateLanguage.IT;

    private GoogleTranslationService() {

    }

    private static int getCode(String language) {
        switch (language) {
            case "Chinese": return CHINESE;
            case "Japanese": return JAPANESE;
            case "English": return ENGLISH;
            case "Korean": return KOREAN;
            case "French": return FRENCH;
            case "Spanish": return SPANISH;
            case "Russian": return RUSSIAN;
            case "German": return GERMAN;
            case "Italian": return ITALIAN;
            default: return CHINESE;
        }
    }

    public static void download() {

    }

    public static boolean isModelDownload(FirebaseTranslateRemoteModel model) {
        boolean flag = false;

        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
        Task<Boolean> task = modelManager.isModelDownloaded(model);
        if(task.isComplete() && task.isSuccessful()) {
            Boolean res = task.getResult();
            if(res != null) {
                flag = res;
            }
        }
        return flag;
    }

    public static void downloadModel(FirebaseTranslateRemoteModel model) {
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();
        FirebaseModelManager
                .getInstance()
                .download(model, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                        } else {

                        }
                    }
                });
    }

    public static void getTranslator(int source, int target, @NonNull OnTranslatorInitializeComplete callback) {
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(source)
                        .setTargetLanguage(target)
                        .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage
                .getInstance()
                .getTranslator(options);


        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                })
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                callback.onSuccess(translator);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              callback.onFailure(e);
                            }
                        });

    }


    public interface OnTranslatorInitializeComplete {
        public void onSuccess(@NonNull FirebaseTranslator translator);
        public void onFailure(Exception e);
    }

    public static void request(@NonNull String from,
                        @NonNull String to,
                        @NonNull String value,
                        @NonNull TranslationCallback callback) {
        int s = getCode(from);
        int t = getCode(to);
        getTranslator(s, t, new OnTranslatorInitializeComplete() {
            @Override
            public void onSuccess(@NonNull FirebaseTranslator translator) {
                Task<String> result = translator.translate(value);
                result.addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()) {
                            callback.onTranslationSuccess(from, to, value, task.getResult());
                        } else {
                            callback.onTranslationFailure(from, to, value, task.getException());
                        }
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onTranslationFailure(from, to, value, e);
            }
        });
    }

    public interface TranslationCallback {

        public void onTranslationSuccess(String from, String to, String value, String result);

        public void onTranslationFailure(String from, String to, String value, Exception e);

        public void onRequireDownloadModel(String from, String to, String value);
    }


}
