package com.example.visiontranslation.ui.voice;

public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    private String content;
    private int type;
    private String lang = "";
    private String target = "";

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public Msg(String content, int type, String lang, String target) {
        this(content, type);
        this.lang = lang;
        this.target = target;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setLanguage(String lang) {
        this.lang = lang;
    }

    public String getLanguage() {
        return lang;
    }

    public int getType() {
        return type;
    }

    public String getTarget(){
        return target;
    }
}
