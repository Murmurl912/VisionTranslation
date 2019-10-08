package com.example.visiontranslation.detector.text;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.L;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Word implements Text {

    private List<Letter> letters;

    private RectF boundingBox;

    private PointF[] pointFS;

    private String value;


    private Word(String value, RectF relativeBounding, PointF[] relativePoint, List<Letter> letters) {
        this.pointFS = relativePoint;
        this.boundingBox = relativeBounding;
        this.letters = letters;
        this.value = value;
    }

    @Nullable
    public static Word build(@NonNull String value, @NonNull RectF relativeBounding, @NonNull PointF[] relativePoint) {
        if(relativePoint.length != 4) {
            return null;
        }

        return new Word(value, relativeBounding, relativePoint, new ArrayList<>());
    }

    @Nullable
    public static Word build(@NonNull List<Letter> letters) {

        RectF bounding = new RectF();
        PointF[] points = new PointF[4];
        StringBuilder value = new StringBuilder();

        for(Letter letter : letters) {
            if(letter == null) {
                return null;
            }

            PointF[] letterPoint = letter.getBoundingPoints();
            RectF letterBox = letter.getBoundingBox();
            bounding.left = bounding.left < letterBox.left ? bounding.left : letterBox.left;
            bounding.top = bounding.top < letterBox.top ? bounding.top : letterBox.top;
            bounding.right = bounding.right > letterBox.right ? bounding.right : letterBox.right;
            bounding.bottom = bounding.bottom > letterBox.bottom ? bounding.bottom : letterBox.bottom;

            value.append(letter.getValue());
        }


        return new Word(value.toString(), bounding, points, letters);


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
        return TextType.WORD;
    }

    @NonNull
    @Override
    public List<? extends Text> getElements() {
        return letters;
    }
}
