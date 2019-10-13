package com.example.visiontranslation.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.visiontranslation.R;
import com.example.visiontranslation.translation.BaiduTranslationService;
import com.example.visiontranslation.ui.camera.CameraFragment;
import com.example.visiontranslation.ui.text.TextFragment;
import com.example.visiontranslation.ui.voice.VoiceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";

    private boolean isMainHomeFabRotating = false;
    private DrawerLayout mainDrawerLayout;
    private Toolbar mainToolbar;

    private PagerAdapter adapter;
    private Spinner sourceSpinner;
    private Spinner targetSpinner;
    private List<String> languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);
        initializeViewPager();
        initializeSpinner();
    }

    private void initializeViewPager() {
        ViewPager viewPager = findViewById(R.id.main_view_pager);
        PagerAdapter pagerAdapter = new MainFragmentAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(pagerAdapter);
    }

    private void initializeSpinner() {
        sourceSpinner = findViewById(R.id.main_source_language);
        targetSpinner = findViewById(R.id.main_target_language);
        languages = new ArrayList<>();
        languages.addAll(Arrays.asList(BaiduTranslationService.getLanguages()));
        sourceSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.adapter_language, R.id.adapter_language_text, languages));
        targetSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.adapter_language, R.id.adapter_language_text, languages));
        sourceSpinner.getAdapter();

        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPreferences = getSharedPreferences("Default Language", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Default Source", position);
                editor.apply();            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        targetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    targetSpinner.setSelection(1);
                    return;
                }
                SharedPreferences sharedPreferences = getSharedPreferences("Default Language", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Default Target", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.main_swap_language).setOnClickListener(v->{
            swapLanguage();
        });

        new Thread(this::remember).start();
    }

    public String getSourceLanguage() {
        return languages.get(sourceSpinner.getSelectedItemPosition());
    }

    public String getTargetLanguage() {
        return languages.get(targetSpinner.getSelectedItemPosition());
    }

    private void swapLanguage() {
        int pos = sourceSpinner.getSelectedItemPosition();
        sourceSpinner.setSelection(targetSpinner.getSelectedItemPosition());
        targetSpinner.setSelection(pos);
    }

    private void remember() {
        SharedPreferences sharedPreferences = getSharedPreferences("Default Language", MODE_PRIVATE);
        if(sharedPreferences.contains("Default Source")) {
            int index = sharedPreferences.getInt("Default Source", 0);
            sourceSpinner.post(()->sourceSpinner.setSelection(index));
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("Default Source", targetSpinner.getSelectedItemPosition());
            editor.apply();
        }

        if(sharedPreferences.contains("Default Target")) {
            int index = sharedPreferences.getInt("Default Target", 1);
            targetSpinner.post(()->targetSpinner.setSelection(index));
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("Default Target", targetSpinner.getSelectedItemPosition());
            editor.apply();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    private class MainFragmentAdapter extends FragmentPagerAdapter {

        public TextFragment textFragment;
        public CameraFragment cameraFragment;
        public VoiceFragment voiceFragment;

        private MainFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                if(textFragment == null) {
                    textFragment = new TextFragment();
                }
                return textFragment;
            }

            if(position == 1) {
                if(cameraFragment == null) {
                    cameraFragment = new CameraFragment();
                }
                return cameraFragment;
            }

            if(voiceFragment == null) {
                voiceFragment = new VoiceFragment();
            }
            return voiceFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

}

