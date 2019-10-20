package com.example.visiontranslation.ui.camera;

import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Size;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.visiontranslation.overlay.ElementDrawable;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.vision.VisionResultProcessor;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;

public class PausedFrameVisionTextResultProcessor implements
        VisionResultProcessor<SparseArray<TextBlock>>, View.OnTouchListener{

    private GraphicsOverlay overlay;
    private List<Drawable> drawables;
    private List<Element> elements;
    private View view;
    private PointF pointF = new PointF(-1, -1);
    private List<ElementDrawable> selected;

    private final Object lock = new Object();
    private TextSelectionListener listener;
    private String separator = " ";
    private boolean isSelecting = false;
    private List<ElementDrawable> localSelection;

    public PausedFrameVisionTextResultProcessor(View view) {
        overlay = new GraphicsOverlay(view);
        drawables = new ArrayList<>();
        elements = new ArrayList<>();
        this.view = view;
        view.setOnTouchListener(this);
        selected = new ArrayList<>();
        localSelection = new ArrayList<>();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        pointF = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                testSelected(pointF);
            } break;

            case MotionEvent.ACTION_MOVE: {
                testSelected(pointF);
            } break;

            case MotionEvent.ACTION_UP: {
                mergeSelection();
                if(listener != null) {
                    listener.onSelectionChanged(getSelection());
                }

            } break;

            case MotionEvent.ACTION_CANCEL: {

            } break;
        }
        overlay.update();
        return false;
    }

    private void testSelected(PointF pointF) {
        synchronized (lock) {
            for(Drawable drawable : drawables) {
                ElementDrawable elementDrawable = (ElementDrawable)drawable;
                if(elementDrawable.contain(pointF)
                        && !localSelection.contains(elementDrawable)) {
                    localSelection.add(elementDrawable);
                    elementDrawable.setSelected(true);
                    break;
                };
            }
        }
    }

    private void mergeSelection() {
        synchronized (lock) {
            for(int i = 0; i < selected.size(); i++) {
                ElementDrawable pre = selected.get(i);
                if(localSelection.contains(pre)) {
                    localSelection.remove(pre);
                    pre.setSelected(false);
                    selected.remove(i);
                    i--;
                }

            }

            selected.addAll(localSelection);
            localSelection.clear();
            overlay.update();
        }
    }
    private String getSelection() {
        synchronized (lock) {
            StringBuilder builder = new StringBuilder();
            for(ElementDrawable drawable : selected) {
                builder.append(drawable.getValue()).append(separator);
            }
            return builder.toString();
        }
    }

    public void clearSelection() {
        synchronized (lock) {
            for(ElementDrawable drawable : selected) {
                drawable.setSelected(false);
            }
            selected.clear();
            localSelection.clear();
        }
    }

    @Override
    public void onResult(@NonNull SparseArray<TextBlock> result, @NonNull Size frameSize) {
        PointF ratio = new PointF(
                (float)view.getWidth() / frameSize.getWidth(),
                (float)view.getHeight() / frameSize.getHeight()
        );
        overlay.remove(drawables);
        drawables.clear();
        for(int i = 0; i < result.size(); i++) {
            TextBlock block = result.get(i);
            for(Text textLine : block.getComponents()) {
                Line line = (Line)textLine;
                for(Text textElement : line.getComponents()) {
                    Element element = (Element)textElement;
                    Drawable drawable = new ElementDrawable(element, ratio);
                    drawables.add(drawable);
                }
            }
        }
        overlay.add(drawables);
        if(listener != null) {
            listener.onSelectionChanged("");
        }
    }

    public void setTextSelectionListener(@NonNull TextSelectionListener textSelectionListener) {
        this.listener = textSelectionListener;
    }

    public interface TextSelectionListener {
        public void onSelectionChanged(@NonNull String text);
    }
}

