package com.example.visiontranslation.ui.text;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.visiontranslation.R;
import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.animation.FloatingActionButtonAnimation;
import com.example.visiontranslation.database.DatabaseManager;
import com.example.visiontranslation.database.TextTranslationHistory;
import com.example.visiontranslation.helper.Helper;
import com.example.visiontranslation.translation.BaiduTranslationService;
import com.example.visiontranslation.ui.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;
import java.util.Objects;

import static com.example.visiontranslation.translation.BaiduTranslationService.STATUS_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class TextFragment extends Fragment {


    private TextToSpeech tts;
    private boolean isTTSReady = false;

    public final String TAG = "TextFragment";
    private FloatingActionButton home;
    private FloatingActionButton gallary;
    private FloatingActionButton voice;
    private FloatingActionButton lens;
    private boolean isRotated = false;

    private EditText source;
    private TextView target;
    private ImageButton speakSourceButton;
    private ImageButton speakTargetButton;
    private ImageButton translateButton;

    private TextTranslationHistoryAdapter adapter;
    private RecyclerView recyclerView;

    public TextFragment() {
        // Required empty public constructor
        Log.d(TAG, "TextFragment() called");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");

        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");

        source  = view.findViewById(R.id.main_text_source_edit_text);
        target = view.findViewById(R.id.main_text_target_text);

        speakSourceButton = view.findViewById(R.id.main_text_speak_source_button);
        speakSourceButton.setOnClickListener(v->{
           speak(
                   source.getText().toString(),
                   Helper.getLocaleByLanguage(((MainActivity)getActivity()).getSourceLanguage())
                   );
        });

        speakTargetButton = view.findViewById(R.id.main_text_speak_target_button);
        speakTargetButton.setOnClickListener(v->{
            speak(
                    target.getText().toString(),
                    Helper.getLocaleByLanguage(((MainActivity)getActivity()).getTargetLanguage())
            );
        });

        translateButton = view.findViewById(R.id.main_text_translate_button);
        translateButton.setOnClickListener(v->{
            Helper.hideSoftKeyboard(getActivity());
            String text = source.getText().toString();
            if(text.equals("")) {
                Toast.makeText(getContext(), "Input is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            BaiduTranslationService.getBaiduTranslationService().request(
                    BaiduTranslationService.getCode(((MainActivity) getActivity()).getSourceLanguage())
                    ,
                    BaiduTranslationService.getCode(((MainActivity) getActivity()).getTargetLanguage())
                    ,
                    text,
                    new BaiduTranslationService.Response() {
                        @Override
                        public void response(String s, int status) {
                            if(status == STATUS_OK) {
                                target.post(()->target.setText(s));
                                recordHistoryToDatabase(text, s);
                                view.post(()->{
                                    Toast.makeText(getContext(), "Translation Services Success", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                view.post(()->{
                                    Toast.makeText(getContext(), "Translation Services Unavailable", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }
            );
            Helper.hideSoftKeyboard(getActivity());
            source.clearFocus();
        });

        view.findViewById(R.id.main_text_clear_source_content_button).setOnClickListener(v->{
            Helper.hideSoftKeyboard(getActivity());
            source.setText("");
            target.setText("");
            source.clearFocus();

        });

        recyclerView = view.findViewById(R.id.main_text_recycler_view);
        adapter = new TextTranslationHistoryAdapter(getContext(), getActivity());
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.main_text_copy_translation).setOnClickListener(v->{
            ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("copy", target.getText());
            Objects.requireNonNull(cm).setPrimaryClip(mClipData);
            Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
        });

        initializeActionButton(view);

        initial(view);
        loadHistory();
    }

    private void loadHistory() {
        new Thread(()->{
            DatabaseManager.getTextTranslationHistory()
                    .requestLoadHistoryRecord(null, (status, data)->{
                        adapter.setHistories(data);
                    });
        }).start();
    }


    private void recordHistoryToDatabase(String source, String target) {
        if(true) {
            return;
        }

        TextTranslationHistory textTranslationHistory = DatabaseManager.getTextTranslationHistory();
        TextTranslationHistory.TranslationHistory history =
                new TextTranslationHistory.TranslationHistory(
                        System.currentTimeMillis(),
                        false,
                        source,
                        target,
                        ((MainActivity)getActivity()).getSourceLanguage(),
                        ((MainActivity)getActivity()).getTargetLanguage()
                );
        if(textTranslationHistory.contain(history)) {
            textTranslationHistory.update(history);
            int index = adapter.find(history);
            adapter.remove(index);
        } else {
            textTranslationHistory.add(history);
        }
        adapter.add(0, history);
    }

    private void initial(View view) {
        Bundle bundle = getArguments();
        if(bundle != null) {
            String value = bundle.getString("REQUEST TRANSLATION");
            if(value != null) {
                source.setText(value);
                view.findViewById(R.id.main_text_translate_button).performClick();
            }
        }
    }

    private void speak(@NonNull String text, Locale locale) {

        if(tts == null) {
            tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public synchronized void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS) {
                        isTTSReady = true;
                        tts.setLanguage(locale);
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                        Toast.makeText(getContext(), "Speaking", Toast.LENGTH_SHORT).show();
                    } else {
                        isTTSReady = false;
                        Toast.makeText(getContext(), "Text To Speech Failed to Initialize", Toast.LENGTH_SHORT).show();
                        tts = null;
                    }
                }
            });
        } else {
            if(isTTSReady) {
                tts.setLanguage(locale);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                Toast.makeText(getContext(), "Speaking", Toast.LENGTH_SHORT).show();
            } else {
                tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public synchronized void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS) {
                            isTTSReady = true;
                            tts.setLanguage(locale);
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                            Toast.makeText(getContext(), "Speaking", Toast.LENGTH_SHORT).show();
                        } else {
                            isTTSReady = false;
                            Toast.makeText(getContext(), "Text To Speech Failed to Initialize", Toast.LENGTH_SHORT).show();
                            tts = null;
                        }
                    }
                });
            }
        }
    }

    private void initializeActionButton(View view) {
        home = view.findViewById(R.id.main_home);
        gallary = view.findViewById(R.id.main_gallary);
        voice = view.findViewById(R.id.main_voice);
        lens = view.findViewById(R.id.main_lens);
        FloatingActionButtonAnimation.init(gallary);
        FloatingActionButtonAnimation.init(voice);
        FloatingActionButtonAnimation.init(lens);

        home.setOnClickListener(v->{
            if(!isRotated) {
                FloatingActionButtonAnimation.rotateFab(home, true);
                FloatingActionButtonAnimation.showIn(gallary);
                FloatingActionButtonAnimation.showIn(voice);
                FloatingActionButtonAnimation.showIn(lens);
            } else {
                FloatingActionButtonAnimation.rotateFab(home, false);
                FloatingActionButtonAnimation.showOut(gallary);
                FloatingActionButtonAnimation.showOut(voice);
                FloatingActionButtonAnimation.showOut(lens);
            }
            isRotated = !isRotated;
        });

        lens.setOnClickListener(v->{
            home.performClick();
            Navigation.findNavController(lens).navigate(R.id.action_textFragment_to_cameraFragment);
        });

        gallary.setOnClickListener(v->{
            home.performClick();
            Navigation.findNavController(gallary).navigate(R.id.action_textFragment_to_imageFragment);

        });

        voice.setOnClickListener(v->{
            home.performClick();
            Navigation.findNavController(voice).navigate(R.id.action_textFragment_to_voiceFragment);

        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestoryView() called");
    }

}
