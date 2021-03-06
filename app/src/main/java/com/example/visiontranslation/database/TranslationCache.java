package com.example.visiontranslation.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TranslationCache {

    private SQLiteDatabase database;
    private final String dname = "TranslationCacheDatabase";
    private final String createSql = "" +
            "create table TranslationCache (" +
            "id number primary key," +
            "source text," +
            "target text," +
            "value text ," +
            "result text)";

    private Context context;

    private Map<String, Map<String, Map<String, Entry>>> maps;

    private final int version = 1;

    protected TranslationCache(Context context) {
        this.context = context;
        maps = new HashMap<>();
        createOrOpenDatabase();
    }

    private void createOrOpenDatabase() {
        database = context.openOrCreateDatabase(dname, Context.MODE_PRIVATE, null);
        if(database.getVersion() != version) {
            database.execSQL(createSql);
            database.setVersion(version);
        }
    }

    public void close() {
        database.close();
    }


    @Nullable
    public Entry find(@NonNull String from, @NonNull String to, @NonNull String query) {
        if(!database.isOpen()) {
            createOrOpenDatabase();
        }

        if(maps.containsKey(from)) {
            Map<String, Map<String, Entry>> mapa = maps.get(from);
            if(mapa != null && mapa.containsKey(to)) {
                Map<String, Entry> mapb = mapa.get(to);
                if(mapb != null && mapb.containsKey(query)) {
                    return mapb.get(query);
                }
            }
        }

        return query(from, to, query);
    }

    @Nullable
    private Entry query(@NonNull String from, @NonNull String to, @NonNull String query) {
        Cursor cursor = database.rawQuery(
                "select * from TranslationCache where source like '"
                        + Helper.escapeSingleQuote(from) + "' and target like '"
                + Helper.escapeSingleQuote(to) + "' and value like '" +
                Helper.escapeSingleQuote(query) + "'", null);

        Entry entry = null;
        if (cursor.moveToNext()) {
            entry = new Entry(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            );
            putToMap(entry);
        }
        cursor.close();
        return entry;
    }

    private void putToMap(Entry entry) {
        // from -> (to -> (query -> entry) )
        if(maps.containsKey(entry.getFrom())) {
            Map<String, Map<String, Entry>> mapa = maps.get(entry.getFrom());
            // to -> (query -> entry)
            if(mapa.containsKey(entry.getTo())) {
                Map<String, Entry> mapb = mapa.get(entry.getTo());
                mapb.put(entry.getQuery(), entry);
            } else {
                Map<String, Entry> mapb = new HashMap<>();
                mapb.put(entry.getQuery(), entry);
                mapa.put(entry.getTo(), mapb);
            }

        } else {
            HashMap<String, Map<String, Entry>> mapa = new HashMap<>();
            HashMap<String, Entry> mapb = new HashMap<>();
            mapb.put(entry.getQuery(), entry);
            mapa.put(entry.getTo(), mapb);
            maps.put(entry.getFrom(), mapa);
        }

    }

    public void put(@NonNull String from, @NonNull String to, @NonNull String query, String result) {

        if(!database.isOpen()) {
            createOrOpenDatabase();
        }


        Entry entry = new Entry(System.currentTimeMillis(), from, to, query, result);
        putToMap(entry);
        database.execSQL(
                "insert into TranslationCache values("
                        + entry.id + ", '"
                        + Helper.escapeSingleQuote(entry.from) + "', '"
                        + Helper.escapeSingleQuote(entry.to) + "', '"
                        + Helper.escapeSingleQuote(entry.query) + "', '"
                        + Helper.escapeSingleQuote(entry.result) + "')"
        );
    }

    public void update(long id, String result) {
        if(database.isOpen()) {
            createOrOpenDatabase();
        }
    }

    public void remove(String from, String to, String query) {

    }

    public void clearCache() {
        if(!database.isOpen()) {
            createOrOpenDatabase();
        }
        database.execSQL("delete from TranslationCache where 1 = 1");
        maps.clear();
    }

    public class Entry {
        private long id;
        private String from;
        private String to;
        private String query;
        private String result;

        private Entry(long id, String from, String to, String query, String result) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.query = query;
            this.result = result;
        }

        public long getId() {
            return id;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getQuery() {
            return query;
        }

        public String getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (id != entry.id) return false;
            if (!from.equals(entry.from)) return false;
            if (!to.equals(entry.to)) return false;
            if (!query.equals(entry.query)) return false;
            return result != null ? result.equals(entry.result) : entry.result == null;
        }

        @Override
        public int hashCode() {
            int result1 = (int) (id ^ (id >>> 32));
            result1 = 31 * result1 + from.hashCode();
            result1 = 31 * result1 + to.hashCode();
            result1 = 31 * result1 + query.hashCode();
            result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
            return result1;
        }
    }
}

