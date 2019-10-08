package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SizeF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextBlockDrawable extends Drawable {

    private TextBlock block;
    private List<? extends Text> lines;

    private SizeF ratio;

    private Paint fill;
    private Paint stroke;
    private Paint foreground;

    private boolean isDecoded;
    private Rect blockBounding;
    private Point[] blockPoints;
    private List<Rect> lineBoundings;
    private List<Point[]> linePoints;
    private List<String> lineTexts;

    private Map<Line, List<Rect>> wordBoundings;

    private TextBlockDrawable(TextBlock block, SizeF ratio) {
        this.block = block;
        this.ratio = ratio;

        fill = new Paint();
        stroke = new Paint();
        foreground = new Paint();

        fill.setStyle(Paint.Style.FILL);
        fill.setColor(Color.parseColor("#88888888"));

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(4);
        stroke.setColor(Color.parseColor("#FF0000"));

        foreground.setColor(Color.parseColor("#FFFFFF"));
        isDecoded = false;
    }

    public static TextBlockDrawable build(@NonNull TextBlock block, @NonNull SizeF ratio) {
        return new TextBlockDrawable(block, ratio);
    }

    private void decode() {

        isDecoded = false;
        blockBounding = block.getBoundingBox();
        /**
         * blockBounding.set(
         *                 (int)(blockBounding.left * ratio.getWidth()),
         *                 (int)(blockBounding.top * ratio.getHeight()),
         *                 (int)(blockBounding.right * ratio.getWidth()),
         *                 (int)(blockBounding.bottom * ratio.getHeight())
         *         );
         */

        blockPoints = block.getCornerPoints();

        /**
         * for(Point point : blockPoints) {
         *             point.set((int)(point.x * ratio.getWidth()), (int)(point.y * ratio.getHeight()));
         *         }
         */



        lines = block.getComponents();
        lineBoundings = new ArrayList<>();
        linePoints = new ArrayList<>();
        lineTexts = new ArrayList<>();

        for(Text line : lines) {
            Rect box = line.getBoundingBox();
            Point[] points = line.getCornerPoints();
            /**
             *  box.set(
             *                     (int)(box.left * ratio.getWidth()),
             *                     (int)(box.top * ratio.getHeight()),
             *                     (int)(box.right * ratio.getWidth()),
             *                     (int)(box.bottom * ratio.getHeight())
             *             );
             */

            lineBoundings.add(box);

            /**
             * for(Point p : points) {
             *                 p.set((int)(p.x * ratio.getWidth()), (int)(p.y * ratio.getHeight()));
             *             }
             */

            linePoints.add(points);
            lineTexts.add(line.getValue());
        }
        isDecoded = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(!isDecoded) {
            decode();
        }

        Rect rect = canvas.getClipBounds();
        if(rect.isEmpty()) {
            rect.set(0, 0, canvas.getWidth(), canvas.getHeight());
        }


        canvas.drawRect(
                blockBounding.left * rect.width() / ratio.getWidth(),
                blockBounding.top * rect.height() / ratio.getHeight(),
                blockBounding.right * rect.width() / ratio.getWidth(),
                blockBounding.bottom * rect.height() / ratio.getHeight(),
                fill);
        canvas.drawRect(
                blockBounding.left * rect.width() / ratio.getWidth(),
                blockBounding.top * rect.height() / ratio.getHeight(),
                blockBounding.right * rect.width() / ratio.getWidth(),
                blockBounding.bottom * rect.height() / ratio.getHeight()
                , stroke);

        canvas.drawLine(
                blockPoints[0].x * rect.width() / ratio.getWidth(),
                blockPoints[0].y * rect.height() / ratio.getHeight(),
                blockPoints[1].x * rect.width() / ratio.getWidth(),
                blockPoints[1].y * rect.height() / ratio.getHeight(),
                stroke
        );
        canvas.drawLine(
                blockPoints[1].x * rect.width() / ratio.getWidth(),
                blockPoints[1].y * rect.height() / ratio.getHeight(),
                blockPoints[2].x * rect.width() / ratio.getWidth(),
                blockPoints[2].y * rect.height() / ratio.getHeight(),
                stroke
        );
        canvas.drawLine(
                blockPoints[2].x * rect.width() / ratio.getWidth(),
                blockPoints[2].y * rect.height() / ratio.getHeight(),
                blockPoints[3].x * rect.width() / ratio.getWidth(),
                blockPoints[3].y * rect.height() / ratio.getHeight(),
                stroke
        );
        canvas.drawLine(
                blockPoints[3].x * rect.width() / ratio.getWidth(),
                blockPoints[3].y * rect.height() / ratio.getHeight(),
                blockPoints[0].x * rect.width() / ratio.getWidth(),
                blockPoints[0].y * rect.height() / ratio.getHeight(),
                stroke
        );
        
        for(int i = 0; i < lineBoundings.size(); i++) {
            Rect r = lineBoundings.get(i);
            String s = lineTexts.get(i);
            canvas.drawText(
                    s,
                    0,
                    s.length(),
                    r.left * rect.width() / ratio.getWidth(),
                    r.bottom * rect.height() / ratio.getHeight(),
                    foreground
            );
        }
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
