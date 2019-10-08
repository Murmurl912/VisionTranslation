package com.example.visiontranslation.detector.text;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Letter implements Text {

    private RectF boundingBox;

    private PointF[] pointFS;

    private String value;

    private List<? extends Text> elements;


    private Letter(String value, RectF relativeBounding, PointF[] relativePoint) {
        this.pointFS = relativePoint;
        this.boundingBox = relativeBounding;
        this.value = value;
        this.elements = new ArrayList<Text>();
    }

    @Nullable
    public static Letter build(@NonNull String value, @NonNull RectF relativeBounding, @NonNull PointF[] relativePoint) {
        if(relativePoint.length != 4) {
            return null;
        }
        return new Letter(value, relativeBounding, relativePoint);
    }

    @NonNull
    @Override
    public RectF getBoundingBox() {
        return boundingBox;
    }

    @NonNull
    @Override
    public PointF[] getBoundingPoints() {
        return pointFS;
    }

    @NonNull
    @Override
    public String getValue() {
        return value;
    }

    @NonNull
    @Override
    public TextType getType() {
        return TextType.LETTER;
    }

    @NonNull
    @Override
    public List<? extends Text> getElements() {
        return elements;
    }
}
