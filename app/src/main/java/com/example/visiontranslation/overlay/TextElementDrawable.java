package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.helper.Helper;
import com.example.visiontranslation.translation.GoogleTranslationService;
import com.google.android.gms.vision.text.Element;

public class TextElementDrawable extends Drawable {
    private Element element;
    private Paint paint;
    private int color;
    private boolean isSelected;
    private PointF ratio;
    private PointF[] pointFS;
    private boolean isInitialize = false;
    private float angle = 0;
    private int fontSize = -1;
    private String value;

    public TextElementDrawable(@NonNull Element element, PointF ratio, String translated) {
        this.element = element;
        this.ratio = ratio;
        this.paint = new Paint();
        this.isSelected = false;
        this.color = Color.GREEN;

        if(translated == null || translated.equals("")) {
            value = element.getValue();
        } else {
            value = translated;
        }
    }

    private void initialize() {
        if(isInitialize) {
            return;
        }

        Point[] cornerPoints = element.getCornerPoints();
        pointFS = new PointF[]{
                new PointF(cornerPoints[0].x * ratio.x, cornerPoints[0].y * ratio.y),
                new PointF(cornerPoints[1].x * ratio.x, cornerPoints[1].y * ratio.y),
                new PointF(cornerPoints[2].x * ratio.x, cornerPoints[2].y * ratio.y),
                new PointF(cornerPoints[3].x * ratio.x, cornerPoints[3].y * ratio.y),
        };
        double a = (pointFS[2].x - pointFS[3].x) * 1e6;
        double b = (pointFS[2].y - pointFS[3].y) * 1e6;
        angle = (float)Math.toDegrees(Math.atan(b / a));

        if(fontSize < 0) {
            int textHeight = (int)Math.sqrt(
                    Math.pow(
                            (pointFS[0].x - pointFS[3].x) * ratio.x,
                            2
                    ) +
                            Math.pow(
                                    (pointFS[0].y - pointFS[3].y) * ratio.y,
                                    2
                            )
            );
            fontSize = Helper.dpToSp(textHeight, VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
            );
            paint.setTextSize(fontSize);
            // paint.setTextSize(48);
        }
        paint.setTextSize(fontSize);

        isInitialize = true;
    }

    public boolean contain(PointF point) {
        return Helper.isPolygonContainsPoint(pointFS, point);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if(selected) {
            this.color = Color.RED;
        } else {
            this.color = Color.GREEN;
        }
    }

    public String getValue() {
        return element.getValue();
    }

    public String getLanguage() {
        return element.getLanguage();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setRatio(PointF ratio) {
        this.ratio = ratio;
    }

    public PointF getRatio() {
        return ratio;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        initialize();
        paint.setStrokeWidth(3);
        paint.setColor(Color.parseColor("#80000000"));



        Path path = new Path();
        path.moveTo(pointFS[0].x, pointFS[0].y);
        path.lineTo(pointFS[1].x, pointFS[1].y);
        path.lineTo(pointFS[2].x, pointFS[2].y);
        path.lineTo(pointFS[3].x, pointFS[3].y);
        path.lineTo(pointFS[0].x, pointFS[0].y);
        canvas.drawPath(path, paint);


        /*
         canvas.drawLine(
                pointFS[0].x,
                pointFS[0].y,
                pointFS[1].x,
                pointFS[1].y,
                paint);
        canvas.drawLine(
                pointFS[1].x,
                pointFS[1].y,
                pointFS[2].x,
                pointFS[2].y,
                paint);
        canvas.drawLine(
                pointFS[2].x,
                pointFS[2].y,
                pointFS[3].x,
                pointFS[3].y,
                paint);
        canvas.drawLine(
                pointFS[3].x,
                pointFS[3].y,
                pointFS[0].x,
                pointFS[0].y,
                paint);
         */

        canvas.rotate(angle, pointFS[3].x, pointFS[3].y);
        paint.setColor(Color.WHITE);
        canvas.drawText(value, pointFS[3].x, pointFS[3].y, paint);
        canvas.rotate(-angle, pointFS[3].x, pointFS[3].y);

    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
