package com.example.visiontranslation.overlay;

import android.graphics.drawable.Drawable;

public abstract class OverlayDrawable extends Drawable {
    protected long id;
    private static long baseline = 0;

    public OverlayDrawable() {
        baseline++;
        id = baseline;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OverlayDrawable that = (OverlayDrawable) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
