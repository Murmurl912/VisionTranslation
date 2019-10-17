package com.example.visiontranslation.ui.camera;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.R;
import com.example.visiontranslation.vision.VisionFrameProcessor;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraManager extends CameraListener {

    private final String TAG = "CameraManager";

    private CameraView cameraView;
    private final Map<VisionFrameProcessor<?>, FrameProcessor> frameProcessors = new HashMap<>();
    private boolean isFrozen = false;
    private boolean isStateChanging = false;
    private Bitmap frozenFrame;
    private final Object frozenFrameLock = new Object();
    private ImageView frozenView;

    public CameraManager(CameraView cameraView) {
        Log.d(TAG, "CameraManager Called: " + cameraView);

        this.cameraView = cameraView;
        cameraView.addCameraListener(this);
        frozenView = cameraView.findViewById(R.id.main_camera_view_water_mark);
    }

    public void addFrameProcessor(@NonNull VisionFrameProcessor<?> frameProcessor) {
        Log.d(TAG, "addFrameProcessor Called: " + frameProcessor);

        synchronized (frameProcessors) {
            this.frameProcessors.put(frameProcessor, new FrameProcessor() {
                @Override
                public void process(@NonNull Frame frame) {
                    frameProcessor.onFrame(
                            frameProcessor.nv21ToBitmap(
                                    frame.getData(),
                                    frame.getSize().getWidth(),
                                    frame.getSize().getHeight()),
                            frame.getRotation()
                    );
                }
            });
            cameraView.addFrameProcessor(frameProcessors.get(frameProcessor));
        }
    }

    public void removeFrameProcessor(@NonNull VisionFrameProcessor<?> frameProcessor) {
        Log.d(TAG, "removeFrameProcessor Called: " + frameProcessor);

        synchronized (frameProcessors) {
            cameraView.removeFrameProcessor(
                    this.frameProcessors.remove(frameProcessor)
            );
        }
    }

    public void clearFrameProcessor() {
        Log.d(TAG, "clearFrameProcessor Called");

        synchronized (frameProcessors) {
            cameraView.clearFrameProcessors();
            frameProcessors.clear();
        }
    }

    public void freezeCameraPreview(boolean freeze) {
        Log.d(TAG, "freezeCameraPreview Called: " + freeze);

        if(isStateChanging) {
            Log.d(TAG, "Camera State Changing Cancel Operation");
            return;
        }

        if(freeze) {
            freeze();
        } else {
            unfreeze();
        }
    }

    private void freeze() {
        Log.d(TAG, "freeze Called");
        if(isFrozen) {
            Log.d(TAG, "Camera Preview is Frozen Cancel Operation");
            return;
        }

        isStateChanging = true;
        Log.d(TAG, "Invoke cameraView.takePictureSnapShot()");
        cameraView.takePictureSnapshot();
    }

    private void unfreeze() {
        Log.d(TAG, "unfreeze Called");
        if(!isFrozen) {
            Log.d(TAG, "Camera Preview is Not Frozen Cancel Operation");
            return;
        }
        isStateChanging = true;
        cameraView.post(()->cameraView.open());
        frozenView.post(()->frozenView.setVisibility(View.GONE));
        isFrozen = false;
        isStateChanging = false;
    }

    private void onSnapShotTakenCallback(@Nullable Bitmap bitmap) {
        Log.d(TAG, "onSnapShotTakenCallback called: " + bitmap);

        if (bitmap == null) {
            Log.d(TAG, "bitmap == null Cancel Operation");
            isStateChanging = false;
            return;
        }

        setFrozenFrame(bitmap);
        cameraView.post(()->cameraView.close());
        frozenView.post(()->frozenView.setVisibility(View.VISIBLE));
        frozenView.post(()->frozenView.setImageBitmap(bitmap));

        isFrozen = true;
        isStateChanging = false;
    }

    public boolean isCameraPreviewStateChanging() {
        Log.d(TAG, "isCameraPreviewSateChangine Called");
        return isStateChanging;
    }

    public boolean isCameraPreviewFrozen() {
        Log.d(TAG, "isCameraPreviewFrozen Called");
        return isFrozen;
    }

    private void setFrozenFrame(@Nullable Bitmap bitmap) {
        Log.d(TAG, "setFrozenFrame Called: " + bitmap);
        synchronized (frozenFrameLock) {
            this.frozenFrame = bitmap;
        }
    }

    @Nullable
    public Bitmap getFrozenFrame() {
        Log.d(TAG, "getFrozenFrame Called");
        synchronized (frozenFrameLock) {
            return frozenFrame;
        }
    }

    public void openCamera(boolean open) {
        Log.d(TAG, "openCamera Called: " + open);
        if(isStateChanging) {
            Log.d(TAG, "Camera State Changing Cancel Operation");
            return;
        }

        if(open) {
            if(isFrozen) {
                return;
            }
            cameraView.open();
        } else {
            cameraView.close();
        }

    }

    public void enableFlash(boolean enable) {
        Log.d(TAG, "enableFlash Called: " + enable);

        if(enable) {
            cameraView.post(()->cameraView.setFlash(Flash.TORCH));
        } else {
            cameraView.post(()->cameraView.setFlash(Flash.OFF));
        }
    }

    @Override
    public void onCameraOpened(@NonNull CameraOptions options) {
        super.onCameraOpened(options);
        Log.d(TAG, "onCameraOpened Called: " + options);

        for(VisionFrameProcessor processor : frameProcessors.keySet()) {
            processor.enableProcessor(true);
        }
    }

    @Override
    public void onCameraClosed() {
        super.onCameraClosed();
        Log.d(TAG, "onCameraClosed Called");
        for(VisionFrameProcessor processor : frameProcessors.keySet()) {
            processor.enableProcessor(false);
        }
    }

    @Override
    public void onPictureTaken(@NonNull PictureResult result) {
        super.onPictureTaken(result);
        Log.d(TAG, "onPictureTaken Called: " + result);
        if(result.isSnapshot()) {
            result.toBitmap(new BitmapCallback() {
                @Override
                public void onBitmapReady(@Nullable Bitmap bitmap) {
                    Log.d(TAG, "onBitmapReady Called: " + bitmap);
                    onSnapShotTakenCallback(bitmap);
                }
            });
        }
    }
}
