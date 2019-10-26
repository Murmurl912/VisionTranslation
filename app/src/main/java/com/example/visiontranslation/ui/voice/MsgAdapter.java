package com.example.visiontranslation.ui.voice;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.visiontranslation.R;
import com.example.visiontranslation.helper.Helper;
import com.example.visiontranslation.ui.MainActivity;

import java.util.List;
import java.util.Locale;

import static androidx.camera.core.CameraX.getContext;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Msg> mMsgList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        View msgView;
       /* ImageButton leftVoice;
        ImageButton rightVoice;*/


        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rightMsg = (TextView) view.findViewById(R.id.right_msg);
            msgView = view;
            /*leftVoice = (ImageButton) view.findViewById(R.id.voice_left_button);
            rightVoice = (ImageButton) view.findViewById(R.id.voice_right_button);*/
        }
    }
    public MsgAdapter(List<Msg> msgList) {
        mMsgList = msgList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.msgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    Msg msg = mMsgList.get(position);

                    //Toast.makeText(view.getContext(), "you clicked button!",Toast.LENGTH_SHORT).show();
                    speak(
                            msg.getTarget(),
                            Helper.getLocaleByLanguage(
                                    msg.getLanguage()),
                            view.getContext()
                    );
                }
        });
        //return new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Msg msg = mMsgList.get(position);
        if (msg.getType() == Msg.TYPE_RECEIVED) {
            // 如果是收到的消息，则显示左边的消息布局，将右边的消息布局隐藏
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else if(msg.getType() == Msg.TYPE_SENT) {
            // 如果是发出的消息，则显示右边的消息布局，将左边的消息布局隐藏
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

    private boolean isTTSReady = false;
    private TextToSpeech tts;

    private void speak(@NonNull String text, Locale locale, @NonNull Context context) {

        if(tts == null) {
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public synchronized void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS) {
                        isTTSReady = true;
                        tts.setLanguage(locale);
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                        Toast.makeText(context, "Speaking", Toast.LENGTH_SHORT).show();
                    } else {
                        isTTSReady = false;
                        Toast.makeText(context, "Text To Speech Failed to Initialize", Toast.LENGTH_SHORT).show();
                        tts = null;
                    }
                }
            });
        } else {
            if(isTTSReady) {
                tts.setLanguage(locale);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                Toast.makeText(context, "Speaking", Toast.LENGTH_SHORT).show();
            } else {
                tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                    @Override
                    public synchronized void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS) {
                            isTTSReady = true;
                            tts.setLanguage(locale);
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                            Toast.makeText(context, "Speaking", Toast.LENGTH_SHORT).show();
                        } else {
                            isTTSReady = false;
                            Toast.makeText(context, "Text To Speech Failed to Initialize", Toast.LENGTH_SHORT).show();
                            tts = null;
                        }
                    }
                });
            }
        }
    }


}

