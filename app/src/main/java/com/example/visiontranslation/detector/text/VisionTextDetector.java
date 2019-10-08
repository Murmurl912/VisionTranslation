package com.example.visiontranslation.detector.text;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.helper.NV21ToBitmap;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.nio.ByteBuffer;

public class VisionTextDetector implements FrameProcessor {

    public final String TAG = "VisionTextDetector";
    private final Object lock;
    private TextRecognizer textRecognizer;
    private NV21ToBitmap nv21ToBitmap;
    private Matrix matrix;
    private boolean processing;
    private Detector.Processor<TextBlock> processor;
    private boolean isReady;
    private boolean isProcessing;
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
        lock = new Object();
        isProcessing = false;
    }

    public void setProcessor(@NonNull Detector.Processor<TextBlock> processor) {
        this.textRecognizer.setProcessor(processor);
        isReady = true;
    }

    public void fireup() {
        processing = true;
    }

    public void shutdown() {
        processing = false;
    }

    public void destroy() {
        processing = false;
        isReady = false;
        textRecognizer.release();
    }

    @Override
    public void process(@NonNull Frame frame) {

        if(isProcessing) {
            return;
        }

        if(!processing || !textRecognizer.isOperational() || !isReady) {
            return;
        }
        try {
            /*
            long startProcessingTime = System.currentTimeMillis();
            isProcessing = true;
            Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight());
            long startRotatingTime = System.currentTimeMillis();
            matrix.setRotate(frame.getRotation());
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            long startConvertTime = System.currentTimeMillis();
            int rotation = frame.getRotation() == 0 ? com.google.android.gms.vision.Frame.ROTATION_0 : (frame.getRotation() == 90 ? com.google.android.gms.vision.Frame.ROTATION_90 : (frame.getRotation() == 180 ? com.google.android.gms.vision.Frame.ROTATION_180 : com.google.android.gms.vision.Frame.ROTATION_270));
            rotation = frame.getRotation() % 90;
            bitmap.recycle();
            com.google.android.gms.vision.Frame image = new com.google.android.gms.vision.Frame.Builder()
                    .setBitmap(rotated)
                    .setTimestampMillis(frame.getTime())
                    .setRotation(0)
                    .build();
            long startDetectingTime = System.currentTimeMillis();
            // problem code should not recycle bitmap
            // rotated.recycle();

             */
            long startDetectingTime = System.currentTimeMillis();
            byte[] bytes = frame.getData();
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            com.google.android.gms.vision.Frame image = new com.google.android.gms.vision.Frame.Builder()
                    .setImageData( byteBuffer, frame.getSize().getWidth(), frame.getSize().getHeight(), ImageFormat.NV21)
                    .setRotation(frame.getRotation() % 90)
                    .setTimestampMillis(frame.getTime()).build();
            long time = System.currentTimeMillis();
            textRecognizer.receiveFrame(image);
            isProcessing = false;
            long endDetectingTime = System.currentTimeMillis();
            Log.d(TAG, "A\nFrame Time: " + (endDetectingTime - startDetectingTime
                    + "\nPreparation Time: " + (time - startDetectingTime)
            ));
            /*
            Log.d(TAG, "A\nFrame Time: " + (System.currentTimeMillis() - startProcessingTime)
                    + "\n\tNV21 To Bitmap Time: " + (startRotatingTime - startProcessingTime)
                    + "\n\tRotate Bitmap Time: " + (startConvertTime - startRotatingTime)
                    + "\n\tBitmap To Frame Time: " + (startDetectingTime - startConvertTime)
                    + "\n\tDetecting Time: " + (endDetectingTime - startDetectingTime)
            );
d
             */
        } catch (Exception e) {
            isProcessing = false;
        }

    }

}
