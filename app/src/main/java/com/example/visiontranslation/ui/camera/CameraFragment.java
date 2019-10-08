package com.example.visiontranslation.ui.camera;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextDetector;
import com.example.visiontranslation.detector.text.VisionTextRecognizer;
import com.example.visiontranslation.overlay.BoundingDrawable;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.overlay.TextBlockDrawable;
import com.example.visiontranslation.tracker.GenericTracker;
import com.example.visiontranslation.ui.MainActivity;
import com.google.android.gms.vision.text.TextBlock;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect2d;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    public final String TAG = "CameraFragment";

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA","android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private FragmentCameraManager manager;
    private CameraView cameraView;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

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
        cameraView = view.findViewById(R.id.main_camera_view);
        if(isAllPermissionGranted()) {
            startCamera();
        } else {
            requestPermission();
        }

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


}
