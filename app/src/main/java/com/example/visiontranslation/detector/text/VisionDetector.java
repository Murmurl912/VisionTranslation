package com.example.visiontranslation.detector.text;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Size;

import androidx.annotation.NonNull;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.helper.NV21ToBitmap;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

public abstract  class VisionDetector implements FrameProcessor {

    private final Object lock = new Object();
    private boolean enableDetector = false;
    private NV21ToBitmap nv21ToBitmap;
    private Size processingSize;
    private boolean isReady = false;

    public VisionDetector() {
        nv21ToBitmap = new NV21ToBitmap(
                VisionTranslationApplication
                        .getVisionTranslationApplication()
                        .getApplicationContext()
        );
    }

    public void setFrameProcessingSize(@NonNull Size size) {
        this.processingSize = size;
        isReady = true;

    }

    private Bitmap rotateAndCropImage(final Bitmap bitmap, int rotation) {

        rotation = Math.abs(rotation) / 90;
        Bitmap image = bitmap;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotation * 90);
        float height = bitmap.getHeight();
        float width = height;
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

    public void fireup() {
        enableDetector = true;
    }

    public void shutdown() {
        enableDetector = false;
    }

    @Override
    public void process(@NonNull Frame frame) {

        if(!enableDetector) {
            return;
        }


        synchronized (lock) {
            Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(
                    frame.getData(),
                    frame.getSize().getWidth(),
                    frame.getSize().getHeight()
            );

            Bitmap rotated = rotateAndCropImage(bitmap, frame.getRotation());

        }

    }

}
