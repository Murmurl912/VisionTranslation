package com.example.visiontranslation.ui.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.SizeF;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.example.visiontranslation.vision.FrameProcessorBridge;
import com.example.visiontranslation.vision.VisionFrameProcessor;
import com.google.android.gms.vision.text.TextBlock;

public class VisionTextProcessorBridge extends FrameProcessorBridge<SparseArray<TextBlock>> {

    public VisionTextProcessorBridge(
            @NonNull VisionFrameProcessor<SparseArray<TextBlock>> processor,
            @NonNull SizeF aspectRatio) {
        super(processor, aspectRatio);
    }

    @NonNull
    @Override
    public Bitmap adapt(@NonNull Bitmap bitmap, int rotation) {
        rotation = Math.abs(rotation) / 90;
        Bitmap image = bitmap;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotation * 90);
        float ratio = getAspectRatio().getHeight() / getAspectRatio().getWidth();
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

}
