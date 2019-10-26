package com.example.visiontranslation.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.visiontranslation.R;
import com.example.visiontranslation.helper.Helper;
import com.example.visiontranslation.translation.BaiduTranslationService;
import com.example.visiontranslation.ui.offline.OfflineTranslationActivity;
import com.example.visiontranslation.ui.star.StarActivity;
import com.google.android.material.navigation.NavigationView;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public final String TAG = "MainActivity";

    private DrawerLayout mainDrawerLayout;

    private Spinner sourceSpinner;
    private Spinner targetSpinner;
    private List<String> languages;

    private Fragment mainNavFragment;
    private Set<OnLanguageChangeListener> languageChangeListenerSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);
        initialStatusBar();
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5d7c91ec");
        initializeSpinner();
        initialFragment();
        initializeToolbar();
        initialNavigationDrawer();
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_dark);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initialNavigationDrawer() {
        mainDrawerLayout = findViewById(R.id.main_drawer_layout);
        NavigationView navigationView = findViewById(R.id.main_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initialStatusBar() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void initialFragment() {
        mainNavFragment = getSupportFragmentManager().findFragmentById(R.id.main_nav_fragment);
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
                editor.apply();
                notifyLanguageChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        targetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPreferences = getSharedPreferences("Default Language", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Default Target", position);
                editor.apply();
                notifyLanguageChange();
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

    private void aboutDialog() {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.main_menu_home: {
                mainDrawerLayout.closeDrawer(GravityCompat.START);
                this.onBackPressed();
            } break;

            case R.id.main_menu_fav: {
                mainDrawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, StarActivity.class);
                startActivity(intent);
            } break;

            case R.id.main_menu_offline: {
                mainDrawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, OfflineTranslationActivity.class);
                startActivity(intent);
            } break;

            case R.id.main_menu_setting: {
                mainDrawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, SettingActivity.class));
            } break;

            case R.id.main_menu_about: {
                mainDrawerLayout.closeDrawer(GravityCompat.START);

            } break;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            mainDrawerLayout.openDrawer(GravityCompat.START);
            Helper.hideSoftKeyboard(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if(mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawer(GravityCompat.START);
            Helper.hideSoftKeyboard(this);
            return;
        }
        super.onBackPressed();

    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavHostFragment.findNavController(mainNavFragment).navigateUp();
    }

    private void notifyLanguageChange() {
        if(languageChangeListenerSet == null) {
            return;
        }

        for(OnLanguageChangeListener languageChangeListener : languageChangeListenerSet) {
            if(languageChangeListener != null) {
                languageChangeListener.onLanguageChange(getSourceLanguage(), getTargetLanguage());
            }
        }
    }

    public void addOnLanguageChangeListener(OnLanguageChangeListener languageChangeListener) {
        if(languageChangeListenerSet == null) {
            languageChangeListenerSet = new HashSet<>();
        }
        languageChangeListenerSet.add(languageChangeListener);
    }

    public void removeOnLanguageChangeListener(OnLanguageChangeListener languageChangeListener) {
        if(languageChangeListenerSet != null) {
            languageChangeListenerSet.add(languageChangeListener);
        }
    }

    public interface OnLanguageChangeListener {
        public void onLanguageChange(String source, String target);
    }
}

