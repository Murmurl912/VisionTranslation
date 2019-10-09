package com.example.visiontranslation.detector.text;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Block implements Text {
    private List<Line> lines;

    private RectF boundingBox;

    private PointF[] pointFS;

    private String value;



    private Block(String value, RectF relativeBounding, PointF[] relativePoint, List<Line> lines) {
        this.pointFS = relativePoint;
        this.boundingBox = relativeBounding;
        this.lines = lines;
        this.value = value;
    }

    @Nullable
    public static Block build(@NonNull String value, @NonNull RectF relativeBounding, @NonNull PointF[] relativePoint) {
        if(relativePoint.length != 4) {
            return null;
        }

        return new Block(value, relativeBounding, relativePoint, new ArrayList<>());
    }

    @Nullable
    public static Block build(@NonNull List<Line> lines) {

        RectF bounding = new RectF();
        PointF[] points = new PointF[4];
        StringBuilder value = new StringBuilder();

        for(Line line : lines) {
            if(line == null) {
                return null;
            }

            RectF box = line.getBoundingBox();
            bounding.left = bounding.left < box.left ? bounding.left : box.left;
            bounding.top = bounding.top < box.top ? bounding.top : box.top;
            bounding.right = bounding.right > box.right ? bounding.right : box.right;
            bounding.bottom = bounding.bottom > box.bottom ? bounding.bottom : box.bottom;

            value.append(line.getValue());
        }


        return new Block(value.toString(), bounding, points, lines);
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
        return TextType.BLOCK;
    }

    @NonNull
    @Override
    public List<? extends Text> getElements() {
        return lines;
    }


}

