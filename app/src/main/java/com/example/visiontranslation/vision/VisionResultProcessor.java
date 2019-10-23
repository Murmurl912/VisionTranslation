package com.example.visiontranslation.vision;

import android.util.Size;

import androidx.annotation.NonNull;

public interface VisionResultProcessor<T> {
    public void onResult(@NonNull T result, @NonNull Size frameSize);

    public interface TextRecognitionListener {
        public void onText(String text);
    }

    public interface TextSelectionListener {
        public void onSelectionChanged(@NonNull String text);
    }
}

