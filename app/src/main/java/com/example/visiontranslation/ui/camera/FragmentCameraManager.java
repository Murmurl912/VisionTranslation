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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextDetector;
import com.example.visiontranslation.helper.Helper;
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

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect2d;

import java.util.ArrayList;
import java.util.List;

public class FragmentCameraManager
        extends CameraListener
        implements
            View.OnTouchListener,
            Detector.Processor<TextBlock>,
            ViewTreeObserver.OnGlobalLayoutListener,
            GenericTracker.TrackingResultCallback {

    public final String TAG = "FragmentCameraManager";

    private CameraView cameraView;
    private GraphicsOverlay overlay;

    private VisionTextDetector detector;

    private Drawable pausedFrameOverlay;
    private List<Drawable> lineOverlay;
    private List<Line> lines;

    private Drawable trackerDrawable;
    private GenericTracker tracker;

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
        lineOverlay = new ArrayList<>();

    }

    public void startDetector() {
        overlay.remove(lineOverlay);
        lineOverlay.clear();
        detector.setPreviewSize(new Size(cameraView.getWidth(), cameraView.getHeight()));
        detector.fireup();
    }

    public void closeDetector() {
        if(detector != null) {
            overlay.remove(lineOverlay);
            lineOverlay.clear();
            detector.shutdown();
        }
    }

    private void setTracker() {
        tracker = new GenericTracker();
        tracker.setTrackingResultCallback(this);
    }

    private void startTracker() {

    }

    private void closeTracker() {

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
    public void onTrackingResult(boolean isSuccess, org.opencv.core.Size frameSize, Rect2d relativeCoordinate) {

    }

    @Override
    public void onGlobalLayout() {
        Helper.loadOpenCV(new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {
                if(status == LoaderCallbackInterface.SUCCESS) {
                    Log.d("OpenCVLoader", "OpenCV Load Success!");
                } else {
                    Log.d("OpenCVLoader", "OpenCV Load Failed!");
                }
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {

            }
        });

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
        SparseArray<TextBlock> blocks = detections.getDetectedItems();
        if(lineOverlay != null && lines != null) {
            overlay.remove(lineOverlay);
            lineOverlay.clear();
            lines.clear();
        } else {
            lineOverlay = new ArrayList<>();
            lines = new ArrayList<>();
        }

        Metadata metadata = detections.getFrameMetadata();
        Size size = new Size(metadata.getWidth(), metadata.getHeight());
        StringBuilder id = new StringBuilder();
        ArrayList<StringBuilder> res = new ArrayList<>();

        for(int i = 0; i < blocks.size(); i++) {
            TextBlock block = blocks.valueAt(i);
            id.append("\n\tid = ")
                    .append(blocks.keyAt(i))
                    .append("\n\t\tbounding = ")
                    .append(block.getBoundingBox())
                    .append("\n\t\tvalue = ")
                    .append(block.getValue())
                    .append("\n\t\tlanguage = ")
                    .append(block.getLanguage());
            if(!block.getLanguage().equals("en")) {
                continue;
            }

            StringBuilder linRes = new StringBuilder();
            for(Text text : block.getComponents()) {
                lines.add((Line)text);
                linRes.append("\n" + text.getValue());
                lineOverlay.add(new LineDrawable((Line)text, size, null));
                id.append("\n\t\tline: ")
                        .append("\n\t\t\tbounding = ")
                        .append(text.getBoundingBox())
                        .append("\n\t\t\tvalue = ")
                        .append(text.getValue())
                        .append("\n\t\t\tlanguage = ")
                        .append(((Line) text).getLanguage())
                        .append("\n\t\t\tangle = ")
                        .append(((Line) text).getAngle());
            }
            res.add(linRes);
        }

        Log.d(TAG, "Result: \n" + id.toString());
        overlay.add(lineOverlay);

        StringBuilder builder = new StringBuilder();
        for(StringBuilder r : res) {
            builder.append(r).append("\n");
        }

    }
}
