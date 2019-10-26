package com.example.visiontranslation.ui.camera;

import android.graphics.PointF;
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
import com.example.visiontranslation.overlay.TextElementDrawable;
import com.example.visiontranslation.translation.GoogleTranslationService;
import com.example.visiontranslation.ui.MainActivity;
import com.example.visiontranslation.vision.VisionResultProcessor;
import com.example.visiontranslation.vision.text.VisionFirebaseTextProcessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;

import java.util.ArrayList;
import java.util.List;


public class VisionTextResultProcessor implements
        VisionResultProcessor<SparseArray<TextBlock>>, View.OnTouchListener, MainActivity.OnLanguageChangeListener {

    private GraphicsOverlay overlay;
    private List<Drawable> drawables;
    private MainActivity activity;
    private TextRecognitionListener listener;
    private FirebaseTranslator translator;
    private String source = "English";
    private String target = "Chinese";
    private long patience = 300;

    private final Object translationLock = new Object();
    private View view;

    public VisionTextResultProcessor(View view, MainActivity activity) {
        this.view = view;
        drawables = new ArrayList<>();
        overlay = new GraphicsOverlay(view);
        this.activity = activity;
        activity.addOnLanguageChangeListener(this);
        source = activity.getSourceLanguage();
        target = activity.getTargetLanguage();
        GoogleTranslationService.getTranslator(
                GoogleTranslationService.getCode(source),
                GoogleTranslationService.getCode(target),
                new GoogleTranslationService.TranslatorInitializeListener() {
                    @Override
                    public void onSuccess(@NonNull FirebaseTranslator translator) {
                        VisionTextResultProcessor.this.translator = translator;
                    }

                    @Override
                    public void onRequestModel(FirebaseTranslateRemoteModel source, FirebaseTranslateRemoteModel target) {

                    }
                }
        );
    }


    private String translate(String text) {

        if(translator == null) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
        synchronized (translationLock) {
            try {
                Task<String> task = translator.translate(text);
                task.addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        synchronized (translationLock) {
                            result.append(task.getResult());
                            translationLock.notifyAll();
                        }
                    }
                });
                translationLock.wait();
            } catch (InterruptedException e) {

            }
        }
        return result.toString();
    }

    @Override
    public void onLanguageChange(String source, String target) {
        this.source = source;
        this.target = target;
        translator = null;
        GoogleTranslationService.getTranslator(
                GoogleTranslationService.getCode(source),
                GoogleTranslationService.getCode(target),
                new GoogleTranslationService.TranslatorInitializeListener() {
                    @Override
                    public void onSuccess(@NonNull FirebaseTranslator translator) {
                        VisionTextResultProcessor.this.translator = translator;
                    }

                    @Override
                    public void onRequestModel(FirebaseTranslateRemoteModel source, FirebaseTranslateRemoteModel target) {

                    }
                }
        );
    }

    @Override
    public void onResult(@NonNull SparseArray<TextBlock> result, @NonNull Size processSize) {
        processLine(result, processSize);
    }

    private void processLine(SparseArray<TextBlock> result, Size processSize) {
        overlay.remove(drawables);
        drawables.clear();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < result.size(); i++) {
            TextBlock block = result.get(i);
            for(Text textLine : block.getComponents()) {
                builder.append(textLine.getValue());
                builder.append("\n");
                LineDrawable lineDrawable = new LineDrawable((Line)textLine, processSize, source, target);
                drawables.add(lineDrawable);
            }
        }
        builder.append("\n");

        final String value = builder.toString();
        overlay.add(drawables);
        if(listener != null) {
            listener.onText(value);
        }
    }

    private void processElement(SparseArray<TextBlock> result, Size processSize) {
        overlay.remove(drawables);
        drawables.clear();
        StringBuilder builder = new StringBuilder();
        PointF ratio = new PointF(
                (float)view.getWidth() / processSize.getWidth(),
                (float)view.getHeight() / processSize.getHeight()
        );

        for(int i = 0; i < result.size(); i++) {
            TextBlock block = result.get(i);

            for(Text textLine : block.getComponents()) {
                builder.append(textLine.getValue());
                builder.append("\n");

                for(Text elementText : textLine.getComponents()) {
                    TextElementDrawable drawable = new TextElementDrawable(
                            (Element)elementText,
                            ratio,
                            translate(elementText.getValue()));
                    drawables.add(drawable);
                }

            }
            builder.append("\n");
        }

        final String value = builder.toString();
        overlay.add(drawables);
        if(listener != null) {
            listener.onText(value);
        }
    }

    public void setTextRecognitionListener(TextRecognitionListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

}
