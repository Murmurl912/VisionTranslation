package com.example.visiontranslation.detector.text;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
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
    private Matrix matrix;
    private boolean processing;
    private boolean isReady;
    private boolean isProcessing;
    private Size previewSize;
    Bitmap rotated;

    public VisionTextDetector() {
        textRecognizer = new TextRecognizer.Builder(
                VisionTranslationApplication.getVisionTranslationApplication()
                .getApplicationContext()
        ).build();
        nv21ToBitmap = new NV21ToBitmap(
                VisionTranslationApplication.getVisionTranslationApplication()
                .getApplicationContext()
        );
        matrix = new Matrix();
        processing = false;
        isReady = false;
        isProcessing = false;
    }

    public void setProcessor(@NonNull Detector.Processor<TextBlock> processor) {
        this.textRecognizer.setProcessor(processor);
        isReady = true;
    }
    public void setPreviewSize(@NonNull Size previewSize) {
        this.previewSize = previewSize;
    }

    public void fireup() {
        processing = true;
    }

    public void shutdown() {
        processing = false;
    }

    public Bitmap getFrame() {
        return rotated;
    }

    public void destroy() {
        processing = false;
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

    public Bitmap rotateAndCropImage(final Bitmap bitmap, final Size previewSize, int rotation) {
        rotation = Math.abs(rotation) / 90;
        Bitmap image = bitmap;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotation * 90);
        float ratio = ((float)previewSize.getHeight()) / previewSize.getWidth();
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

        if(isProcessing) {
            return;
        }

        if(!processing || !textRecognizer.isOperational() || !isReady) {
            return;
        }

        // lock
        isProcessing = true;
        try {


            long startProcessingTime = System.currentTimeMillis();
            Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight());
            long startRotatingTime = System.currentTimeMillis();
            matrix.setRotate(frame.getRotation());
            if(rotated != null && !rotated.isRecycled()) {
                rotated.recycle();
            }
            rotated = rotateAndCropImage(bitmap, previewSize, frame.getRotation());
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


        } catch (Exception e) {
            isProcessing = false;
        } finally {
            // unlock
            isProcessing = false;
        }

    }

    public interface DetectionCallback {
        public void onDetectionComplete(boolean isSuccess, SparseArray<TextBlock> detections);
    }

}
