package com.example.visiontranslation.ui.offline;

import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.visiontranslation.R;
import com.example.visiontranslation.translation.GoogleTranslationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OfflineModelAdapter  extends RecyclerView.Adapter<OfflineModelAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        private TextView message;
        private ProgressBar progressBar;
        private ImageButton download;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.adapter_offline_message);
            name = itemView.findViewById(R.id.adapter_offline_model_name);
            progressBar = itemView.findViewById(R.id.adapter_offline_progress);
            download = itemView.findViewById(R.id.adapter_offline_download);
        }
    }


    private List<Model> models;

    public OfflineModelAdapter() {
        models = new ArrayList<>();
    }

    public int addModel(String name, boolean isDownloaded) {
        Model m = new Model(name, isDownloaded);
        if(models.contains(m)) {
            return models.indexOf(m);
        }

        models.add(m);
        return models.indexOf(m);
    }

    public void removeModel(String name) {
        models.remove(new Model(name, false));
    }

    public void updateModel(String name, boolean isDownloaded) {
        Model model = new Model(name, isDownloaded);
        int index = models.indexOf(model);
        if(index != -1) {
            models.get(index).setDownloaded(isDownloaded);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_offline_model, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = models.get(position);
        holder.name.post(()->{
            holder.name.setText(model.name);
        });
        if(model.isDownload) {
            holder.download.setImageResource(R.drawable.ic_delete_dark);
        } else {
            holder.download.setImageResource(R.drawable.ic_download_dark);
        }
        holder.download.setOnClickListener(v->{
            holder.download.setEnabled(false);

            if(model.isDownload) {
                FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
                FirebaseTranslateRemoteModel firebaseTranslateRemoteModel = new FirebaseTranslateRemoteModel.Builder(GoogleTranslationService.getCode(model.name)).build();
                modelManager.isModelDownloaded(firebaseTranslateRemoteModel).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if(task.isSuccessful()) {
                            if(task.getResult() != null) {
                                if(task.getResult()) {
                                    modelManager.deleteDownloadedModel(firebaseTranslateRemoteModel);
                                    holder.download.post(()->{
                                        holder.download.setImageResource(R.drawable.ic_download_dark);
                                    });
                                    model.setDownloaded(false);
                                    notifyItemChanged(position);
                                }
                            }
                        }
                        holder.download.setEnabled(true);
                    }
                });
            } else {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.message.post(()->{
                    holder.message.setText("Downloading");
                });
                FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
                FirebaseTranslateRemoteModel firebaseTranslateRemoteModel =
                        new FirebaseTranslateRemoteModel.Builder(GoogleTranslationService.getCode(model.name)).build();
                GoogleTranslationService.downloadModel(firebaseTranslateRemoteModel, new GoogleTranslationService.ModelDownloadCallback() {
                    @Override
                    public void onDownloadComplete() {
                        holder.download.post(()->{
                            holder.download.setImageResource(R.drawable.ic_delete_dark);
                        });
                        model.setDownloaded(true);
                        notifyItemChanged(position);
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        holder.download.setEnabled(true);
                        holder.message.post(()->{
                            holder.message.setText("");
                        });
                    }

                    @Override
                    public void onDownloadFailure(Exception e) {
                        holder.message.post(()->{
                            holder.message.setText("Error downloading model");
                        });
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        holder.download.setEnabled(true);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    private class Model {
        private String name;
        private boolean isDownload;

        public Model(@NonNull String name, boolean isDownloaded) {
            this.name = name;
            this.isDownload = isDownloaded;
        }

        public boolean isDownloaded() {
            return isDownload;
        }

        @NonNull
        public String getName() {
            return name;
        }

        public void setName(@NonNull String name) {
            this.name = name;
        }

        public void setDownloaded(boolean isDownload) {
            this.isDownload = isDownload;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Model model = (Model) o;
            return name.equals(model.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
