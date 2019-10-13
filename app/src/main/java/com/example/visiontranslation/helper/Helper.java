package com.example.visiontranslation.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.visiontranslation.detector.text.Block;
import com.google.android.gms.vision.L;
import com.google.android.gms.vision.text.TextBlock;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.List;
import java.util.Locale;

public class Helper {

    public static String[] languages = new String[] {
            "Auto",
            "Chinese",
            "English",
            "Korean",
            "French",
            "Spanish",
            "Russian",
            "German",
            "Italian",
            "Swedish",
            "Chinese Tradition"};
    public static Locale[] locales = new Locale[] {
            Locale.getDefault(),
            Locale.UK,
            Locale.KOREA,
            Locale.FRANCE,
            new Locale("Spanish", "ES"),
            new Locale("Russian", "RU"),
            new Locale("German", "DE"),
            new Locale("Italian", "IT"),
            new Locale("Swedish", "SE"),
            Locale.TRADITIONAL_CHINESE
    };



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
            case "Auto": { return Locale.getDefault();}
            case "Chinese": {return Locale.CHINA;}
            case "English": {return Locale.UK;}
            case "Korean": {return Locale.KOREA;}
            case "French": {return Locale.FRANCE;}
            case "Spanish": {return new Locale("Spanish", "ES");}
            case "Russian": {return new Locale("Russian", "RU");}
            case "German": {return new Locale("German", "DE");}
            case "Italian": {return  new Locale("Italian", "IT");}
            case "Swedish": {return new Locale("Swedish", "SE");}
            case "Chinese Tradition":{return Locale.TRADITIONAL_CHINESE;}
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
}
