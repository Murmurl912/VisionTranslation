package com.example.visiontranslation.ui.camera;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.overlay.GraphicsOverlay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraViewOverlayManager {

    private GraphicsOverlay overlay;

    private Map<String, List<Drawable>> drawableMaps;
    private Map<String, Drawable> drawableMap;


    protected CameraViewOverlayManager() {
        drawableMap = new HashMap<>();
        drawableMaps = new HashMap<>();
    }

    public void put(@NonNull String key, @NonNull Drawable drawable) {
        drawableMap.put(key, drawable);
    }

    public void putList(@NonNull String key, @NonNull List<Drawable> drawables) {
        drawableMaps.put(key, drawables);
    }

    public void remove(@NonNull String key) {
        drawableMap.remove(key);
    }

    public void removeList(@NonNull String key) {
        drawableMaps.remove(key);
    }

    @Nullable
    public Drawable get(@NonNull String key) {
        return drawableMap.get(key);
    }

    @Nullable
    public List<Drawable> getList(@NonNull String key) {
        return drawableMaps.get(key);
    }

    public void clear() {
        drawableMap.clear();
    }

    public void clearList() {
        drawableMaps.clear();
    }
}

