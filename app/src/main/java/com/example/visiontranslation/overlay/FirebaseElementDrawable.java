package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.helper.Helper;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.util.List;

public class FirebaseElementDrawable extends Drawable {
    private FirebaseVisionText.Element element;
    private Paint paint;
    private int color;
    private boolean isSelected;
    private PointF ratio;
    private PointF[] pointFS;
    private boolean isInitialize = false;

    public FirebaseElementDrawable(@NonNull FirebaseVisionText.Element element, PointF ratio) {
        this.element = element;
        this.ratio = ratio;
        this.paint = new Paint();
        this.isSelected = false;
        this.color = Color.GREEN;

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
        return element.getText();
    }

    public List<RecognizedLanguage> getLanguage() {
        return element.getRecognizedLanguages();
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
        paint.setColor(color);
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
