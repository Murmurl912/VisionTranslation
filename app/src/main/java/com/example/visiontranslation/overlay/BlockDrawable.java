package com.example.visiontranslation.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;

public class BlockDrawable extends Drawable {

    private Size frameSize;
    private TextBlock textBlock;
    private Paint background;

    public BlockDrawable(TextBlock block, Size frameSize) {
        this.textBlock = block;
        this.frameSize = frameSize;

        background = new Paint();
        background.setColor(Color.parseColor("#88ffffff"));
        background.setStyle(Paint.Style.FILL);
    }

    private void decode() {
        Rect textBlockBoundingBox = textBlock.getBoundingBox();
        Point[] textBlockCornerPoints = textBlock.getCornerPoints();

        List<? extends Text> lines = textBlock.getComponents();
        for(Text text : lines) {
            Line line = (Line)text;
            List<? extends Text> texts = line.getComponents();
            for(Text t : texts) {
                Element element = (Element)t;

            }
        }
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect rect = canvas.getClipBounds();
        Line line;
        Rect textBlockBoundingBox = textBlock.getBoundingBox();
        canvas.drawRect(
                textBlockBoundingBox.left * rect.width() / (float)frameSize.getWidth(),
                textBlockBoundingBox.top * rect.height() / (float)frameSize.getHeight(),
                textBlockBoundingBox.right * rect.width() / (float)frameSize.getWidth(),
                textBlockBoundingBox.bottom * rect.height() / (float)frameSize.getHeight(),
                background
        );

        background.setColor(Color.parseColor("#ff0000"));
        background.setStyle(Paint.Style.STROKE);
        background.setStrokeWidth(5);
        canvas.drawRect(
                textBlockBoundingBox.left * rect.width() / (float)frameSize.getWidth(),
                textBlockBoundingBox.top * rect.height() / (float)frameSize.getHeight(),
                textBlockBoundingBox.right * rect.width() / (float)frameSize.getWidth(),
                textBlockBoundingBox.bottom * rect.height() / (float)frameSize.getHeight(),
                background
        );
        background.setColor(Color.parseColor("#00ff00"));
        Point[] textBlockCornerPoints = textBlock.getCornerPoints();
        canvas.drawLine(
                textBlockCornerPoints[0].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[0].y * rect.height() / (float) frameSize.getHeight(),
                textBlockCornerPoints[1].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[1].y * rect.height() / (float) frameSize.getHeight(),
                background
        );
        canvas.drawLine(
                textBlockCornerPoints[1].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[1].y * rect.height() / (float) frameSize.getHeight(),
                textBlockCornerPoints[2].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[2].y * rect.height() / (float) frameSize.getHeight(),
                background
        );
        canvas.drawLine(
                textBlockCornerPoints[2].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[2].y * rect.height() / (float) frameSize.getHeight(),
                textBlockCornerPoints[3].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[3].y * rect.height() / (float) frameSize.getHeight(),
                background
        );
        canvas.drawLine(
                textBlockCornerPoints[3].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[3].y * rect.height() / (float) frameSize.getHeight(),
                textBlockCornerPoints[0].x * rect.width() / (float) frameSize.getWidth(),
                textBlockCornerPoints[0].y * rect.height() / (float) frameSize.getHeight(),
                background
        );

        /*


        List<? extends Text> lines = textBlock.getComponents();
        for(Text text : lines) {
            Line line = (Line)text;
            Rect lineRect = line.getBoundingBox();
            Point[] linePoints = line.getCornerPoints();
            background.setColor(Color.parseColor("#0000ff"));
            canvas.drawRect(
                    lineRect.left * rect.width() / (float)frameSize.getWidth(),
                    lineRect.top * rect.height() / (float)frameSize.getHeight(),
                    lineRect.right * rect.width() / (float)frameSize.getWidth(),
                    lineRect.bottom * rect.width() / (float)frameSize.getHeight(),
                    background
            );

            background.setColor(Color.parseColor("#ffff00"));
            canvas.drawLine(
                    linePoints[0].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[0].y * rect.height() / (float) frameSize.getHeight(),
                    linePoints[1].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[1].y * rect.height() / (float) frameSize.getHeight(),
                    background
            );
            canvas.drawLine(
                    linePoints[1].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[1].y * rect.height() / (float) frameSize.getHeight(),
                    linePoints[2].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[2].y * rect.height() / (float) frameSize.getHeight(),
                    background
            );
            canvas.drawLine(
                    linePoints[2].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[2].y * rect.height() / (float) frameSize.getHeight(),
                    linePoints[3].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[3].y * rect.height() / (float) frameSize.getHeight(),
                    background
            );
            canvas.drawLine(
                    linePoints[3].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[3].y * rect.height() / (float) frameSize.getHeight(),
                    linePoints[0].x * rect.width() / (float) frameSize.getWidth(),
                    linePoints[0].y * rect.height() / (float) frameSize.getHeight(),
                    background
            );

            List<? extends Text> texts = line.getComponents();
            for(Text t : texts) {
                Element element = (Element)t;

                Rect elementBoundingBox = element.getBoundingBox();
                Point[] elementCornerPoints = element.getCornerPoints();

                background.setColor(Color.parseColor("#ff00ff"));
                canvas.drawRect(
                        elementBoundingBox.left * rect.width() / (float)frameSize.getWidth(),
                        elementBoundingBox.top * rect.height() / (float)frameSize.getHeight(),
                        elementBoundingBox.right * rect.width() / (float)frameSize.getWidth(),
                        elementBoundingBox.bottom * rect.width() / (float)frameSize.getHeight(),
                        background
                );

                background.setColor(Color.parseColor("#00ffff"));
                canvas.drawLine(
                        elementCornerPoints[0].x * rect.width() / (float) frameSize.getWidth(),
                        elementCornerPoints[0].y * rect.height() / (float) frameSize.getHeight(),
                        elementCornerPoints[1].x * rect.width() / (float) frameSize.getWidth(),
                        elementCornerPoints[1].y * rect.height() / (float) frameSize.getHeight(),
                        background
                );
                canvas.drawLine(
                        elementCornerPoints[1].x * rect.width() / (float) frameSize.getWidth(),
                        elementCornerPoints[1].y * rect.height() / (float) frameSize.getHeight(),
                        elementCornerPoints[2].x * rect.width() / (float) frameSize.getWidth(),
                        elementCornerPoints[2].y * rect.height() / (float) frameSize.getHeight(),
                        background
                );
                canvas.drawLine(
                        elementCornerPoints[2].x * rect.width() / (float) frameSize.getWidth(),
                        elementCornerPoints[2].y * rect.height() / (float) frameSize.getHeight(),
                        elementCornerPoints[3].x * rect.width() / (float) frameSize.getWidth(),
                        elementCornerPoints[3].y * rect.height() / (float) frameSize.getHeight(),
                        background
                );
                canvas.drawLine(
                        elementCornerPoints[3].x * rect.width() / (float) frameSize.getWidth(),
                        elementCornerPoints[3].y * rect.height() / (float) frameSize.getHeight(),
                        elementCornerPoints[0].x * rect.width() / (float) frameSize.getWidth(),
                        linePoints[0].y * rect.height() / (float) frameSize.getHeight(),
                        background
                );
            }
        }


         */

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
