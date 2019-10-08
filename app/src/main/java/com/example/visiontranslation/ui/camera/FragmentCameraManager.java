package com.example.visiontranslation.ui.camera;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Size;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOverlay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.detector.text.VisionTextDetector;
import com.example.visiontranslation.detector.text.VisionTextRecognizer;
import com.example.visiontranslation.overlay.BlockDrawable;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.tracker.GenericTracker;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class FragmentCameraManager
        extends CameraListener
        implements View.OnTouchListener, Detector.Processor<TextBlock> {

    private CameraView cameraView;
    private GraphicsOverlay overlay;

    private GenericTracker tracker;
    private List<Drawable> trackerResultOverlay;

    private VisionTextDetector detector;
    private List<Drawable> detectorResultOverlay;

    private List<Detector.Detections<TextBlock>> textBlockBuffer;

    public FragmentCameraManager(CameraView cameraView) {
        this.cameraView = cameraView;
        overlay = new GraphicsOverlay(cameraView);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setUpCamera() {
        cameraView.setOnTouchListener(this);
        cameraView.addCameraListener(this);
    }


    public void setTracker(@Nullable GenericTracker tracker) {
        this.tracker = tracker;
        trackerResultOverlay = new ArrayList<>();
        cameraView.addFrameProcessor(tracker);
    }

    public void setDetector(@Nullable VisionTextDetector detector) {
        this.detector = detector;
        if(detector !=  null) {
            detector.setProcessor(this);
        }
        detectorResultOverlay = new ArrayList<>();
        cameraView.addFrameProcessor(detector);
        textBlockBuffer = new ArrayList<>();
    }

    public void startDetector() {
        if(detector != null) {
            detector.fireup();
        }
    }

    public void closeDetector() {
        if(detector != null) {
            detector.shutdown();
        }
    }

    public void startTracker() {
        if(tracker != null) {
            tracker.fireup();
        }
    }

    public void closeTracker() {
        if(tracker != null) {
            tracker.shutdown();
        }
    }

    public void destroy() {
        if(detector != null) {
            detector.destroy();
        }

        if(tracker != null) {
            tracker.destroy();
        }
    }


    /**
     * on touch listener
     * @param v
     * @param event
     * @return
     */
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    /**
     * Detector.Processor<TextBlock>
     *     |
     *     |
     *     V
     */
    @Override
    public void release() {

        if(trackerResultOverlay != null) {
            overlay.remove(trackerResultOverlay);
            trackerResultOverlay.clear();
        }

        if(detectorResultOverlay != null) {
            overlay.remove(detectorResultOverlay);
            detectorResultOverlay.clear();
            textBlockBuffer.clear();
        }

    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        if(detections == null) {
            return;
        }

        SparseArray<TextBlock> textBlockSparseArray = detections.getDetectedItems();
        int height = detections.getFrameMetadata().getHeight();
        int width = detections.getFrameMetadata().getWidth();
        overlay.remove(detectorResultOverlay);
        detectorResultOverlay.clear();
        for(int i = 0; i < textBlockSparseArray.size(); i++) {
            TextBlock block = textBlockSparseArray.valueAt(i);
            Drawable drawable = new BlockDrawable(block, new Size(width, height));
            detectorResultOverlay.add(drawable);
            overlay.add(drawable);
        }

    }


    @Override
    public void onCameraOpened(@NonNull CameraOptions options) {
        super.onCameraOpened(options);
    }

    @Override
    public void onPictureTaken(@NonNull PictureResult result) {
        super.onPictureTaken(result);
    }

    @Override
    public void onAutoFocusStart(@NonNull PointF point) {
        super.onAutoFocusStart(point);
    }

    @Override
    public void onAutoFocusEnd(boolean successful, @NonNull PointF point) {
        super.onAutoFocusEnd(successful, point);
    }


}
