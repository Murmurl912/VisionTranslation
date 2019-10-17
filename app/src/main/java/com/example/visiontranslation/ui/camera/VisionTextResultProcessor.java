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
    private List<Line> lines;

    public VisionTextResultProcessor(View view) {
        drawables = new ArrayList<>();
        overlay = new GraphicsOverlay(view);
    }

    @Override
    public void onResult(@NonNull SparseArray<TextBlock> result, @NonNull Size processSize) {
        overlay.remove(drawables);
        drawables.clear();
        for(int i = 0; i < result.size(); i++) {
            TextBlock block = result.get(i);
            for(Text textLine : block.getComponents()) {
                LineDrawable drawable = new LineDrawable((Line)textLine, processSize, "");
                drawables.add(drawable);
            }
        }
        overlay.add(drawables);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
