package com.example.visiontranslation.detector.text;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.SizeF;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.helper.NV21ToBitmap;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import org.opencv.android.Utils;
import org.opencv.imgproc.Imgproc;

public class VisionTextRecognizer implements FrameProcessor {

    private TextRecognizer recognizer;
    private boolean isReady;
    private NV21ToBitmap nv21ToBitmap;
    private Matrix matrix;
    private boolean isProcessing;
    private boolean isRecognizing;

    private OnDetectingResultListener listener;
    private OnDetectingResultListener mListener;

    public VisionTextRecognizer() {
        isReady = false;
        isProcessing = false;

    }

    public void fierup() {
        isRecognizing = true;
        isProcessing = false;
    }

    public void shutdown() {
        isRecognizing = false;
        isProcessing = false;
    }

    public void init() {
        isReady = false;
        recognizer = new TextRecognizer
                .Builder(
                VisionTranslationApplication
                        .getVisionTranslationApplication()
                        .getApplicationContext()
        ).build();
        nv21ToBitmap = new NV21ToBitmap(
                VisionTranslationApplication
                        .getVisionTranslationApplication()
                        .getApplicationContext()
        );
        matrix = new Matrix();

        isReady = recognizer.isOperational();
        isProcessing = false;
    }

    public void destroy() {
        if(recognizer != null) {
            recognizer.release();
        }
    }

    public SparseArray<TextBlock> detect(com.google.android.gms.vision.Frame frame, @Nullable OnDetectingResultListener callback) {
        SparseArray<TextBlock> result = recognizer.detect(frame);
        if(callback != null) {
            callback.onDetectionResult(result, new SizeF(frame.getMetadata().getWidth(), frame.getMetadata().getHeight()));
        }
        return result;
    }

    private void detect(com.google.android.gms.vision.Frame frame) {
        SparseArray<TextBlock> result = recognizer.detect(frame);
        if(listener != null) {
            listener.onDetectionResult(result, new SizeF(frame.getMetadata().getWidth(), frame.getMetadata().getHeight()));
            System.gc();
        }
    }


    public com.google.android.gms.vision.Frame prepareFrame(com.otaliastudios.cameraview.frame.Frame frame) {
        Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight());
        matrix.setRotate(frame.getRotation());
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        int rotation = frame.getRotation() == 0 ? Frame.ROTATION_0 : (frame.getRotation() == 90 ? Frame.ROTATION_90 : (frame.getRotation() == 180 ? Frame.ROTATION_180 : Frame.ROTATION_270));
        rotation = frame.getRotation() % 90;
        bitmap.recycle();
        return new com.google.android.gms.vision.Frame.Builder()
                .setBitmap(rotated)
                .setTimestampMillis(frame.getTime())
                .setRotation(0)
                .build();
    }

    public com.google.android.gms.vision.Frame prepareFrame(Bitmap bitmap) {
        // matrix.postRotate(frame.getRotation());
        // Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return new com.google.android.gms.vision.Frame.Builder()
                .setBitmap(bitmap)
                .build();
    }



    @Override
    public void process(@NonNull com.otaliastudios.cameraview.frame.Frame frame) {
        if(isReady && isRecognizing && !isProcessing) {
            isProcessing = true;
            detect(prepareFrame(frame));
            isProcessing = false;
        }

    }

    public void setOnDetectingResultListener(OnDetectingResultListener listener) {
        this.listener = listener;
    }


    public interface OnDetectingResultListener {
        void onDetectionResult(SparseArray<TextBlock> result, SizeF ratio);
    }

}