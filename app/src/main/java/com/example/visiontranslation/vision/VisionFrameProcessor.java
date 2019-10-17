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

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB scriptIntrinsicYuvToRGB;

    private VisionResultProcessor<T> visionResultProcessor;
    private boolean enableProcessor = true;
    private Bitmap preparedFrame;
    private final Object lock = new Object();
    private Size processSize;

    public VisionFrameProcessor(@NonNull Context context, @NonNull Size processSize) {
        rs = RenderScript.create(context);
        scriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        this.processSize = processSize;
    }

    public final synchronized Bitmap nv21ToBitmap(@NonNull byte[] data, int width, int height) {
        Allocation in, out;
        Type.Builder yuv, rgba;

        yuv = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
        in = Allocation.createTyped(rs, yuv.create(), Allocation.USAGE_SCRIPT);

        rgba = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        out = Allocation.createTyped(rs, rgba.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(data);
        scriptIntrinsicYuvToRGB.setInput(in);
        scriptIntrinsicYuvToRGB.forEach(out);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bitmap);
        return bitmap;
    }

    public boolean isProcessorEnabled() {
        return enableProcessor;
    }

    public void enableProcessor(boolean enable) {
        this.enableProcessor = enable;
    }

    public void setProcessSize(@NonNull Size size) {
        this.processSize = size;
    }

    @NonNull
    public Size getProcessSize() {
        return processSize;
    }

    public void onFrame(@NonNull Bitmap frame, int rotation) {

        if(!enableProcessor) {
            return;
        }

        synchronized (lock) {
            preparedFrame = onPrepare(frame, rotation);
            if(visionResultProcessor != null) {
                visionResultProcessor.onResult(onProcess(preparedFrame), processSize);
            }
        }
    }

    @NonNull
    public abstract Bitmap onPrepare(@NonNull Bitmap bitmap, int rotation);

    @NonNull
    public abstract T onProcess(@NonNull Bitmap bitmap);

    public abstract boolean isOperational();

    @Nullable
    protected Bitmap getPreparedFrame() {
        synchronized (lock) {
            if(preparedFrame != null && !preparedFrame.isRecycled()) {
                return preparedFrame.copy(preparedFrame.getConfig(), true);
            } else {
                return null;
            }
        }
    }

    public final void setVisionResultProcessor(@NonNull VisionResultProcessor<T> processor) {
        this.visionResultProcessor = processor;
    }
}
