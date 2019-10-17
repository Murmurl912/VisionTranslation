package com.example.visiontranslation.ui.camera;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextDetector;
import com.example.visiontranslation.vision.text.VisionTextProcessor;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Flash;

import org.opencv.android.OpenCVLoader;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    public final String TAG = "CameraFragment";
    public final int REQUEST_CODE_PICK_IMAGE = 1;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA","android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private CameraView cameraView;
    private ImageButton pauseButton;
    private ImageButton flashButton;
    private ImageView waterMark;

    private CameraManager manager;

    private VisionTextProcessor textProcessor;
    private VisionTextProcessorBridge textProcessorBridge;
    private VisionTextResultProcessor resultProcessor;

    public CameraFragment() {
        // Required empty public constructor
        Log.d(TAG, "CameraFragment() called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        pauseButton = view.findViewById(R.id.main_camera_pause_button);
        flashButton = view.findViewById(R.id.main_camera_flash);

        cameraView = view.findViewById(R.id.main_camera_view);
        waterMark = view.findViewById(R.id.main_camera_view_water_mark);
        manager = new CameraManager(cameraView);
        pauseButton.setOnClickListener(v->{
            onPauseButtonClicked();
        });
        flashButton.setOnClickListener(v->{
            onFlashButtonClicked();
        });
        addFrameProcessor();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestoryView() called");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (isAllPermissionGranted()) {
                startCamera();
            } else {
                Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart Called");
        if(isAllPermissionGranted()) {
            startCamera();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause Called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume Called");
        manager.openCamera(true);
        manager.addFrameProcessor(textProcessorBridge);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop Called");
        manager.openCamera(false);
        manager.removeFrameProcessor(textProcessorBridge);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy Called");
    }

    private void startCamera() {
        cameraView.setPlaySounds(false);

    }

    private void requestPermission() {

        this.requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    private boolean isAllPermissionGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void onFlashButtonClicked() {
        if(cameraView != null) {
            if(cameraView.getFlash() == Flash.ON || cameraView.getFlash() == Flash.TORCH) {
                cameraView.setFlash(Flash.OFF);
                flashButton.setImageResource(R.drawable.ic_flash_off_dark);
            } else {
                cameraView.setFlash(Flash.TORCH);
                flashButton.setImageResource(R.drawable.ic_flash_on_dark);
            }
        }
    }

    private void onPauseButtonClicked() {
        if(cameraView != null) {
            if(manager.isCameraPreviewStateChanging()) {
                return;
            }

            if(manager.isCameraPreviewFrozen()) {
                pauseButton.setImageResource(R.drawable.ic_pase_circle_dark);
                manager.freezeCameraPreview(false);
            } else {
                pauseButton.setImageResource(R.drawable.ic_resume_dark);
                manager.freezeCameraPreview(true);
            }
        }
    }

    private void addFrameProcessor() {
        textProcessor = new VisionTextProcessor(getContext());
        textProcessorBridge = new VisionTextProcessorBridge(textProcessor, new SizeF(cameraView.getWidth(), cameraView.getHeight()));
        resultProcessor = new VisionTextResultProcessor(cameraView);
        textProcessor.setVisionResultProcessor(resultProcessor);
    }

}
