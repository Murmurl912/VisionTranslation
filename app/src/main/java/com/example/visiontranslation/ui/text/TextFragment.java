package com.example.visiontranslation.ui.text;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassifier;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.visiontranslation.R;
import com.example.visiontranslation.animation.FloatingActionButtonAnimation;
import com.example.visiontranslation.helper.Helper;
import com.example.visiontranslation.translation.BaiduTranslationService;
import com.example.visiontranslation.ui.MainActivity;
import com.google.android.gms.vision.L;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

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
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory() called");

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach() called");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach() called");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() called");

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

        EditText source  = view.findViewById(R.id.main_text_source_edit_text);
        TextView target = view.findViewById(R.id.main_text_target_text);

        view.findViewById(R.id.main_text_speak_source_button).setOnClickListener(v->{
           speak(
                   source.getText().toString(),
                   Helper.getLocaleByLanguage(((MainActivity)getActivity()).getSourceLanguage())
                   );
        });

        view.findViewById(R.id.main_text_speak_target_button).setOnClickListener(v->{
            speak(
                    target.getText().toString(),
                    Helper.getLocaleByLanguage(((MainActivity)getActivity()).getTargetLanguage())
            );
        });

        view.findViewById(R.id.main_text_translate_button).setOnClickListener(v->{
            Helper.hideSoftKeyboard(getActivity());
            String text = source.getText().toString();
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
                                target.setText(s);
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

        initializeActionButton(view);

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
            Navigation.findNavController(gallary).navigate(R.id.action_textFragment_to_cameraFragment);

        });

        voice.setOnClickListener(v->{
            home.performClick();
            Navigation.findNavController(voice).navigate(R.id.action_textFragment_to_voiceFragment);

        });
    }


    private void clearText() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestoryView() called");
    }

}
