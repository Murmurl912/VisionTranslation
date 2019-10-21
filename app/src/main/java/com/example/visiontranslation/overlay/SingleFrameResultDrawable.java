package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Size;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;

public class SingleFrameResultDrawable extends Drawable {

    private SparseArray<TextBlock> blocks;
    private Size size;

    public SingleFrameResultDrawable(SparseArray<TextBlock> blocks, Size frameSize) {
        this.blocks = blocks;
        this.size = frameSize;
    }

    private void decode() {
        for(int i = 0; i < blocks.size(); i++) {
            TextBlock block = blocks.valueAt(i);
            for(Text a : block.getComponents()) {
                Line line = (Line)a;
                for(Text b : a.getComponents()) {
                    Element element = (Element)b;

                }
            }
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
