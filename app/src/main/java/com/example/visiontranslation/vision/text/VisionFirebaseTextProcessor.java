package com.example.visiontranslation.vision.text;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.example.visiontranslation.vision.VisionFrameProcessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class VisionFirebaseTextProcessor extends VisionFrameProcessor<Task<FirebaseVisionText>> {

    private FirebaseVisionTextRecognizer recognizer;

    public VisionFirebaseTextProcessor() {
        recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

    }

    @NonNull
    @Override
    public Task<FirebaseVisionText> onProcess(@NonNull Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        return recognizer.processImage(image);
    }

    @Override
    public boolean isOperational() {
        return true;
    }
}
