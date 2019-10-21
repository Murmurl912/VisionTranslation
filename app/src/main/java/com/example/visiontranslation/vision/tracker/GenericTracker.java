package com.example.visiontranslation.vision.tracker;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.helper.NV21ToBitmap;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect2d;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.tracking.TrackerBoosting;
import org.opencv.tracking.TrackerCSRT;
import org.opencv.tracking.TrackerKCF;
import org.opencv.tracking.TrackerMIL;
import org.opencv.tracking.TrackerMOSSE;
import org.opencv.tracking.TrackerMedianFlow;
import org.opencv.tracking.TrackerTLD;

public class GenericTracker implements FrameProcessor {

    public final String TAG = "GenericTracker";

    private org.opencv.tracking.Tracker tracker;
    private Mat initialFrame;
    private Rect2d initialBoundingBox;

    private boolean isTracking;
    private boolean isReady;

    private Rect2d absolute;
    private NV21ToBitmap nv21ToBitmap;
    private Mat image;

    private TrackingResultCallback callback;

    private int tracker_type = 2;
    public static final int CSRT = 1; // 40 - 60
    public static final int MOSSE = 2;
    public static final int MIL = 3;
    public static final int MedianFlow = 4;
    public static final int TLD = 5;
    public static final int KCF = 6;
    public static final int GOTURN = 7;
    public static final int Boosting = 8;  // 50 -70

    private long lastFrameTime;
    private boolean isProcessing = true;
    public GenericTracker() {

        absolute = new Rect2d();
        nv21ToBitmap = new NV21ToBitmap(VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext());
        image = new Mat();
        isReady = false;
        isTracking = false;
    }

    private void create(int type) {
        switch (type) {
            case CSRT : tracker = TrackerCSRT.create(); break;
            case MOSSE : tracker = TrackerMOSSE.create(); break;
            case MIL : tracker = TrackerMIL.create(); break;
            case MedianFlow : tracker = TrackerMedianFlow.create(); break;
            case TLD : tracker = TrackerTLD.create(); break;
            case KCF : tracker = TrackerKCF.create(); break;
            /* cannot use this code
              case GOTURN : tracker = TrackerGOTURN.create();

                Net net = Dnn.readNetFromCaffe(
                        "/data/data/com.example.visiontranslation/goturn.prototxt",
                        "/data/data/com.example.visiontranslation/goturn.caffemodel");

                break;
             */
            case Boosting : tracker = TrackerBoosting.create(); break;
        }
    }

    public void init(Mat frame, Rect2d relativeBoundingBox) {
        isReady = false;
        if(frame == null || relativeBoundingBox == null) {
            return;
        }
        if(initialFrame != null) {
            initialFrame.release();
        }
        initialFrame = frame;
        initialBoundingBox = new Rect2d(
                relativeBoundingBox.x * frame.width(),
                relativeBoundingBox.y * frame.height(),
                relativeBoundingBox.width * frame.width(),
                relativeBoundingBox.height * frame.height());
        create(tracker_type);
        tracker.init(initialFrame, initialBoundingBox);
        isReady = true;
        isProcessing = false;
    }

    public void fireup() {
        isTracking = true;
        isProcessing = false;
    }

    public void shutdown() {
        isTracking = false;
        isProcessing = false;
        if(initialFrame != null)  {
            initialFrame.release();
        }
    }

    public void destroy() {
        try {
            image.release();
            initialFrame.release();
            isReady = false;
            isTracking = false;
        } catch (Throwable e) {

        }

    }

    public void setTrackingResultCallback(TrackingResultCallback callback) {
        this.callback = callback;
    }

    private void onTargetLost() {
        if(callback != null) {
            callback.onTrackingResult(
                    false,
                    image.size(),
                    new Rect2d(
                            absolute.x / image.width(),
                            absolute.y / image.height(),
                            absolute.width / image.width(),
                            absolute.height / image.height()));
        }
    }

    private void onTargetTracked() {
        if(callback != null) {
            callback.onTrackingResult(
                    true,
                    image.size(),
                    new Rect2d(
                            absolute.x / image.width(),
                            absolute.y / image.height(),
                            absolute.width / image.width(),
                            absolute.height / image.height()));
        }
    }

    private void track(Mat frame) {
        long trackerStart = System.currentTimeMillis();
        Imgproc.resize(frame, frame, initialFrame.size());
        tracker.update(frame, absolute);
        long trackerUpdate = System.currentTimeMillis();

        if(absolute.x <= 0 && ( absolute.y <= 0 || absolute.y >= frame.height())) {
            onTargetLost();
        } else if(absolute.x >= frame.width() && (absolute.y <= 0 || absolute.y >= frame.height())) {
            onTargetLost();
        } else {
            onTargetTracked();
        }

        long handleResultTime = System.currentTimeMillis();

        Log.d(TAG, "B  \nTracking Frame Time Cost:\n\t Tracker Update: " + (trackerUpdate - trackerStart)
        + "\n\t Tracking Result Handle: " + (handleResultTime - trackerUpdate)
        + "\n\t Overall: " + (handleResultTime - trackerStart));
    }

    private void prepareFrame(Frame frame) {

        long startPrepareFrame = System.currentTimeMillis();

        Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight());

        long frameConvertedToBitmap = System.currentTimeMillis();


        Utils.bitmapToMat(bitmap, image);
        bitmap.recycle();
        rotate(image, frame.getRotation());

        long bitmapToMatTime = System.currentTimeMillis();

        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2RGB);

        long matToRGBTime = System.currentTimeMillis();

        Log.d(TAG, "A  \nPrepare Frame Time Cost: \n\t NV21 To Bitmap: " + (frameConvertedToBitmap - startPrepareFrame)
        + "\n\t Bitmap To Mat: " + (bitmapToMatTime - frameConvertedToBitmap)
        + "\n\t Convert RGBA To RGB: " + (matToRGBTime - bitmapToMatTime)
        + "\n\t Overall: " + (matToRGBTime - startPrepareFrame));
    }

    private void rotate(@NonNull Mat mat, int degree) {
        degree = Math.abs(degree) % 360;
        if(degree == 0) {
            return;
        }

        switch (degree % 360) {

            case 90: {
                Core.transpose(mat, mat);
                Core.flip(mat, mat, 1);
            } break;

            case 180: {
                Core.flip(mat, mat, 0);
                Core.flip(mat, mat, 1);

            } break;

            case 270: {
                Core.transpose(mat, mat);
                Core.flip(mat, mat, 0);
            } break;
        }
    }

    @Override
    public void process(@NonNull Frame frame) {
        if(isReady && isTracking && !isProcessing) {
            isProcessing = true;
            long startProcessTime = System.currentTimeMillis();
            prepareFrame(frame);
            track(this.image);
            long endProcessTime = System.currentTimeMillis();
            lastFrameTime = endProcessTime - lastFrameTime;
            isProcessing = false;
            Log.d(TAG, "C  \nOverall Frame Time: " + (endProcessTime - startProcessTime));
        }
    }

    public interface TrackingResultCallback {
        public void onTrackingResult(boolean isSuccess, Size frameSize, Rect2d relativeCoordinate);
    }
}
