package com.example.visiontranslation.ui.camera;

import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.visiontranslation.overlay.FirebaseElementDrawable;
import com.example.visiontranslation.overlay.FirebaseLineDrawable;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.vision.VisionResultProcessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.List;

public class VisionFirebasePausedFrameTextResultProcessor
        implements VisionResultProcessor<Task<FirebaseVisionText>>, View.OnTouchListener {

    private GraphicsOverlay overlay;
    private List<Drawable> drawables;
    private View view;
    private PointF pointF = new PointF(-1, -1);
    private List<FirebaseElementDrawable> selected;

    private final Object lock = new Object();
    private TextSelectionListener listener;
    private String separator = " ";
    private boolean isSelecting = false;
    private List<FirebaseElementDrawable> localSelection;

    public VisionFirebasePausedFrameTextResultProcessor(View view) {
        overlay = new GraphicsOverlay(view);
        drawables = new ArrayList<>();
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
                FirebaseElementDrawable elementDrawable = (FirebaseElementDrawable)drawable;
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
                FirebaseElementDrawable pre = selected.get(i);
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
            for(FirebaseElementDrawable drawable : selected) {
                builder.append(drawable.getValue()).append(separator);
            }
            return builder.toString();
        }
    }

    public void clearSelection() {
        synchronized (lock) {
            for(FirebaseElementDrawable drawable : selected) {
                drawable.setSelected(false);
            }
            selected.clear();
            localSelection.clear();
            overlay.update();
        }
    }

    public void setTextSelectionListener(@NonNull TextSelectionListener textSelectionListener) {
        this.listener = textSelectionListener;
    }

    @Override
    public void onResult(@NonNull Task<FirebaseVisionText> task, @NonNull Size frameSize) {


        task.addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
            @Override
            public void onComplete(@NonNull Task<FirebaseVisionText> task) {
                overlay.remove(drawables);
                drawables.clear();

                if(task.isSuccessful()) {
                    process(task, frameSize);
                }
            }
        });

    }

    private void process(Task<FirebaseVisionText> task, Size frameSize) {
        if(!task.isSuccessful()) {
            return;
        }
        FirebaseVisionText firebaseVisionText = task.getResult();
        if(firebaseVisionText == null) {
            return;
        }

        PointF ratio = new PointF(
                (float)view.getWidth() / frameSize.getWidth(),
                (float)view.getHeight() / frameSize.getHeight()
        );
        overlay.remove(drawables);
        drawables.clear();

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        for(FirebaseVisionText.TextBlock block : blocks) {
            List<FirebaseVisionText.Line> lines = block.getLines();
            for(FirebaseVisionText.Line line : lines) {
                List<FirebaseVisionText.Element> elements = line.getElements();
                for(FirebaseVisionText.Element element : elements) {
                    FirebaseElementDrawable drawable = new FirebaseElementDrawable(element, ratio);
                    drawables.add(drawable);
                }
            }
        }
        overlay.add(drawables);
        if(listener != null) {
            listener.onSelectionChanged("");
        }
    }
}
