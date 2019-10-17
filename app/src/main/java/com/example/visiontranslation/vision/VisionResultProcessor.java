package com.example.visiontranslation.vision;

import android.util.Size;

import androidx.annotation.NonNull;

public interface VisionResultProcessor<T> {
    public void onResult(@NonNull T result, @NonNull Size processSize);
}

