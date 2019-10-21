package com.example.visiontranslation.ui.image;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.visiontranslation.R;
import com.example.visiontranslation.helper.Helper;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfRect;
import org.opencv.text.TextDetectorCNN;

import java.io.IOError;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {

    private final int REQUEST_IMAGE = 103;
    private final String TAG = getClass().getSimpleName();

    public ImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button).setOnClickListener(v->{
            startImageChooseActivity();
        });
        init();
    }

    private void init() {
        Helper.loadOpenCV(new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {

            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            if(data == null) {
                return;
            }

            try {
                Uri uri = data.getData();
                if(uri == null) {
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri));
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
                new Thread(()->{
                    process(mat);
                }).start();
            } catch (IOException e) {
                Log.d(TAG, "Failed to read image", e);
            }

        }
    }

    private void process(Mat mat) {
        TextDetectorCNN textDetectorCNN = org.opencv.text.TextDetectorCNN.create(
                "/data/data/com.example.visiontranslation/files/TextBoxes_icdar13.caffemodel",
                "/data/data/com.example.visiontranslation/files/textbox.prototxt"

                );

        MatOfRect matOfRect = new MatOfRect();
        MatOfFloat matOfFloat = new MatOfFloat();

        textDetectorCNN.detect(mat, matOfRect, matOfFloat);
    }

    private void startImageChooseActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        this.startActivityForResult(intent, REQUEST_IMAGE);
    }
}
