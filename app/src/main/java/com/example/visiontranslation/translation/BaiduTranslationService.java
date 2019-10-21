package com.example.visiontranslation.translation;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.visiontranslation.database.DatabaseManager;
import com.example.visiontranslation.database.TranslationCache;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;



public class BaiduTranslationService {

    private static final String APP_ID = "20190911000333754";
    private static final String SECURITY_KEY = "8MkOHPjkFR1xwPngDWGZ";
    private String from = "auto";
    private String to = "zh";
    private TransApi api = new TransApi(APP_ID ,SECURITY_KEY );
    private Gson gson = new Gson();
    private String s;
    public static final  int STATUS_OK = 1;
    public static final  int STATUS_ERROR = 0;
    private static BaiduTranslationService service;

    public static final String AUTO = "auto";
    public static final String CHINESE = "zh";
    public static final String ENGLISH = "en";
    public static final String JAPANESE = "jp";
    public static final String KOREAN = "kor";
    public static final String FRENCH = "fra";
    public static final String SPANISH = "spa";
    public static final String RUSSIAN = "ru";
    public static final String GERMAN = "de";
    public static final String ITALIAN = "it";
    public static final String CHINESE_TRADITIONAL = "cht";
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
            "Italian",
            "Chinese Tradition"
    };


    private BaiduTranslationService() {

    }

    public static String[] getLanguages() {
        return languages;
    }

    public static String getCode(@NonNull String language) {
        switch (language) {
            case "Auto": { return AUTO;}
            case "Chinese": {return CHINESE;}
            case "Japanese": {return JAPANESE;}
            case "English": {return ENGLISH;}
            case "Korean": {return KOREAN;}
            case "French": {return FRENCH;}
            case "Spanish": {return SPANISH;}
            case "Russian": {return RUSSIAN;}
            case "German": {return GERMAN;}
            case "Italian": {return ITALIAN;}
            case "Chinese Tradition":{return CHINESE_TRADITIONAL;}
            default: return AUTO;
        }
    }

    public static BaiduTranslationService getBaiduTranslationService() {
        if(service == null) {
            service =  new BaiduTranslationService();
        }

        return service;
    }

    private String transResult(@NonNull String from, @NonNull String to, @NonNull String query) {
        TranslationCache cache = DatabaseManager.getTranslationCache();
        TranslationCache.Entry entry = cache.find(from, to, query);
        String  translation = "";
        if(entry == null) {
            String transResult = api.getTransResult(query, from, to);
            TranJson result = gson.fromJson(transResult, TranJson.class);
            translation = (String) result.trans_result[0].get("dst");
            cache.put(from, to, query, translation);
        } else{
            translation = entry.getResult();
        }

        return translation;
    }

    public void request(@NonNull String from, @NonNull String to, @NonNull String query,@NonNull Response rep) {

        Runnable run = new Runnable () {
            public void run() {
                int status = 1;
                try{
                    s = transResult(from, to,query);
                }catch(Exception e)	{
                    status = 0;
                } finally {
                    rep.response(s, status);
                }
            }
        };
        Thread tran = new Thread(run);
        tran.start();

    }

    public interface Response {
        public void response(String s, int status);
    }

    class TranJson{
        public String from;
        public String to;
        public Map[] trans_result;
    }
}

