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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextDetector;
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

    private FragmentCameraManager manager;
    private CameraView cameraView;
    private ImageButton imageChoose;
    private ImageButton pauseButton;
    private ImageButton flashButton;

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
        imageChoose = view.findViewById(R.id.main_camera_image_choose);
        pauseButton = view.findViewById(R.id.main_camera_pause_button);
        flashButton = view.findViewById(R.id.main_camera_flash);

        cameraView = view.findViewById(R.id.main_camera_view);


        imageChoose.setOnClickListener(v->{
            startChooseImageActivity();
        });
        pauseButton.setOnClickListener(v->{
            onPauseButtonClicked();
        });
        flashButton.setOnClickListener(v->{
            onFlashButtonClicked();
        });
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
        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE: {
                if(resultCode == Activity.RESULT_OK) {
                    onPickImageSuccess(data);
                } else {
                    onPickImageFailed(data);
                }
            } break;
            default: {

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isAllPermissionGranted()) {
            startCamera();
        } else {
            requestPermission();
        }
    }

    private void startCamera() {
        cameraView.setLifecycleOwner(this);
        cameraView.setPlaySounds(false);
        manager = new FragmentCameraManager(cameraView);
        manager.setDetector(new VisionTextDetector());
        manager.startDetector();

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

    public void startChooseImageActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void onPickImageSuccess(Intent data) {

    }

    private void onPickImageFailed(Intent data) {

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
            if(manager.isFrozen()) {
                pauseButton.setImageResource(R.drawable.ic_pase_circle_dark);
                manager.resumeFrame();
            } else {
                pauseButton.setImageResource(R.drawable.ic_resume_dark);
                manager.pauseFrame();
            }
        }
    }

}
