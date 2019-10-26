package com.example.visiontranslation.ui.offline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.visiontranslation.R;
import com.example.visiontranslation.translation.GoogleTranslationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class OfflineTranslationActivity extends AppCompatActivity {

    private OfflineModelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_translation);
        init();
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.offline_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerView = findViewById(R.id.offline_all);
        adapter = new OfflineModelAdapter();
        recyclerView.setAdapter(adapter);
        new Thread(()->{
            loadModel();
        }).start();
    }

    private void loadModel() {
        for(String language : GoogleTranslationService.languages) {
            Task<Boolean> task = GoogleTranslationService.isModelDownload(
                    GoogleTranslationService.getCode(language)
            );
            task.addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if(task.isSuccessful() && task.getResult() != null) {
                        if(task.getResult()) {
                            runOnUiThread(()->{
                                adapter.notifyItemInserted(adapter.addModel(language, true));
                            });
                            return;
                        }
                    }
                    runOnUiThread(()->{
                        adapter.notifyItemInserted(adapter.addModel(language, false));
                    });
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
