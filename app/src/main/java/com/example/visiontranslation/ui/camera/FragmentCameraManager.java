package com.example.visiontranslation.ui.camera;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextDetector;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.overlay.LineDrawable;
import com.example.visiontranslation.tracker.GenericTracker;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;

import java.util.ArrayList;
import java.util.List;

public class FragmentCameraManager
        extends CameraListener
        implements View.OnTouchListener, Detector.Processor<TextBlock> {

    public final String TAG = "FragmentCameraManager";

    private CameraView cameraView;
    private GraphicsOverlay overlay;

    private GenericTracker tracker;
    private List<Drawable> trackerResultOverlay;

    private VisionTextDetector detector;
    private List<Drawable> detectorResultOverlay;
    private List<Detector.Detections<TextBlock>> textBlockBuffer;

    private Drawable pausedFrameOverlay;

    private boolean isFrozen;
    private ImageView waterMark;

    public FragmentCameraManager(CameraView cameraView) {
        this.cameraView = cameraView;
        overlay = new GraphicsOverlay(cameraView);
        isFrozen = false;
        waterMark = cameraView.findViewById(R.id.main_camera_view_water_mark);

        setUpCamera();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpCamera() {
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
            overlay.remove(detectorResultOverlay);
            detectorResultOverlay.clear();
            detector.fireup();
        }
    }

    public void closeDetector() {
        if(detector != null) {
            detectorResultOverlay.clear();
            overlay.remove(detectorResultOverlay);
            detector.shutdown();
        }
    }

    public void startTracker() {
        if(tracker != null) {
            overlay.remove(trackerResultOverlay);
            trackerResultOverlay.clear();
            tracker.fireup();
        }
    }

    public void closeTracker() {
        if(tracker != null) {
            overlay.remove(trackerResultOverlay);
            trackerResultOverlay.clear();
            tracker.shutdown();
        }
    }

    public void destroy() {
        if(detector != null) {
            detectorResultOverlay.clear();
            detector.destroy();
        }

        if(tracker != null) {
            trackerResultOverlay.clear();
            tracker.destroy();
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void pauseFrame() {
        if(isFrozen) {
            return;
        }
        cameraView.takePictureSnapshot();
    }

    public void resumeFrame() {
        if(!isFrozen) {
            return;
        }
        cameraView.open();
        isFrozen = false;
        waterMark.setVisibility(View.INVISIBLE);
        startTracker();
        startDetector();
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
        long startProcessingDetectionTime = System.currentTimeMillis();
        if(detections == null) {
            return;
        }

        overlayDetections(detections);

        long endProcessingDetectionTime = System.currentTimeMillis();
        Log.d(TAG, "Processing Result Time: " + (endProcessingDetectionTime - startProcessingDetectionTime));
    }

    private void overlayDetections(@NonNull Detector.Detections<TextBlock> detections) {
        SparseArray<TextBlock> textBlockSparseArray = detections.getDetectedItems();
        overlay.remove(detectorResultOverlay);
        detectorResultOverlay.clear();

        for(int i = 0; i <textBlockSparseArray.size(); i++) {
            TextBlock block = textBlockSparseArray.valueAt(i);
            String language = block.getLanguage();
            List<? extends Text> lines = block.getComponents();
            for(Text lineT : lines) {
                Line line = (Line)lineT;
                Drawable drawable = new LineDrawable(line, new Size(detections.getFrameMetadata().getWidth(), detections.getFrameMetadata().getHeight()));
                detectorResultOverlay.add(drawable);
                overlay.add(drawable);
            }
        }

    }

    private void onPauseFrame(Bitmap frame) {
        if(frame == null) {
            Toast.makeText(cameraView.getContext(), "Freezing Frame Failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        waterMark.setImageBitmap(frame);
        waterMark.setVisibility(View.VISIBLE);
        closeDetector();
        closeTracker();
        cameraView.close();
        isFrozen = true;
        processPausedFrame(frame);
    }

    private void processPausedFrame(@NonNull Bitmap frame) {

    }

    @Override
    public void onCameraOpened(@NonNull CameraOptions options) {
        super.onCameraOpened(options);
    }

    @Override
    public void onPictureTaken(@NonNull PictureResult result) {
        result.toBitmap(new BitmapCallback() {
            @Override
            public void onBitmapReady(@Nullable Bitmap bitmap) {
                onPauseFrame(bitmap);
            }
        });
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
