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

import android.os.Bundle;
import android.util.Log;

import com.example.visiontranslation.R;
import com.example.visiontranslation.ui.camera.CameraFragment;
import com.example.visiontranslation.ui.text.TextFragment;
import com.example.visiontranslation.ui.voice.VoiceFragment;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";

    private boolean isMainHomeFabRotating = false;
    private DrawerLayout mainDrawerLayout;
    private Toolbar mainToolbar;

    private PagerAdapter adapter;

    private static MainActivity activity;
    private static boolean isAlive = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        setContentView(R.layout.activity_main);

        initializeViewPager();

        activity = this;
        isAlive = true;
    }

    private void initializeViewPager() {
        ViewPager viewPager = findViewById(R.id.main_view_pager);
        PagerAdapter pagerAdapter = new MainFragmentAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(pagerAdapter);
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
        isAlive = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }


    public static MainActivity getActivity() {
        return activity;
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

