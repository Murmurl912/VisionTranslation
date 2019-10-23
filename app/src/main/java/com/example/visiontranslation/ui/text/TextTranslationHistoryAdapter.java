package com.example.visiontranslation.ui.text;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.example.visiontranslation.R;
import com.example.visiontranslation.database.DatabaseManager;
import com.example.visiontranslation.database.TextTranslationHistory;
import com.example.visiontranslation.helper.Helper;

public class TextTranslationHistoryAdapter extends RecyclerView.Adapter<TextTranslationHistoryAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageButton speakSource;
        private ImageButton speakTarget;

        private ImageButton star;
        private ImageButton copy;
        private ImageButton more;
        private TextView source;
        private TextView target;
        private PopupMenu menu;

        public ViewHolder(View view) {
            super(view);
            speakSource = view.findViewById(R.id.main_text_history_speak_source_content_button);
            speakTarget = view.findViewById(R.id.main_text_history_speak_target_content_button);
            star = view.findViewById(R.id.main_text_history_fav_button);
            copy = view.findViewById(R.id.main_text_history_copy_translation);
            more = view.findViewById(R.id.main_text_history_more);
            source = view.findViewById(R.id.main_text_history_source_text);
            target = view.findViewById(R.id.main_text_history_target_text);
            menu = new PopupMenu(view.getContext(), more);
            menu.getMenuInflater().inflate(R.menu.pupup_menu, menu.getMenu());
        }
    }

    private Context context;
    private List<TextTranslationHistory.TranslationHistory> histories;
    private Activity activity;

    private TextToSpeech tts;
    private boolean isTTSReady = false;

    public TextTranslationHistoryAdapter(Context context, Activity activity) {
        this.context = context;
        histories = new ArrayList<>();
        this.activity = activity;
    }

    private void speak(@NonNull String text, Locale locale) {

        if(tts == null) {
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public synchronized void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS) {
                        isTTSReady = true;
                        tts.setLanguage(locale);
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                        Toast.makeText(context, "Speaking", Toast.LENGTH_SHORT).show();
                    } else {
                        isTTSReady = false;
                        Toast.makeText(context, "Text To Speech Failed to Initialize", Toast.LENGTH_SHORT).show();
                        tts = null;
                    }
                }
            });
        } else {
            if(isTTSReady) {
                tts.setLanguage(locale);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                Toast.makeText(context, "Speaking", Toast.LENGTH_SHORT).show();
            } else {
                tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                    @Override
                    public synchronized void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS) {
                            isTTSReady = true;
                            tts.setLanguage(locale);
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                            Toast.makeText(context, "Speaking", Toast.LENGTH_SHORT).show();
                        } else {
                            isTTSReady = false;
                            Toast.makeText(context, "Text To Speech Failed to Initialize", Toast.LENGTH_SHORT).show();
                            tts = null;
                        }
                    }
                });
            }
        }
    }

    public void setHistories(List<TextTranslationHistory.TranslationHistory> histories) {
        if(histories == null) {
            return;
        }

        this.histories.clear();

        for(TextTranslationHistory.TranslationHistory h : histories) {
            this.histories.add((TextTranslationHistory.TranslationHistory)h.clone());
        }
        activity.runOnUiThread(this::notifyDataSetChanged);
        histories.sort(new Comparator<TextTranslationHistory.TranslationHistory>() {
            @Override
            public int compare(TextTranslationHistory.TranslationHistory a, TextTranslationHistory.TranslationHistory b) {
                return (int)(a.getTime() - b.getTime());
            }
        });
    }

    public synchronized void add(TextTranslationHistory.TranslationHistory history) {
        histories.add((TextTranslationHistory.TranslationHistory)history.clone());
        activity.runOnUiThread(()->{
            notifyItemInserted(histories.size() - 1);
        });
    }

    public synchronized void add(int index, TextTranslationHistory.TranslationHistory history) {
        histories.add(index, (TextTranslationHistory.TranslationHistory)history.clone());
        activity.runOnUiThread(()->{
            notifyItemInserted(index);
        });
    }

    public TextTranslationHistory.TranslationHistory remove(int index) {
        try {
            TextTranslationHistory.TranslationHistory history = histories.remove(index);
            activity.runOnUiThread(()->{
                notifyItemRemoved(index);
            });
            return history;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public synchronized void update(int index, TextTranslationHistory.TranslationHistory history) {
        if(histories.contains(history)) {
            int i = histories.indexOf(history);
            histories.remove(history);
            histories.add(i, (TextTranslationHistory.TranslationHistory)history.clone());
        }
        activity.runOnUiThread(()->{
            notifyItemChanged(index);
        });
    }

    public synchronized int find(TextTranslationHistory.TranslationHistory history) {
        if(history == null) {
            return -1;
        }
        return histories.indexOf(history);
    }

    @NonNull
    @Override
    public TextTranslationHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                     int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.adapter_translation_history, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TextTranslationHistoryAdapter.ViewHolder holder
            , int position) {
        TextTranslationHistory.TranslationHistory h = histories.get(position);

        holder.star.setOnClickListener(v->{
            if(h.isFavorite()) {
                h.setFavorite(false);
                holder.star.setImageResource(R.drawable.ic_star_border_dark);
                DatabaseManager.getTextTranslationHistory().update(h);
            } else {
                h.setFavorite(true);
                holder.star.setImageResource(R.drawable.ic_star_dark);
                DatabaseManager.getTextTranslationHistory().update(h);
            }
        });

        holder.copy.setOnClickListener(v->{
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("copy", h.getTarget());
            Objects.requireNonNull(cm).setPrimaryClip(mClipData);
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
        });

        holder.speakSource.setOnClickListener(v->{
            speak(h.getSource(), Helper.getLocaleByLanguage(h.getSource_lg()));
        });

        holder.speakTarget.setOnClickListener(v->{
            speak(h.getTarget(), Helper.getLocaleByLanguage(h.getTarget_lg()));
        });

        holder.source.post(()->{
            holder.source.setText(h.getSource());
        });

        holder.target.post(()->{
            holder.target.setText(h.getTarget());
        });

        holder.more.setOnClickListener((v)->{
            holder.menu.show();
        });

        holder.menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pupup_delete: {
                        DatabaseManager.getTextTranslationHistory().remove(h);
                        remove(find(h));
                    } break;
                };
                return true;
            }
        });

        if(h.isFavorite()) {
            holder.star.post(()->holder.star.setImageResource(R.drawable.ic_star_dark));
        } else {
            holder.star.post(()->holder.star.setImageResource(R.drawable.ic_star_border_dark));
        }

    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

}
