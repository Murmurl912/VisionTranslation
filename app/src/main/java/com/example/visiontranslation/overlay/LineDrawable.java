package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Size;
import android.util.SizeF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.VisionTranslationApplication;
import com.example.visiontranslation.helper.Helper;
import com.google.android.gms.vision.text.Line;

public class LineDrawable extends Drawable {

    private Paint paint;
    private Line line;
    private Size frameSize;
    private Rect lineBox;
    private String value;
    private String language;
    private Point[] points;
    private float fontSize;

    public LineDrawable(@NonNull Line line, @NonNull Size frameSize) {
        this.line = line;
        this.frameSize = frameSize;
        this.lineBox = line.getBoundingBox();
        this.paint = new Paint();
        this.value = line.getValue();
        this.language = line.getLanguage();
        this.points = line.getCornerPoints();
        fontSize = -1;
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect box = canvas.getClipBounds();
        if(box.isEmpty()) {
            box = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        SizeF ratio = new SizeF((float)box.width() / frameSize.getWidth(), (float)box.height() / frameSize.getHeight());

        if(fontSize < 0) {
            fontSize = Helper.dpToSp(
                    (int)Math.sqrt(
                            Math.pow(
                                    (points[0].x - points[3].x) * ratio.getWidth(),
                                    2
                            ) +
                                    Math.pow(
                                            (points[0].y - points[3].y) * ratio.getHeight(),
                                            2
                                    )
                    ),
                    VisionTranslationApplication.getVisionTranslationApplication().getApplicationContext()
            );
            paint.setTextSize(fontSize);
        }

        canvas.rotate(line.getAngle());
        paint.setColor(Color.BLACK);
        canvas.drawRect(
                lineBox.left * ratio.getWidth(),
                lineBox.top * ratio.getHeight(),
                lineBox.right * ratio.getWidth(),
                lineBox.bottom * ratio.getHeight(),
                paint
        );
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawText(
                value,
                lineBox.left * ratio.getWidth(),
                lineBox.bottom * ratio.getHeight(),
                paint
        );

        canvas.rotate(-line.getAngle());
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
