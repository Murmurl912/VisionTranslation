package com.example.visiontranslation.vision;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.util.SizeF;

import androidx.annotation.NonNull;

import com.example.visiontranslation.VisionTranslationApplication;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

public abstract class FrameProcessorBridge<T> implements FrameProcessor {

    private SizeF aspectRatio;
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB scriptIntrinsicYuvToRGB;
    private VisionFrameProcessor<T> processor;

    public FrameProcessorBridge(@NonNull VisionFrameProcessor<T> processor,
                                @NonNull SizeF aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.processor = processor;
        rs = RenderScript.create(
                VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
        );
        scriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));    }

    @NonNull
    private synchronized Bitmap nv21ToBitmap(@NonNull byte[] data, int width, int height) {
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

    public VisionFrameProcessor<T> getProcessor() {
        return processor;
    }

    public final void setAspectRatio(@NonNull SizeF aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    @NonNull
    public final SizeF getAspectRatio() {
        return aspectRatio;
    }

    @NonNull
    public abstract Bitmap adapt(@NonNull Bitmap bitmap, int rotation);

    private boolean isProcessing = false;

    @Override
    public void process(@NonNull Frame frame) {
        try {
            if(isProcessing) {
                return;
            }
            isProcessing = true;
            Bitmap bitmap = nv21ToBitmap(
                    frame.getData(),
                    frame.getSize().getWidth(),
                    frame.getSize().getHeight()
            );

            processor.onFrame(adapt(bitmap, frame.getRotation()));
        } catch (Exception e) {
            Log.d("Process Error", "Exception", e);
        } finally {
            isProcessing = false;
        }

    }

}
