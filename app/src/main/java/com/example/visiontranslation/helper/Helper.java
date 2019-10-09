package com.example.visiontranslation.helper;

import android.content.Context;
import android.util.Size;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.example.visiontranslation.detector.text.Block;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;

public class Helper {
    public static Block convertTextBlockToBlock(TextBlock block, Size size) {
        Block cBlock;

        block.getBoundingBox();

        return null;
    }

    public List<Block> convertTextBlocksToBlocks(List<TextBlock> blocks, Size size) {
        return null;
    }

    public static int dpToPx(float dp, @NonNull Context context) {
        return (int)
                TypedValue
                        .applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                dp,
                                context.getResources().getDisplayMetrics()
                        );
    }

    public static int spToPx(float sp, @NonNull Context context) {
        return (int)
                TypedValue
                        .applyDimension(
                                TypedValue.COMPLEX_UNIT_SP,
                                sp,
                                context.getResources().getDisplayMetrics()
                        );
    }

    public static int dpToSp(float dp, @NonNull Context context) {
        return (int) (dpToPx(dp, context) / context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static float pxToSp(int px, Context context) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }
}
