package com.example.visiontranslation.ui.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private List<Drawable> drawables;

    public GraphicOverlay(Context context) {
        super(context);
        initialize();
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public GraphicOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public GraphicOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        drawables = new ArrayList<>();
    }

    private void addDrawable() {

    }

    public void removeDrawable() {

    }

    public void clearDrawable() {

    }

    public void update() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        Canvas canvas = surfaceHolder.lockCanvas();
        surfaceHolder.unlockCanvasAndPost(canvas);
    }
}
