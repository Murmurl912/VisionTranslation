package com.example.visiontranslation.detector.text;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.List;

public interface Text {

    public enum TextType {
        LETTER, WORD, LINE, BLOCK
    }

    @NonNull
    public RectF getBoundingBox();

    @NonNull
    public PointF[] getBoundingPoints();

    @NonNull
    public String getValue();

    @NonNull
    public TextType getType();

    @NonNull
    public List<? extends Text> getElements() ;
}
