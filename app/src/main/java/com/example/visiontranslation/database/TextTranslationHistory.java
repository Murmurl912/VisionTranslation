package com.example.visiontranslation.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TextTranslationHistory {
    private Context context;
    private SQLiteDatabase database;
    private final String dbname = "history_database";
    private List<TranslationHistory> histories;

    public TextTranslationHistory(Context context) {
        this.context = context;
        openOrCreateDatabase();
        histories = new ArrayList<>();
    }

    private void openOrCreateDatabase() {
        database = context.openOrCreateDatabase("history_database", Context.MODE_PRIVATE, null);
        if(database.getVersion() == 0) {
            database.execSQL("create table History (" +
                    "source text, " +
                    "target text, " +
                    "source_lg text, " +
                    "target_lg text, " +
                    "time integer, " +
                    "is_favorite integer, " +
                    "primary key(source, target_lg))");
            database.setVersion(1);
        }

    }

    public void close() {
        database.close();
    }

    public boolean contain(TranslationHistory history) {

        if(database == null || !database.isOpen()) {
            openOrCreateDatabase();
        }

        if(history == null) {
            return false;
        }
        if(histories.isEmpty()) {
            Cursor cursor = database.rawQuery("select * from History", null);
            while (cursor.moveToNext()) {
                histories.add(new TranslationHistory(
                        cursor.getLong(4),
                        cursor.getInt(5) != 0,
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                        ));
            }
            cursor.close();
        }
        return histories.contains(history);
    }

    public void add(TranslationHistory history) {
        if(database == null || !database.isOpen()) {
            openOrCreateDatabase();
        }


        if(history == null) {
            return;
        }

        if(contain(history)) {
            return;
        }
        database.execSQL("insert into History values("
                + "'" + escapeSingleQuote(history.getSource()) + "', "
                + "'" + escapeSingleQuote(history.getTarget()) + "', "
                + "'" + escapeSingleQuote(history.getSource_lg()) + "', "
                + "'" + escapeSingleQuote(history.getTarget_lg()) + "', "
                + history.getTime()+ ", "
                + (history.isFavorite() ? 1 : 0) + ")");
        histories.add((TranslationHistory) history.clone());
    }

    public void remove(TranslationHistory history) {
        if(database == null || !database.isOpen()) {
            openOrCreateDatabase();
        }

        if(history == null || history.getSource().equals("")) {
            return;
        }

        if(contain(history)) {
            database.execSQL("delete from History where source = '"
                    + escapeSingleQuote(history.getSource())
                    + "' and source_lg = '" +
                    escapeSingleQuote(history.getTarget_lg() +"'"));
        }
        histories.remove(history);
    }

    public void update(TranslationHistory value) {
        if(database == null || !database.isOpen()) {
            openOrCreateDatabase();
        }

        if(value == null) {
            return;
        }

        database.execSQL("update History set target = " +
                "'" + escapeSingleQuote(value.getTarget()) +
                "', time = " + value.getTime() +
                ", is_favorite = " + (value.isFavorite() ? 1 : 0) +
                " where source = '" + escapeSingleQuote(value.getSource()) +
                "' and target_lg = '" + escapeSingleQuote(value.getTarget_lg()) + "'"
        );
        TranslationHistory history = histories.get(histories.indexOf(value));
        history.setTime(value.getTime());
        history.setFavorite(value.isFavorite());
        history.setTarget(value.getTarget());
        history.setSource_lg(value.getSource_lg());
        history.setTarget_lg(value.getTarget_lg());
    }

    public void requestLoadHistoryRecord(List<TranslationHistory> container, OnHistoryRecordLoadComplete callback) {
        if(database == null || !database.isOpen()) {
            openOrCreateDatabase();
        }

        List<TranslationHistory> list = container == null ? new ArrayList<>() : container;
        if(histories.isEmpty()) {
            Cursor cursor = database.rawQuery("select * from History", null);
            while (cursor.moveToNext()) {
                histories.add(new TranslationHistory(
                        cursor.getLong(4),
                        cursor.getInt(5) != 0,
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
            cursor.close();
        }

        for(TranslationHistory h : histories) {
            list.add((TranslationHistory) h.clone());
        }

        callback.onHistoryRecordLoadComplete(0, list);
    }

    public String escapeSingleQuote(String input) {
        StringBuilder output = new StringBuilder();
        if(input == null || input.equals("")) {
            return output.toString();
        }
        char[] chars = input.toCharArray();
        for(char c : chars) {
            if(c == '\'') {
                output.append('\\').append(c);
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    public interface OnHistoryRecordLoadComplete {
        void onHistoryRecordLoadComplete(int status, List<TranslationHistory> data);
    }

    public static class TranslationHistory {
        private boolean isFavorite;
        private long time;
        private String source;
        private String target;
        private String source_lg;
        private String target_lg;

        public TranslationHistory(long time,
                                  boolean isFavorite,
                                  String source,
                                  String target,
                                  String source_lg,
                                  String target_lg) {
            this.time = time;
            this.isFavorite = isFavorite;
            this.source = source == null ? "" : source;
            this.target = target == null ? "" : target;
            this.source_lg = source_lg;
            this.target_lg = target_lg;
        }

        public void setFavorite(boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void setSource(String source) {
            this.source = source == null ? "" : source;
        }

        public void setTarget(String target) {
            this.target = target == null ? "" : target;
        }

        public boolean isFavorite() {
            return isFavorite;
        }

        public long getTime() {
            return time;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public String getSource_lg() {
            return source_lg;
        }

        public void setSource_lg(String source_lg) {
            this.source_lg = source_lg;
        }

        public String getTarget_lg() {
            return target_lg;
        }

        public void setTarget_lg(String target_lg) {
            this.target_lg = target_lg;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TranslationHistory history = (TranslationHistory) o;

            if (!source.equals(history.source)) return false;
            return target_lg.equals(history.target_lg);
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + target_lg.hashCode();
            return result;
        }

        @NonNull
        @Override
        public Object clone() {
            return new TranslationHistory(time, isFavorite, source, target, source_lg, target_lg);
        }
    }
}
