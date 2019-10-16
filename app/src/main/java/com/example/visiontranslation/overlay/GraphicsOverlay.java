package com.example.visiontranslation.overlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewOverlay;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.visiontranslation.ui.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GraphicsOverlay  {


    private View view; // whic view to overlay
    private ViewOverlay overlay;
    private HashSet<Drawable> array;

    public GraphicsOverlay(View view) {
        this.view = view;
        array = new HashSet<>();
        overlay = view.getOverlay();
    }

    public synchronized void add(Drawable drawable) {
        array.add(drawable);
        overlay.add(drawable);
        update();
    }

    public synchronized void remove(Drawable drawable) {
        array.remove(drawable);
        overlay.remove(drawable);
        update();
    }


    public synchronized void add(@NonNull List<? extends Drawable> drawableList) {
        for(Drawable drawable : drawableList) {
            if (drawable == null) {
                continue;
            }

            array.add(drawable);
            overlay.add(drawable);
        }
        update();
    }

    public synchronized void remove(@NonNull List<? extends Drawable> drawableList) {
        for(Drawable drawable : drawableList) {
            if (drawable == null) {
                continue;
            }
            array.remove(drawable);
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
