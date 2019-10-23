package com.example.visiontranslation.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public abstract class VisionFrameProcessor<T> {

    private VisionResultProcessor<T> visionResultProcessor;
    private boolean enableProcessor = true;
    private Bitmap bitmap;
    private final Object lock = new Object();
    public VisionFrameProcessor() {

    }

    public boolean isProcessorEnabled() {
        return enableProcessor;
    }

    public void enableProcessor(boolean enable) {
        this.enableProcessor = enable;
    }

    public void onFrame(@NonNull Bitmap frame) {

        if(!enableProcessor) {
            return;
        }

        synchronized (lock) {
            bitmap = frame;
            if(visionResultProcessor != null) {
                visionResultProcessor.onResult(
                        onProcess(bitmap),
                        new Size(
                                bitmap.getWidth(),
                                bitmap.getHeight()
                        )
                );
            }
        }
    }

    @NonNull
    public abstract T onProcess(@NonNull Bitmap bitmap);

    public abstract boolean isOperational();

    @Nullable
    protected Bitmap getLatestFrame() {
        synchronized (lock) {
            if(bitmap != null && !bitmap.isRecycled()) {
                return bitmap.copy(bitmap.getConfig(), true);
            } else {
                return null;
            }
        }
    }

    public final void setVisionResultProcessor(@NonNull VisionResultProcessor<T> processor) {
        this.visionResultProcessor = processor;
    }
}
