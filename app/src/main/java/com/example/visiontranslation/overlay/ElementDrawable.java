package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Size;
import android.util.SizeF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.text.Element;

public class ElementDrawable extends Drawable {
    private ColorFilter filter;
    private int alpha;
    private Element element;
    private Size frameSize;
    private Point[] points;
    private Paint paint;
    private int color;
    private boolean isSelected;
    private Rect rect;

    public ElementDrawable(@NonNull Element element, @NonNull Size frameSize) {
        this.element = element;
        this.frameSize = frameSize;
        this.points = element.getCornerPoints();
        this.rect = element.getBoundingBox();
        this.paint = new Paint();
        this.isSelected = false;
        this.color = Color.GREEN;
    }

    public void contain(Point point) {

    }

    private void setSelected(boolean selected) {
        this.isSelected = selected;
        if(selected) {
            this.color = Color.RED;
        } else {
            this.color = Color.GREEN;
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect box = canvas.getClipBounds();
        if(box.isEmpty()) {
            box = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        SizeF ratio = new SizeF((float)box.width() / frameSize.getWidth(), (float)box.height() / frameSize.getHeight());

        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas.drawLine(
                points[0].x * ratio.getWidth(),
                points[0].y * ratio.getHeight(),
                points[1].x * ratio.getWidth(),
                points[1].y * ratio.getHeight(),
                paint);
        canvas.drawLine(
                points[1].x * ratio.getWidth(),
                points[1].y * ratio.getHeight(),
                points[2].x * ratio.getWidth(),
                points[2].y * ratio.getHeight(),
                paint);
        canvas.drawLine(
                points[2].x * ratio.getWidth(),
                points[2].y * ratio.getHeight(),
                points[3].x * ratio.getWidth(),
                points[3].y * ratio.getHeight(),
                paint);
        canvas.drawLine(
                points[3].x * ratio.getWidth(),
                points[3].y * ratio.getHeight(),
                points[0].x * ratio.getWidth(),
                points[0].y * ratio.getHeight(),
                paint);

    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        this.filter = colorFilter;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
