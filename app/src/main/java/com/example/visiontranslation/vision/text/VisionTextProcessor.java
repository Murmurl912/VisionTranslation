package com.example.visiontranslation.vision.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Size;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.vision.VisionFrameProcessor;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class VisionTextProcessor extends VisionFrameProcessor<SparseArray<TextBlock>> {
    private TextRecognizer recognizer;

    public VisionTextProcessor(@NonNull Context context) {
        recognizer = new TextRecognizer.Builder(
                VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
        ).build();
    }

    @Override
    public boolean isOperational() {
        return recognizer.isOperational();
    }

    @NonNull
    @Override
    public SparseArray<TextBlock> onProcess(@NonNull Bitmap bitmap) {
        Frame frame = new Frame.Builder().setBitmap(bitmap).setRotation(0).build();
        Bitmap bitmap1 = frame.getBitmap();
        return recognizer.detect(frame);
    }
}
