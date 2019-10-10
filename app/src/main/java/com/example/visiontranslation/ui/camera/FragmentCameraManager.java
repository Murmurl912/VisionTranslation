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
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextDetector;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.overlay.LineDrawable;
import com.example.visiontranslation.overlay.TextBlockDrawable;
import com.example.visiontranslation.tracker.GenericTracker;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.Frame.Metadata;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;

import java.util.ArrayList;
import java.util.List;

public class FragmentCameraManager
        extends CameraListener
        implements View.OnTouchListener, Detector.Processor<TextBlock>, ViewTreeObserver.OnGlobalLayoutListener {

    public final String TAG = "FragmentCameraManager";

    private CameraView cameraView;
    private GraphicsOverlay overlay;

    private VisionTextDetector detector;

    private Drawable pausedFrameOverlay;
    private List<Drawable> lineOverlay;
    private List<Line> lines;

    private boolean isFrozen;
    private ImageView waterMark;
    private TextView display;

    public FragmentCameraManager(CameraView cameraView, TextView display) {
        this.cameraView = cameraView;
        overlay = new GraphicsOverlay(cameraView);
        isFrozen = false;
        waterMark = cameraView.findViewById(R.id.main_camera_view_water_mark);
        this.display = display;
        setUpCamera();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpCamera() {
        cameraView.setOnTouchListener(this);
        cameraView.addCameraListener(this);
        cameraView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    public Size getPreviewSize() {
        return new Size(cameraView.getWidth(), cameraView.getHeight());
    }

    public void setDetector(@NonNull VisionTextDetector detector) {
        if(this.detector != null) {
            closeDetector();
            this.detector.destroy();
        }
        cameraView.clearFrameProcessors();
        this.detector = detector;
        this.detector.setProcessor(this);
        cameraView.addFrameProcessor(detector);
    }

    public void startDetector() {
        detector.setPreviewSize(new Size(cameraView.getWidth(), cameraView.getHeight()));
        detector.fireup();
    }

    public void closeDetector() {
        if(detector != null) {
            detector.shutdown();
        }
    }


    public void destroy() {
        if(detector != null) {
            detector.destroy();
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
        startDetector();
    }

    private void onPauseFrame(Bitmap frame) {
        if(frame == null) {
            Toast.makeText(cameraView.getContext(), "Freezing Frame Failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        waterMark.setImageBitmap(frame);
        waterMark.setVisibility(View.VISIBLE);
        closeDetector();
        cameraView.close();
        isFrozen = true;
        processPausedFrame(frame);
    }

    private void processPausedFrame(@NonNull Bitmap frame) {

    }

    @Override
    public void onGlobalLayout() {
        if(detector != null) {
            startDetector();
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        return false;
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


    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        display.setText("");
        SparseArray<TextBlock> blocks = detections.getDetectedItems();
        if(lineOverlay != null && lines != null) {
            overlay.remove(lineOverlay);
            lines.clear();
        } else {
            lineOverlay = new ArrayList<>();
            lines = new ArrayList<>();
        }

        Metadata metadata = detections.getFrameMetadata();
        Size size = new Size(metadata.getWidth(), metadata.getHeight());
        for(int i = 0; i < blocks.size(); i++) {
            TextBlock block = blocks.valueAt(i);
            for(Text text : block.getComponents()) {
                lines.add((Line)text);
                lineOverlay.add(new LineDrawable((Line)text, size));
            }
        }

        overlay.add(lineOverlay);



    }
}
