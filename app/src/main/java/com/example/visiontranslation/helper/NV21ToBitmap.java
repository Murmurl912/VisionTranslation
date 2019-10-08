package com.example.visiontranslation.helper;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class NV21ToBitmap {
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB scriptIntrinsicYuvToRGB;
    private Allocation in, out;
    private Type.Builder yuv, rgba;

    public NV21ToBitmap(Context context) {
        rs = RenderScript.create(context);
        scriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

    }

    public Bitmap nv21ToBitmap(byte[] data, int width, int height) {
        if(data != null) {
            yuv = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
            in = Allocation.createTyped(rs, yuv.create(), Allocation.USAGE_SCRIPT);

            rgba = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgba.create(), Allocation.USAGE_SCRIPT);

            in.copyFrom(data);
            scriptIntrinsicYuvToRGB.setInput(in);
            scriptIntrinsicYuvToRGB.forEach(out);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            out.copyTo(bitmap);

            return bitmap;
        }
        return null;
    }
}
