package com.example.visiontranslation.ui.camera;

import android.graphics.drawable.Drawable;
import android.util.Size;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.visiontranslation.R;
import com.example.visiontranslation.overlay.GraphicsOverlay;
import com.example.visiontranslation.overlay.LineDrawable;
import com.example.visiontranslation.ui.MainActivity;
import com.example.visiontranslation.vision.VisionResultProcessor;
import com.google.android.gms.vision.L;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.otaliastudios.cameraview.CameraView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VisionTextResultProcessor implements
        VisionResultProcessor<SparseArray<TextBlock>>, View.OnTouchListener {

    private GraphicsOverlay overlay;
    private List<Drawable> drawables;
    private MainActivity activity;
    private TextRecognitionListener listener;

    public VisionTextResultProcessor(View view, MainActivity activity) {
        drawables = new ArrayList<>();
        overlay = new GraphicsOverlay(view);
        this.activity = activity;
    }

    @Override
    public void onResult(@NonNull SparseArray<TextBlock> result, @NonNull Size processSize) {
        overlay.remove(drawables);
        drawables.clear();
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < result.size(); i++) {
            TextBlock block = result.get(i);
            if(block.getLanguage().equals("und")) {
                break;
            }

            for(Text textLine : block.getComponents()) {

                builder.append(textLine.getValue());
                builder.append("\n");
                LineDrawable drawable = new LineDrawable((Line)textLine, processSize, activity.getTargetLanguage());
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public interface TextRecognitionListener {
        public void onText(String text);
    }
}
