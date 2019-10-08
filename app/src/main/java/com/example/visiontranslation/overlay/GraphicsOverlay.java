package com.example.visiontranslation.overlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewOverlay;

import androidx.fragment.app.FragmentActivity;

import com.example.visiontranslation.ui.MainActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GraphicsOverlay  {


    private View view; // whic view to overlay
    private Context context;
    private ViewOverlay overlay;

    private LongSparseArray<OverlayDrawable> array;


    public GraphicsOverlay(View view, Context context) {
        this.view = view;
        this.context = context;
        array = new LongSparseArray<>();
        overlay = view.getOverlay();
    }

    public synchronized void add(OverlayDrawable drawable) {
        array.append(drawable.id, drawable);
        overlay.add(drawable);
        update();
    }

    public synchronized void remove(OverlayDrawable drawable) {
        array.remove(drawable.id);
        overlay.remove(drawable);
        update();

    }


    public synchronized void remove(long id) {
        Drawable drawable = array.get(id, null);
        if(drawable == null) {
            return;
        }
        overlay.remove(drawable);
        array.remove(id);
        update();
    }

    public OverlayDrawable get(long id) {
        return array.get(id, null);
    }

    public synchronized void add(List<? extends OverlayDrawable> drawableList) {
        for(OverlayDrawable drawable : drawableList) {
            if (drawable == null) {
                continue;
            }
            array.append(drawable.id, drawable);
            overlay.add(drawable);
        }
        update();
    }

    public synchronized void remove(List<? extends OverlayDrawable> drawableList) {
        for(OverlayDrawable drawable : drawableList) {
            if (drawable == null) {
                continue;
            }
            array.remove(drawable.id);
            overlay.remove(drawable);
        }
        update();
    }


    public void clear() {
        array.clear();
        overlay.clear();
        update();
    }

    public void update()  {
        view.getHandler().post(()->{
            view.invalidate();

        });
    }

}
