package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.method.TextKeyListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.R;

public class BoundingDrawable extends OverlayDrawable {

    private Paint stroke;
    private Paint fill;

    private RectF rectF;

    public BoundingDrawable(RectF rectF) {
        super();
        this.rectF = rectF == null ? new RectF() : rectF;
        stroke = new Paint();
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(5);
        stroke.setColor(Color.GREEN);

        fill = new Paint();
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(Color.parseColor("#88ffffff"));
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF == null ? new RectF() : rectF;
    }

    public RectF getRectF() {
        if(rectF == null) {
            rectF = new RectF();
        }
        return rectF;
    }

    public Paint getFill() {
        return fill;
    }

    public Paint getStroke() {
        return stroke;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect rect = canvas.getClipBounds();
        if(rect.isEmpty()) {
            rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        RectF o = new RectF(
                rectF.left * rect.width(),
                rectF.top * rect.height(),
                rectF.right * rect.width(),
                rectF.bottom * rect.height()
        );

        canvas.drawRect(
                o,
                stroke
        );
        canvas.drawRect(o, fill);
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
