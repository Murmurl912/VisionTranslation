package com.example.visiontranslation.detector.text;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Line implements Text {

    private RectF boundingBox;

    private PointF[] pointFS;

    private String value;

    private List<Word> words;

    private Line(String value, RectF relativeBounding, PointF[] relativePoint, List<Word> words) {
        this.pointFS = relativePoint;
        this.boundingBox = relativeBounding;
        this.value = value;
        this.words = words;
    }

    @Nullable
    public static Line build(@NonNull String value, @NonNull RectF relativeBounding, @NonNull PointF[] relativePoint) {
        if(relativePoint.length != 4) {
            return null;
        }

        return new Line(value, relativeBounding, relativePoint, new ArrayList<>());
    }

    @Nullable
    public static Line build(@NonNull List<Word> words) {

        RectF bounding = new RectF();
        PointF[] points = new PointF[4];
        StringBuilder value = new StringBuilder();

        for(Word word : words) {
            if(word == null) {
                return null;
            }

            RectF box = word.getBoundingBox();
            bounding.left = bounding.left < box.left ? bounding.left : box.left;
            bounding.top = bounding.top < box.top ? bounding.top : box.top;
            bounding.right = bounding.right > box.right ? bounding.right : box.right;
            bounding.bottom = bounding.bottom > box.bottom ? bounding.bottom : box.bottom;

            value.append(word.getValue());
        }


        return new Line(value.toString(), bounding, points, words);
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
        return TextType.LINE;
    }

    @NonNull
    @Override
    public List<? extends Text> getElements() {
        return words;
    }
}
