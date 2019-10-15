package com.example.visiontranslation.ui.camera;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextDetector;
import com.example.visiontranslation.helper.Helper;
import com.example.visiontranslation.overlay.ElementDrawable;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.overlay.LineDrawable;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Element;
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

import java.util.ArrayList;
import java.util.List;

public class FragmentCameraManager
        extends CameraListener
        implements
            View.OnTouchListener,
            Detector.Processor<TextBlock>,
            ViewTreeObserver.OnGlobalLayoutListener {

    public final String TAG = "FragmentCameraManager";

    private CameraView cameraView;
    private GraphicsOverlay overlay;

    private VisionTextDetector detector;
    private List<Drawable> lineOverlay;
    private List<Line> lines;

    private List<Drawable> elementDrawable;
    private List<Element> elements;
    private GraphicsOverlay pausedOverlay;

    private boolean isFrozen;
    private boolean isChanging;

    private ImageView waterMark;

    public FragmentCameraManager(CameraView cameraView) {
        this.cameraView = cameraView;
        overlay = new GraphicsOverlay(cameraView);
        isFrozen = false;
        isChanging = false;
        waterMark = ((View)cameraView.getParent()).findViewById(R.id.main_camera_view_water_mark);
        pausedOverlay = new GraphicsOverlay(waterMark);
        waterMark.setOnTouchListener(this);
        this.cameraView.addCameraListener(this);
        this.cameraView.setOnTouchListener(this);
        this.cameraView.getViewTreeObserver().addOnGlobalLayoutListener(this);
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
        if(isChanging || isFrozen) {
            return;
        }
        overlay.remove(lineOverlay);
        lineOverlay.clear();
        detector.setProcessingSize(new Size(cameraView.getWidth(), cameraView.getHeight()));
        detector.fireup();
    }

    public void closeDetector() {
        if(detector != null) {
            overlay.remove(lineOverlay);
            lineOverlay.clear();
            detector.shutdown();
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public boolean isChanging() {
        return isChanging;
    }
    public void pauseFrame() {
      if(isChanging) {
          return;
      }
      if(isFrozen) {
          return;
      }
      isChanging = true;
      pause();
    }

    public void resumeFrame() {
        if(isChanging) {
            return;
        }
        if(!isFrozen) {
            return;
        }
        isChanging = true;
        resume();
    }

    private void pause() {
        closeDetector();
        cameraView.close();
        Bitmap bitmap = detector.getFrame();
        waterMark.setImageBitmap(bitmap);
        waterMark.setVisibility(View.VISIBLE);
        processPausedFrame(bitmap);
        isFrozen = true;
        isChanging = false;
    }

    private void resume() {
        closeDetector();
        cameraView.open();
        waterMark.setVisibility(View.GONE);
        startDetector();
        isFrozen = false;
        isChanging = false;
    }

    synchronized private void onPauseFrame(Bitmap frame, int rotation) {
        if(frame == null) {
            Toast.makeText(cameraView.getContext(), "Freezing Frame Failed!", Toast.LENGTH_SHORT).show();
            isChanging = false;
            return;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(rotation);
        Bitmap map = Bitmap.createBitmap(frame, 0, 0, frame.getWidth(), frame.getHeight(), matrix, false);
        waterMark.setImageBitmap(map);
        waterMark.setVisibility(View.VISIBLE);
        closeDetector();
        cameraView.close();
        isFrozen = true;
        isChanging = false;
    }

    private void processPausedFrame(@NonNull Bitmap frame) {
        if(detector != null) {
            Frame gframe = new Frame.Builder().setBitmap(frame).build();
            detector.detect(gframe, new VisionTextDetector.DetectionCallback() {
                @Override
                public void onDetectionComplete(boolean isSuccess, SparseArray<TextBlock> detections) {
                    onDetectPausedFrameComplete(isSuccess, detections, new Size(frame.getWidth(), frame.getHeight()));
                }
            });
        }
    }

    private void onDetectPausedFrameComplete(boolean isSuccess, SparseArray<TextBlock> detections, Size frameSize) {
        if(!isSuccess) {
            return;
        }
        new Thread(()->{
            if(elementDrawable == null) {
                elementDrawable = new ArrayList<>();
            }
            if(elements == null) {
                elements = new ArrayList<>();
            }
            pausedOverlay.clear();
            elementDrawable.clear();
            for(int i = 0; i < detections.size(); i++) {
                TextBlock block = detections.valueAt(i);
                for(Text line : block.getComponents()) {
                    for(Text text : line.getComponents()) {
                        Element element = (Element)text;
                        elements.add(element);
                        elementDrawable.add(new ElementDrawable(element, frameSize, new Size(cameraView.getWidth(), cameraView.getHeight())));
                    }
                }
            }
            pausedOverlay.add(elementDrawable);

        }).start();
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

        switch (v.getId()) {
            case R.id.main_camera_view: {
                onCameraViewTouch(event);
            } break;

            case R.id.main_camera_view_water_mark: {
                onWaterMarkTouch(event);
            } break;

            default: {

            }
        }
        return false;
    }

    private void onCameraViewTouch(MotionEvent event) {

    }

    private void onWaterMarkTouch(MotionEvent event) {
        if(elementDrawable == null) {
            return;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX();
                float y = event.getY();

                for(Drawable drawable : elementDrawable) {
                    ElementDrawable e = (ElementDrawable)drawable;
                    if(e.contain(new Point((int)x, (int)y))){
                        e.setSelected(true);
                        pausedOverlay.update();
                        return;
                    }
                }
            }

            default: {

            }
        }
    }

    @Override
    public void onCameraOpened(@NonNull CameraOptions options) {
        super.onCameraOpened(options);
        Log.d(TAG, "Camera Opened!");
    }

    @Override
    public void onCameraClosed() {
        super.onCameraClosed();
        Log.d(TAG, "Camera Closed");
    }

    @Override
    public void onPictureTaken(@NonNull PictureResult result) {
        result.toBitmap(new BitmapCallback() {
            @Override
            public void onBitmapReady(@Nullable Bitmap bitmap) {
                onPauseFrame(bitmap, result.getRotation());
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
