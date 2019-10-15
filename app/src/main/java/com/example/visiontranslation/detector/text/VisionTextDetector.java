package com.example.visiontranslation.detector.text;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.helper.NV21ToBitmap;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;


public class VisionTextDetector implements FrameProcessor {

    public final String TAG = "VisionTextDetector";
    private TextRecognizer textRecognizer;
    private NV21ToBitmap nv21ToBitmap;
    private Matrix matrix = new Matrix();
    private boolean isReady = false;
    private Size processingSize;
    private Bitmap rotated;
    private final Object lock = new Object();
    private boolean enableDetector = false;

    public VisionTextDetector() {
        textRecognizer = new TextRecognizer.Builder(
                VisionTranslationApplication.getVisionTranslationApplication()
                .getApplicationContext()
        ).build();
        nv21ToBitmap = new NV21ToBitmap(
                VisionTranslationApplication.getVisionTranslationApplication()
                .getApplicationContext()
        );
    }

    public void setProcessor(@NonNull Detector.Processor<TextBlock> processor) {
        this.textRecognizer.setProcessor(processor);
        isReady = true;
    }
    public void setProcessingSize(@NonNull Size processingSize) {
        synchronized (lock) {
            this.processingSize = processingSize;
        }
    }

    public void fireup() {
        synchronized (lock){
            enableDetector = true;
        }
    }

    public void shutdown() {
        synchronized (lock) {
            enableDetector = false;
        }
    }

    public Bitmap getFrame() {
        synchronized (lock) {
            return rotated;
        }
    }

    public void destroy() {
        isReady = false;
        textRecognizer.release();
    }

    public void setFocusID(int id) {
        if(isReady && textRecognizer.isOperational()) {
            this.textRecognizer.setFocus(id);
        }
    }

    public void detect(@NonNull com.google.android.gms.vision.Frame frame, @NonNull DetectionCallback callback) {
        if(textRecognizer.isOperational()) {
            try {
                SparseArray<TextBlock> results = textRecognizer.detect(frame);
                callback.onDetectionComplete(true, results);
            } catch (Exception e) {
                callback.onDetectionComplete(false, null);
            }

        } else {
            callback.onDetectionComplete(false, null);
        }
    }

    private Bitmap rotateAndCropImage(final Bitmap bitmap, int rotation) {
        rotation = Math.abs(rotation) / 90;
        Bitmap image = bitmap;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotation * 90);
        float ratio = ((float)processingSize.getHeight()) / processingSize.getWidth();
        float height = bitmap.getHeight();
        float width = height * ratio;
        image = Bitmap.createBitmap(
                image,
                (int)((bitmap.getWidth() - width) / 2),
                0,
                (int)width,
                (int)height,
                matrix,
                false
        );
        return image;
    }

    @Override
    public void process(@NonNull Frame frame) {

        if(!textRecognizer.isOperational() || !isReady || !enableDetector) {
            return;
        }

        synchronized (lock) {
            long startProcessingTime = System.currentTimeMillis();
            Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight());
            long startRotatingTime = System.currentTimeMillis();
            matrix.setRotate(frame.getRotation());
            rotated = rotateAndCropImage(bitmap, frame.getRotation());
            long startConvertTime = System.currentTimeMillis();
            com.google.android.gms.vision.Frame image = new com.google.android.gms.vision.Frame.Builder()
                    .setBitmap(rotated)
                    .setTimestampMillis(frame.getTime())
                    .setRotation(0)
                    .build();
            long startDetectingTime = System.currentTimeMillis();
            // problem code should not recycle bitmap
            // rotated.recycle();
            textRecognizer.receiveFrame(image);
            long endDetectingTime = System.currentTimeMillis();

            Log.d(TAG, "A\nFrame Time: " + (System.currentTimeMillis() - startProcessingTime)
                    + "\n\tNV21 To Bitmap Time: " + (startRotatingTime - startProcessingTime)
                    + "\n\tRotate Bitmap Time: " + (startConvertTime - startRotatingTime)
                    + "\n\tBitmap To Frame Time: " + (startDetectingTime - startConvertTime)
                    + "\n\tDetecting Time: " + (endDetectingTime - startDetectingTime)
            );

        }

    }

    public interface DetectionCallback {
        public void onDetectionComplete(boolean isSuccess, SparseArray<TextBlock> detections);
    }

}
