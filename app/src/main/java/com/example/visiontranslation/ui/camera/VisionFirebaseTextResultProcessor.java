package com.example.visiontranslation.ui.camera;

import android.graphics.drawable.Drawable;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.visiontranslation.overlay.FirebaseLineDrawable;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.ui.MainActivity;
import com.example.visiontranslation.vision.VisionResultProcessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.protobuf.Internal;

import java.util.ArrayList;
import java.util.List;

public class VisionFirebaseTextResultProcessor implements VisionResultProcessor<Task<FirebaseVisionText>> {

    private GraphicsOverlay overlay;
    private List<Drawable> drawables;
    private MainActivity activity;
    private TextRecognitionListener listener;

    public VisionFirebaseTextResultProcessor(View view, MainActivity activity) {
        drawables = new ArrayList<>();
        overlay = new GraphicsOverlay(view);
        this.activity = activity;
    }


    @Override
    public void onResult(@NonNull Task<FirebaseVisionText> task, @NonNull Size frameSize) {

        try {
            synchronized (task) {
                Thread thread = Thread.currentThread();
                Thread.currentThread().wait();
                task.addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
                    @Override
                    public void onComplete(@NonNull Task<FirebaseVisionText> task) {
                        overlay.remove(drawables);
                        drawables.clear();

                        if(task.isSuccessful()) {
                            process(task, frameSize);
                        }
                        thread.notify();
                    }
                });
            }

        } catch (InterruptedException e) {

        }

    }

    private void process(Task<FirebaseVisionText> task, Size frameSize) {
        overlay.remove(drawables);
        drawables.clear();
        StringBuilder builder = new StringBuilder();
        if(!task.isSuccessful()) {
            return;
        }
        FirebaseVisionText firebaseVisionText = task.getResult();
        if(firebaseVisionText == null) {
            return;
        }

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        for(FirebaseVisionText.TextBlock block : blocks) {
            List<FirebaseVisionText.Line> lines = block.getLines();
            for(FirebaseVisionText.Line line : lines) {
                builder.append(line.getText()).append("\n");
                FirebaseLineDrawable drawable = new FirebaseLineDrawable(line, frameSize, activity.getTargetLanguage());
                drawables.add(drawable);
            }
            builder.append("\n");

        }

        final String value = builder.toString();
        overlay.add(drawables);
        if(listener != null) {
            listener.onText(value);
        }
    }


    public void setTextRecognitionListener(TextRecognitionListener listener) {
        this.listener = listener;
    }
}
