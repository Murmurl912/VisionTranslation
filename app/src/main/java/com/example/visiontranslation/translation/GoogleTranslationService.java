package com.example.visiontranslation.translation;

import androidx.annotation.NonNull;

import com.example.visiontranslation.database.DatabaseManager;
import com.example.visiontranslation.database.TranslationCache;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//ToDo: Add model manager
//ToDo: Add dialog to show download info
//ToDo: Fix translation fail at first attempt bug

public class GoogleTranslationService {

    public static String[] languages = new String[] {
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

    private static boolean taskA = false;
    private static boolean taskB = false;

    private GoogleTranslationService() {

    }

    public static int getCode(String language) {
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

    public static void downloadModel(FirebaseTranslateRemoteModel model, ModelDownloadCallback callback) {
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();
        Task<Void> task = FirebaseModelManager
                .getInstance()
                .download(model, conditions)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onDownloadFailure(e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            callback.onDownloadComplete();
                        } else {
                            callback.onDownloadFailure(task.getException());
                        }
                    }
                });

    }

    public static void getTranslator(int source, int target, @NonNull TranslatorInitializeListener listener) {
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(source)
                        .setTargetLanguage(target)
                        .build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();

        FirebaseTranslateRemoteModel sourceModel = new FirebaseTranslateRemoteModel
                .Builder(source).build();
        FirebaseTranslateRemoteModel targetModel = new FirebaseTranslateRemoteModel
                .Builder(target).build();

        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();

        modelManager.isModelDownloaded(sourceModel).addOnCompleteListener(new OnCompleteListener<Boolean>() {

            boolean isSourceDownload = false;
            boolean isTargetDownload = false;

            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                    if(task.isSuccessful()) {
                        if(task.getResult() != null) {
                            isSourceDownload = task.getResult();
                        }
                    }
                    modelManager.isModelDownloaded(targetModel).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if(task.isSuccessful()) {
                                if(task.getResult() != null) {
                                    isTargetDownload = task.getResult();
                                }
                            }

                            if(isSourceDownload && isTargetDownload) {
                                listener.onSuccess(
                                        FirebaseNaturalLanguage
                                                .getInstance()
                                                .getTranslator(options)
                                );
                            } else {
                                listener.onRequestModel(sourceModel, targetModel);
                            }
                        }
                    });
            }
        });

    }

    public static void request(@NonNull String from,
                        @NonNull String to,
                        @NonNull String value,
                        @NonNull TranslationCallback callback) {

        TranslationCache cache = DatabaseManager.getTranslationCache();
        TranslationCache.Entry entry = cache.find(from, to, value);
        if(entry == null) {
            int s = getCode(from);
            int t = getCode(to);
            getTranslator(s, t, new TranslatorInitializeListener() {
                @Override
                public void onSuccess(@NonNull FirebaseTranslator translator) {
                    translator.translate(value)
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if(task.isSuccessful()) {
                                        String result = task.getResult();
                                        if(result != null) {
                                            callback.onTranslationSuccess(from, to, value, task.getResult());
                                            cache.put(from, to, value, task.getResult());
                                            return;
                                        }
                                    }
                                    callback.onTranslationFailure(from, to, value, task.getException());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    callback.onTranslationFailure(from, to, value, e);
                                }
                            });
                }

                @Override
                public void onRequestModel(FirebaseTranslateRemoteModel source, FirebaseTranslateRemoteModel target) {
                    callback.onRequireDownloadModel(from, to, value, source, target);
                }
            });
        } else {
            callback.onTranslationSuccess(entry.getFrom(), entry.getTo(), entry.getQuery(), entry.getResult());
        }
    }


    public static Task<Set<FirebaseTranslateRemoteModel>> getDownloadedModels() {
        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
        return modelManager.getDownloadedModels(FirebaseTranslateRemoteModel.class);
    }

    public static Task<Boolean> isModelDownload(int code) {
        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
        return modelManager.isModelDownloaded(new FirebaseTranslateRemoteModel.Builder(code).build());
    }


    public interface TranslationCallback {

        public void onTranslationSuccess(String from, String to, String value, String result);

        public void onTranslationFailure(String from, String to, String value, Exception e);

        public void onRequireDownloadModel(String from, String to, String value, FirebaseTranslateRemoteModel source, FirebaseTranslateRemoteModel target);
    }

    public interface ModelDownloadCallback {
        public void onDownloadComplete();

        public void onDownloadFailure(Exception e);
    }

    public interface TranslatorInitializeListener {
        public void onSuccess(@NonNull FirebaseTranslator translator);
        public void onRequestModel(FirebaseTranslateRemoteModel source, FirebaseTranslateRemoteModel target);
    }
}
