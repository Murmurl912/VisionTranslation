package com.example.visiontranslation.translation;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;



public class BaiduTranslationService {

    private static final String APP_ID = "20190911000333754";
    private static final String SECURITY_KEY = "8MkOHPjkFR1xwPngDWGZ";
    private String from = "auto";
    private String to = "zh";
    private String transResult;
    private TranJson result;
    private TransApi api = new TransApi(APP_ID ,SECURITY_KEY );
    private Gson gson = new Gson();
    private String s;

    private static BaiduTranslationService service;

    public static BaiduTranslationService getBaiduTranslationService() {
        if(service == null) {
            service =  new BaiduTranslationService();
        }

        return service;
    }

    private String transResult(String query) {
        transResult = api.getTransResult(query, from, to);
        result = gson.fromJson(transResult, TranJson.class);
        return (String) result.trans_result[0].get("dst");
    }

    private String transResult(String to, String query) {
        transResult = api.getTransResult(query, from, to);
        result = gson.fromJson(transResult, TranJson.class);
        return (String) result.trans_result[0].get("dst");
    }

    private String transResult(String from, String to, String query) {
        transResult = api.getTransResult(query, from, to);
        result = gson.fromJson(transResult, TranJson.class);
        return (String) result.trans_result[0].get("dst");
    }

    public void request(final String query, final Response rep) {
        Runnable run = new Runnable () {
            public void run() {
                int status = 1;
                try{
                    s = transResult(to,query);
                }catch(Exception e)	{
                    status = 0;
                }
                System.out.print(s);
                rep.response(s, status);

            }
        };
        Thread tran = new Thread(run);
        tran.start();

    }

    public void request(final String to, final String query, final Response rep) {
        Runnable run = new Runnable () {
            public void run() {
                int status = 1;
                try{
                    s = transResult(to, query);
                    Log.i("RESULT", s);
                }catch(Exception e)	{
                    status = 0;
                    Log.i("NNNNN",status+"w");
                }
                System.out.print(status);
                rep.response(s, status);

            }
        };
        Thread tran = new Thread(run);
        tran.start();

    }

    public void request(String from, final String to, final String query, final Response rep) {
        Runnable run = new Runnable () {
            public void run() {
                int status = 1;
                try{
                    s = transResult(to, query);
                }catch(Exception e)	{
                    status = 0;
                }
                System.out.print(s);
                rep.response(s, status);

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

