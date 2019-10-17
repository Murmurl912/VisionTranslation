package com.example.visiontranslation.ui.camera;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.util.SizeF;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.R;
import com.example.visiontranslation.vision.FrameProcessorBridge;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Flash;

import java.util.HashSet;
import java.util.Set;

//ToDo: implement auto adapt process frame size mechanism
public class CameraManager extends CameraListener
    implements View.OnLayoutChangeListener {
    private final String TAG = "CameraManager";

    private CameraView cameraView;
    private boolean isFrozen = false;
    private boolean isStateChanging = false;
    private Bitmap frozenFrame;
    private final Object frozenFrameLock = new Object();
    private ImageView frozenView;
    private final Set<FrameProcessorBridge> frameProcessorBridges = new HashSet<>();

    public CameraManager(CameraView cameraView) {
        Log.d(TAG, "CameraManager Called: " + cameraView);
        this.cameraView = cameraView;
        cameraView.addCameraListener(this);
        cameraView.addOnLayoutChangeListener(this);
        frozenView = cameraView.findViewById(R.id.main_camera_view_water_mark);
    }

    public void addFrameProcessor(@NonNull FrameProcessorBridge frameProcessorBridge) {
        Log.d(TAG, "addFrameProcessor Called: " + frameProcessorBridge);

        synchronized (frameProcessorBridges) {
            frameProcessorBridges.add(frameProcessorBridge);
            cameraView.addFrameProcessor(frameProcessorBridge);
        }
    }

    public void removeFrameProcessor(@NonNull FrameProcessorBridge frameProcessorBridge) {
        Log.d(TAG, "removeFrameProcessor Called: " + frameProcessorBridge);

        synchronized (frameProcessorBridges) {
            frameProcessorBridges.remove(frameProcessorBridge);
            cameraView.removeFrameProcessor(frameProcessorBridge);
        }
    }

    public void clearFrameProcessor() {
        Log.d(TAG, "clearFrameProcessor Called");

        synchronized (frameProcessorBridges) {
            cameraView.clearFrameProcessors();
            frameProcessorBridges.clear();
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

    private void onPreviewAspcetRatioChange(SizeF previewAspectRatio) {
        for(FrameProcessorBridge bridge : frameProcessorBridges) {
            bridge.setAspectRatio(previewAspectRatio);
        }
    }

    @Override
    public void onCameraOpened(@NonNull CameraOptions options) {
        super.onCameraOpened(options);
        Log.d(TAG, "onCameraOpened Called: " + options);

        for(FrameProcessorBridge bridge : frameProcessorBridges) {
            bridge.getProcessor().enableProcessor(true);
        }
    }

    @Override
    public void onCameraClosed() {
        super.onCameraClosed();
        Log.d(TAG, "onCameraClosed Called");
        for(FrameProcessorBridge bridge : frameProcessorBridges) {
            bridge.getProcessor().enableProcessor(false);
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

    @Override
    public void onLayoutChange(View v,
                               int left,
                               int top,
                               int right,
                               int bottom,
                               int oldLeft,
                               int oldTop,
                               int oldRight,
                               int oldBottom) {
        Rect rect = new Rect(left, top, right, bottom);
        Rect old = new Rect(oldLeft, oldTop, oldRight, oldBottom);
        if(rect.height() != old.height() || rect.width() != old.width()) {
            SizeF previewAspectRatio = new SizeF(rect.width(), rect.height());
            onPreviewAspcetRatioChange(previewAspectRatio);
        }

    }
}
