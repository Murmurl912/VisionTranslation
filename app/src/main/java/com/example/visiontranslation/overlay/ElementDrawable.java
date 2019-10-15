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

import com.example.visiontranslation.helper.Helper;
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
    private Size canvasSize;
    private SizeF ratio;

    public ElementDrawable(@NonNull Element element, @NonNull Size frameSize, @NonNull Size viewSize) {
        this.element = element;
        this.frameSize = frameSize;
        this.points = element.getCornerPoints();
        this.rect = element.getBoundingBox();
        this.paint = new Paint();
        this.isSelected = false;
        this.color = Color.GREEN;
        this.ratio = new SizeF((float)viewSize.getWidth() / frameSize.getWidth(), (float)viewSize.getHeight() / frameSize.getHeight());
        this.points = new Point[]{
                new Point((int)(points[0].x * ratio.getWidth()), (int)(points[0].y * ratio.getHeight())),
                new Point((int)(points[1].x * ratio.getWidth()), (int)(points[1].y * ratio.getHeight())),
                new Point((int)(points[2].x * ratio.getWidth()), (int)(points[2].y * ratio.getHeight())),
                new Point((int)(points[3].x * ratio.getWidth()), (int)(points[3].y * ratio.getHeight()))
        };
    }


    public boolean contain(Point point) {
        return Helper.isPolygonContainsPoint(points, point);
    }

    public void setSelected(boolean selected) {
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


        paint.setStrokeWidth(3);
        paint.setColor(color);
        canvas.drawLine(
                points[0].x,
                points[0].y,
                points[1].x,
                points[1].y,
                paint);
        canvas.drawLine(
                points[1].x,
                points[1].y,
                points[2].x,
                points[2].y,
                paint);
        canvas.drawLine(
                points[2].x,
                points[2].y,
                points[3].x,
                points[3].y,
                paint);
        canvas.drawLine(
                points[3].x,
                points[3].y,
                points[0].x,
                points[0].y,
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
