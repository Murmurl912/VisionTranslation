package com.example.visiontranslation.ui.camera;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.visiontranslation.R;
import com.example.visiontranslation.detector.text.VisionTextRecognizer;
import com.example.visiontranslation.overlay.BoundingDrawable;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.overlay.OverlayDrawable;
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


    private CameraView cameraView;
    private CameraListener cameraListener;
    private FrameProcessor frameProcessor;
    private GraphicsOverlay overlay;
    private OverlayDrawable trackingResultBoundingBox;
    private OverlayDrawable selectionBoundingBox;
    private OnCapturedPictureListener capturedPictureListener;
    private VisionTextRecognizer textRecognizer;

    private List<TextBlockDrawable> textBlockDrawableList;


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
        startCamera();

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

    @SuppressLint("ClickableViewAccessibility")
    private void startCamera() {
        if(!isAllPermissionGranted()) {
            requestPermission();
        }
        // set up over lay
        overlay = new GraphicsOverlay(cameraView, getActivity());

        // bound to lifecycle
        cameraView.setLifecycleOwner(getViewLifecycleOwner());

        setupCameraListener();
        setupCameraViewTouchListener();
        setupFrameProcessor();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCameraViewTouchListener() {
        // set up camera view touch listener
        cameraView.setOnTouchListener((v, e)->{

            if(selectionBoundingBox == null) {
                selectionBoundingBox = new BoundingDrawable(new RectF());
            }
            RectF rectF = ((BoundingDrawable)selectionBoundingBox).getRectF();
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    overlay.add(selectionBoundingBox);

                    rectF.set(e.getX() / v.getWidth(), e.getY() / v.getHeight(), e.getX() / v.getWidth(), e.getY() / v.getHeight());
                    if(rectF.right > 1) {
                        rectF.right = 1;
                    } else if (rectF.right < 0) {
                        rectF.right = 0;
                    }

                    if(rectF.left > 1) {
                        rectF.left = 1;
                    } else if (rectF.left < 0) {
                        rectF.left = 0;
                    }
                    if(rectF.top > 1) {
                        rectF.top = 1;
                    } else if (rectF.top < 0) {
                        rectF.top = 0;
                    }

                    if(rectF.bottom > 1) {
                        rectF.bottom = 1;
                    } else if (rectF.bottom < 0) {
                        rectF.bottom = 0;
                    }
                    overlay.update();
                } break;

                case MotionEvent.ACTION_UP: {
                    rectF.set(rectF.left, rectF.top,e.getX() / v.getWidth(), e.getY() / v.getHeight());
                    if(rectF.right > 1) {
                        rectF.right = 1;
                    } else if (rectF.right < 0) {
                        rectF.right = 0;
                    }

                    if(rectF.left > 1) {
                        rectF.left = 1;
                    } else if (rectF.left < 0) {
                        rectF.left = 0;
                    }
                    if(rectF.top > 1) {
                        rectF.top = 1;
                    } else if (rectF.top < 0) {
                        rectF.top = 0;
                    }

                    if(rectF.bottom > 1) {
                        rectF.bottom = 1;
                    } else if (rectF.bottom < 0) {
                        rectF.bottom = 0;
                    }
                    overlay.update();
                    onAreaSelected(new Size(v.getWidth(), v.getHeight()), rectF);

                } break;

                case MotionEvent.ACTION_MOVE: {
                    rectF.set(rectF.left, rectF.top, e.getX() / v.getWidth(), e.getY() / v.getHeight());
                    if(rectF.right > 1) {
                        rectF.right = 1;
                    } else if (rectF.right < 0) {
                        rectF.right = 0;
                    }

                    if(rectF.left > 1) {
                        rectF.left = 1;
                    } else if (rectF.left < 0) {
                        rectF.left = 0;
                    }
                    if(rectF.top > 1) {
                        rectF.top = 1;
                    } else if (rectF.top < 0) {
                        rectF.top = 0;
                    }

                    if(rectF.bottom > 1) {
                        rectF.bottom = 1;
                    } else if (rectF.bottom < 0) {
                        rectF.bottom = 0;
                    }
                    overlay.update();
                } break;

                default: {

                }
            }
            return false;
        });
    }

    private void setupCameraListener() {

        // set up camera listener
        if(cameraListener == null) {
            cameraListener = new CameraListener() {
                @Override
                public void onPictureTaken(@NonNull PictureResult result) {
                    super.onPictureTaken(result);
                    // start tracking
                    result.toBitmap(new BitmapCallback() {
                        @Override
                        public void onBitmapReady(@Nullable Bitmap bitmap) {
                            overlay.remove(selectionBoundingBox);
                            if(capturedPictureListener != null) {
                                capturedPictureListener.onCapturedPicture(bitmap);
                            }
                        }
                    });

                }




            };
        }
        cameraView.addCameraListener(cameraListener);
    }

    private void setupFrameProcessor() {
        // set up image processor
        if(frameProcessor == null) {
            frameProcessor = new GenericTracker();
            ((GenericTracker)frameProcessor).setTrackingResultCallback(new GenericTracker.TrackingResultCallback() {
                @Override
                public void onTrackingResult(boolean isSuccess, org.opencv.core.Size frameSize, Rect2d relativeCoordinate) {
                    if(trackingResultBoundingBox == null) {
                        trackingResultBoundingBox = new BoundingDrawable(
                                new RectF(
                                        (float)relativeCoordinate.x,
                                        (float)relativeCoordinate.y,
                                        (float)(relativeCoordinate.x + relativeCoordinate.width),
                                        (float)(relativeCoordinate.y + relativeCoordinate.height)));
                        overlay.add(trackingResultBoundingBox);
                    } else {
                        RectF rectF = ((BoundingDrawable)trackingResultBoundingBox).getRectF();
                        rectF.set(
                                (float)relativeCoordinate.x,
                                (float)relativeCoordinate.y,
                                (float)(relativeCoordinate.x + relativeCoordinate.width),
                                (float)(relativeCoordinate.y + relativeCoordinate.height));
                    }
                    overlay.update();
                }
            });
        }
        cameraView.addFrameProcessor(frameProcessor);

        // set up text recognizer
        if(textRecognizer == null) {
            textRecognizer = new VisionTextRecognizer();
            textRecognizer.setOnDetectingResultListener(((result, size) -> {
                if(textBlockDrawableList == null) {
                    textBlockDrawableList = new ArrayList<>();
                } else {
                    overlay.remove(textBlockDrawableList);
                    textBlockDrawableList.clear();
                }
                for(int i = 0; i < result.size(); i++) {
                    TextBlock block = result.get(result.keyAt(i));
                    textBlockDrawableList.add(TextBlockDrawable.build(block, size));
                }
                overlay.add(textBlockDrawableList);
            }));
            textRecognizer.init();
        }
        cameraView.addFrameProcessor(textRecognizer);
        textRecognizer.fierup();

    }

    private void onAreaSelected(Size windowSize, final RectF bounding) {

        if(Math.abs(bounding.width() * bounding.height()) < 0.0005) {
            return;
        }

        capturedPictureListener = new OnCapturedPictureListener() {
            @Override
            public void onCapturedPicture(Bitmap bitmap) {
                if(frameProcessor == null || bitmap == null) {
                    return;
                }

                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB);
                ((GenericTracker)frameProcessor).shutdown();
                ((GenericTracker)frameProcessor).init(mat, new Rect2d(bounding.left, bounding.top, bounding.width(), bounding.height()));
                ((GenericTracker)frameProcessor).fireup();
            }
        };
        // cameraView.takePicture();
        cameraView.takePictureSnapshot();

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

    private void showImage(Bitmap bitmap) {
        Activity activity = MainActivity.getActivity();

        if(activity != null && bitmap != null) {
            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.dialog_image);
            ImageView imageView = dialog.findViewById(R.id.dialog_image_view);
            imageView.setImageBitmap(bitmap);
            dialog.show();

        }

    }

    public interface OnCapturedPictureListener {
        void onCapturedPicture(Bitmap bitmap);
    }

}
