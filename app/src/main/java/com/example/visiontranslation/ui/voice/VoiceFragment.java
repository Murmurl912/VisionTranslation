package com.example.visiontranslation.ui.voice;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import com.example.visiontranslation.translation.BaiduTranslationService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.visiontranslation.R;
import com.example.visiontranslation.ui.MainActivity;
import com.google.gson.Gson;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import java.util.ArrayList;
import java.util.List;

import static com.example.visiontranslation.translation.BaiduTranslationService.STATUS_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class VoiceFragment extends Fragment {
    public final String TAG = "VoiceFragment";
    TextView textView;
    SpeechRecognizer mIat;
    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;

    public VoiceFragment() {
        // Required empty public constructor
        Log.d(TAG, "VoiceFragment");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory() called");

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach() called");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach() called");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() called");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");

        return inflater.inflate(R.layout.fragment_voice, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");

        msgRecyclerView = (RecyclerView) view.findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        //SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5d7c91ec");
        textView = (TextView) view.findViewById(R.id.text_hint);
        final Button buttonRight = (Button) view.findViewById(R.id.中文);
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initSpeechZHCN(getContext());
                        textView.setText("请开始说话...");
                        break;
                    case MotionEvent.ACTION_UP:
                        mIat.stopListening();
                        textView.setText("");
                        break;
                    default:
                }
                return true;
            }
        });
        final Button buttonLeft = (Button) view.findViewById(R.id.English);
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initSpeechENUS(getContext());
                        textView.setText("请开始说话...");
                        break;
                    case MotionEvent.ACTION_UP:
                        mIat.stopListening();
                        textView.setText("");
                        break;
                    default:
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestoryView() called");
    }

    public void initSpeechZHCN(final Context context) {
        mIat = SpeechRecognizer.createRecognizer(context, new InitListener() {
            @Override
            public void onInit(int i) {
                return;
            }
        });

        try {
            wait();
        } catch (Exception e) {

        }

        //设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
        mIat.setParameter( SpeechConstant.CLOUD_GRAMMAR, null );
        mIat.setParameter( SpeechConstant.SUBJECT, null );
        //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处engineType为“cloud”
        mIat.setParameter( SpeechConstant.ENGINE_TYPE, "cloud" );
        //设置语音输入语言，zh_cn为简体中文
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //设置结果返回语言
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
        //取值范围{1000～10000}
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        mIat.setParameter(SpeechConstant.VAD_EOS, "10000");
        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT,"1");

        //开始识别，并设置监听器
        mIat.startListening(new RecognizerListener() {
            String result;
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            @Override
            public void onBeginOfSpeech() {
                Log.d("VOICE : ", "开始识别");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d("VOICE : ", "识别结束");
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                if (b) {
                    result = parseVoice(recognizerResult.getResultString());
                    if (!"".equals(result)) {
                        BaiduTranslationService.getBaiduTranslationService().request("zh", "en", result, new BaiduTranslationService.Response() {
                            @Override
                            public void response(String s, int status) {
                                if(status == STATUS_OK) {
                                    result = result + "\n"+ s;
                                    Log.d("中文", s);
                                } else {
                                    result = result + "\n" + "Translation Services Unavailable!";
                                }
                            }
                        });
                        Msg msg = new Msg(result, Msg.TYPE_SENT);
                        msgList.add(msg);
                        adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新RecyclerView中的显示
                        msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将RecyclerView定位到最后一行
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                Log.d("VOICE", "speech error: " + speechError.toString());
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });

    }

    public void initSpeechENUS(final Context context) {
        mIat = SpeechRecognizer.createRecognizer(context, null);

        //设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
        mIat.setParameter( SpeechConstant.CLOUD_GRAMMAR, null );
        mIat.setParameter( SpeechConstant.SUBJECT, null );
        //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处engineType为“cloud”
        mIat.setParameter( SpeechConstant.ENGINE_TYPE, "cloud" );
        //设置语音输入语言，zh_cn为简体中文
        mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        //设置结果返回语言
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
        //取值范围{1000～10000}
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        mIat.setParameter(SpeechConstant.VAD_EOS, "10000");
        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT,"1");

        //开始识别，并设置监听器
        mIat.startListening(new RecognizerListener() {
            String result;
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            @Override
            public void onBeginOfSpeech() {
                Log.d("VOICE : ", "开始识别");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d("VOICE : ", "识别结束");
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                if (b) {
                    result = parseVoice(recognizerResult.getResultString());
                    if (!"".equals(result)) {
                        BaiduTranslationService.getBaiduTranslationService().request("en", "zh", result, new BaiduTranslationService.Response() {
                            @Override
                            public void response(String s, int status) {
                                if(status == STATUS_OK) {
                                    result = result + "\n"+ s;
                                } else {
                                    result = result + "\n" + "Translation Services Unavailable!";
                                }
                            }
                        });
                        Msg msg = new Msg(result, Msg.TYPE_RECEIVED);
                        msgList.add(msg);
                        adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新RecyclerView中的显示
                        msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将RecyclerView定位到最后一行
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                Log.d("VOICE", "speech error: " + speechError.toString());
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });

    }


    //解析语音json
    public String parseVoice(String resultString) {
        Gson gson = new Gson();
        Voice voiceBean = gson.fromJson(resultString, Voice.class);
        StringBuffer sb = new StringBuffer();
        ArrayList<Voice.WSBean> ws = voiceBean.ws;
        for (Voice.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }

    //语音对象封装
    public class Voice {
        public ArrayList<WSBean> ws;
        public class WSBean {

            public ArrayList<CWBean> cw;
        }

        public class CWBean {
            public String w;
        }
    }

}
