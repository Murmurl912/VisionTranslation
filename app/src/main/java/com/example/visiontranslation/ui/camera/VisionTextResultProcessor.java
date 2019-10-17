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
import com.example.visiontranslation.vision.VisionResultProcessor;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.TextBlock;
import com.otaliastudios.cameraview.CameraView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VisionTextResultProcessor implements VisionResultProcessor<SparseArray<TextBlock>>, View.OnTouchListener {

    private View view;
    private CameraView cameraView;
    private ImageView waterMark;
    private EditText editText;
    private GraphicsOverlay overlay;
    private List<Element> elements;
    private List<Drawable> elementDrawables;
    private Set<Drawable> selectedDrawable;

    public VisionTextResultProcessor(View view) {
        this.view = view;
        elements = new ArrayList<>();
        elementDrawables = new ArrayList<>();

        bindView();
        bindListener();
    }

    private void bindView() {
        cameraView = view.findViewById(R.id.main_camera_view);
        waterMark = view.findViewById(R.id.main_camera_view_water_mark);
        editText = view.findViewById(R.id.main_camera_source_edit_text);
        overlay = new GraphicsOverlay(waterMark);
    }

    private void bindListener() {

    }

    @Override
    public void onResult(@NonNull SparseArray<TextBlock> result, @NonNull Size processSize) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
