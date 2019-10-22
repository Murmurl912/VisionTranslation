package com.example.visiontranslation.translation;

import androidx.annotation.NonNull;

import com.example.visiontranslation.database.DatabaseManager;
import com.example.visiontranslation.database.TranslationCache;
import com.google.gson.Gson;
import java.util.Map;



public class BaiduTranslationService {

    private static final String APP_ID = "20190911000333754";
    private static final String SECURITY_KEY = "8MkOHPjkFR1xwPngDWGZ";
    private String from = "auto";
    private String to = "zh";
    private TransApi api = new TransApi(APP_ID ,SECURITY_KEY );
    private Gson gson = new Gson();
    private String s = "";
    public static final  int STATUS_OK = 1;
    public static final  int STATUS_ERROR = 0;
    private static BaiduTranslationService service;

    public static final String CHINESE = "zh";
    public static final String ENGLISH = "en";
    public static final String JAPANESE = "jp";
    public static final String KOREAN = "kor";
    public static final String FRENCH = "fra";
    public static final String SPANISH = "spa";
    public static final String RUSSIAN = "ru";
    public static final String GERMAN = "de";
    public static final String ITALIAN = "it";

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


    private BaiduTranslationService() {

    }

    public static String[] getLanguages() {
        return languages;
    }

    public static String getCode(@NonNull String language) {
        switch (language) {
            case "Chinese": {return CHINESE;}
            case "Japanese": {return JAPANESE;}
            case "English": {return ENGLISH;}
            case "Korean": {return KOREAN;}
            case "French": {return FRENCH;}
            case "Spanish": {return SPANISH;}
            case "Russian": {return RUSSIAN;}
            case "German": {return GERMAN;}
            case "Italian": {return ITALIAN;}
            default: return CHINESE;
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
                String tempstr = "";
                int status = 1;
                try{
                    for(int i=0; i<query.length(); i++) {
                        if(query.charAt(i) == '\n') {
                            if(tempstr != "") {
                                s = s + transResult(from, to, tempstr) + "\n";
                                tempstr = "";
                            }
                            else
                                s = s + "\n";
                            continue;
                        }
                        tempstr = tempstr + query.charAt(i);
                        if(i == query.length() - 1){
                            if(tempstr != "")
                                s = s + transResult(from, to, tempstr);
                            else
                                s = s + "\n";
                        }
                    }

                    status = 1;

                }catch(Exception e)	{
                    status = 0;
                } finally {
                    rep.response(s, status);
                    s = "";
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

