package com.example.visiontranslation.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.List;
import java.util.Locale;

public class Helper {

    public static String[] languages = new String[] {
            "Chinese",
            "Japanese",
            "English",
            "Korean",
            "French",
            "Spanish",
            "Russian",
            "German",
            "Italian"
    };

    public static Locale[] locales = new Locale[] {
            Locale.getDefault(),
            Locale.JAPAN,
            Locale.UK,
            Locale.KOREA,
            Locale.FRANCE,
            new Locale("Spanish", "ES"),
            new Locale("Russian", "RU"),
            new Locale("German", "DE"),
            new Locale("Italian", "IT"),
    };


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

    public static String escapeSingleQuote(String input) {
        StringBuilder output = new StringBuilder();
        if(input == null || input.equals("")) {
            return output.toString();
        }
        char[] chars = input.toCharArray();
        for(char c : chars) {
            if(c == '\'') {
                output.append('\\').append(c);
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    public static void hideSoftKeyboard(@Nullable Activity activity) {
        if (activity != null) {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static Locale getLocaleByLanguage(String language) {
        switch (language) {
            case "Chinese": {return Locale.CHINA;}
            case "Japanese": {return Locale.JAPAN;}
            case "English": {return Locale.UK;}
            case "Korean": {return Locale.KOREA;}
            case "French": {return Locale.FRANCE;}
            case "Spanish": {return new Locale("Spanish", "ES");}
            case "Russian": {return new Locale("Russian", "RU");}
            case "German": {return new Locale("German", "DE");}
            case "Italian": {return  new Locale("Italian", "IT");}
            default: return Locale.getDefault();
        }
    }

    private static boolean isOpenCVLoaded = false;

    public static void loadOpenCV(LoaderCallbackInterface callback) {
        if(isOpenCVLoaded) {
            return;
        }

        Thread thread = new Thread(()->{
            if(OpenCVLoader.initDebug()) {
                callback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                isOpenCVLoaded = true;
            } else {
                callback.onManagerConnected(LoaderCallbackInterface.INIT_FAILED);
            }
        });
        thread.start();
    }

    public static boolean isPolygonContainsPoint(PointF[] mPoints, PointF point) {
        int nCross = 0;
        for (int i = 0; i < mPoints.length; i++) {
            PointF p1 = mPoints[i];
            PointF p2 = mPoints[(i + 1) % mPoints.length];
            // 取多边形任意一个边,做点point的水平延长线,求解与当前边的交点个数
            // p1p2是水平线段,要么没有交点,要么有无限个交点
            if (p1.y == p2.y)
                continue;
            // point 在p1p2 底部 --> 无交点
            if (point.y < Math.min(p1.y, p2.y))
                continue;
            // point 在p1p2 顶部 --> 无交点
            if (point.y >= Math.max(p1.y, p2.y))
                continue;
            // 求解 point点水平线与当前p1p2边的交点的 X 坐标
            double x = (point.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x;
            if (x > point.x) // 当x=point.x时,说明point在p1p2线段上
                nCross++; // 只统计单边交点
        }
        // 单边交点为偶数，点在多边形之外 ---
        return (nCross % 2 == 1);
    }
}

