package com.example.lenovo;

import android.graphics.Color;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lenovo.correctly.R;
import com.example.lenovo.correctly.clients.StreamingRecognizeClient;
import com.example.lenovo.correctly.fluencyActivity;
import com.example.lenovo.correctly.utils.GoogleAudioFormat;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Locale;
import java.util.Random;

public class PronunceActivity extends AppCompatActivity implements View.OnClickListener {
    public TextView trans;
    public ImageView sounder,correctImage,record;
    public EditText wordshow;
    public int i = 0;
    public String confidence = "";
    public String transcript = "";
    public String topic, level;
    String text = "";
    TextToSpeech t1;
    //ProgressDialog progressDialog;


    private AudioRecord mAudioRecord = null;
    private boolean mIsRecording = false;
    private StreamingRecognizeClient mStreamingClient;

    private void startRecording() {
        mAudioRecord.startRecording();
        mIsRecording = true;
        Thread mRecordingThread = new Thread(new Runnable() {
            public void run() {
                readData();
            }
        }, "AudioRecorder Thread");
        mRecordingThread.start();
    }

    public void changeColorOfWord() {
        String str = wordshow.getText().toString();
        SpannableString spannable = new SpannableString(str);
        str = str.replace(" ", "");
        transcript = transcript.replace("\"", "").replace(" ", "");
        Float confidenceScore = Float.parseFloat(confidence);
        Boolean isMatch = (str.compareToIgnoreCase(StringEscapeUtils
                .unescapeXml(transcript)) == 0);
        Boolean isConfident = (confidenceScore >= GoogleAudioFormat.CONFIDENCE);
        Boolean correct = (isMatch && isConfident);

        if (correct) {
            trans.setText(String.format(Locale.getDefault(),
                    getString(R.string.correct_message), confidenceScore * 100));
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor
                    ("#008744")), 0, wordshow.getText().toString()
                    .length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            correctImage.setImageResource(R.mipmap.correct);
            correctImage.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.zoom_in));
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    //updateWord();

                }

            };

            Handler h = new Handler();
            h.postDelayed(r, 3000);

        } else {
            if (isMatch) {
                trans.setText(getString(R.string.low_confidence_msg));
            } else if (isConfident) {
                trans.setText(String.format(Locale.getDefault(),
                        getString(R.string.high_confidence_incorrect), transcript));
            } else {
                trans.setText(getString(R.string.incorrect_msg));
            }
            spannable.setSpan(
                    new ForegroundColorSpan(
                            Color.parseColor(
                                    getString(R.string.foreground_span_color))),
                    0,
                    wordshow.getText().toString().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            correctImage.setImageResource(R.mipmap.incorrect);
            correctImage.setAnimation(
                    AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in));

        }

        trans.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_in));
        trans.animate().start();


    }
    private void readData() {
        byte sData[] = new byte[GoogleAudioFormat.BufferSize];
        while (mIsRecording) {
            int bytesRead = mAudioRecord.read(sData, 0, GoogleAudioFormat
                    .BufferSize);
            if (bytesRead > 0) {
                try {
                    mStreamingClient.recognizeBytes(sData, bytesRead);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(getClass().getSimpleName(), "Error while reading bytes:" +
                        " " + bytesRead);
            }
        }
    }
    private void initialize() {

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg != null) {
                    String[] help = msg.obj.toString().split("\n");
                    for (int i = 0; i < help.length; i++) {
                        if (help[i].contains("confidence:")) {
                            confidence = help[i].replace("confidence:", "");
                            transcript = help[i - 1].replace("transcript:", "");
                            mIsRecording = false;
                            mAudioRecord.stop();
                            mStreamingClient.finish();
                            changeColorOfWord();
                            record.clearAnimation();

                        }
                    }
                }
                super.handleMessage(msg);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProviderInstaller.installIfNeeded(getApplicationContext());
                } catch (GooglePlayServicesRepairableException e) {

                    // Indicates that Google Play services is out of date,
                    // disabled, etc.
                    e.printStackTrace();
                    // Prompt the user to install/update/enable Google Play
                    // services.
                    GooglePlayServicesUtil.showErrorNotification(
                            e.getConnectionStatusCode(), getApplicationContext());
                    return;

                } catch (GooglePlayServicesNotAvailableException e) {
                    // Indicates a non-recoverable error; the
                    // ProviderInstaller is not able
                    // to install an up-to-date Provider.
                    e.printStackTrace();
                    return;
                }

                try {
                    mStreamingClient = GoogleAudioFormat.getStreamRecognizer
                            (PronunceActivity.this, handler);
                } catch (Exception e) {
                    Log.e(fluencyActivity.class.getSimpleName(), "Error", e);
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStreamingClient != null) {
            try {
                mStreamingClient.shutdown();
            } catch (InterruptedException e) {
                Log.e(fluencyActivity.class.getSimpleName(), "Error", e);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunce);

        wordshow=(EditText)findViewById(R.id.here);

        trans= (TextView) findViewById(R.id.trans);
        correctImage = (ImageView)findViewById(R.id
                .correctImages);
        correctImage.setVisibility(View.INVISIBLE);


        sounder=(ImageView)findViewById(R.id.sound);
        record= (FloatingActionButton) findViewById(R.id.record);
        //hear = (ImageView) findViewById(R.id.hear);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                t1.setLanguage(Locale.ENGLISH);
                t1.setSpeechRate((float) 0.8);
            }


        });
        mAudioRecord = GoogleAudioFormat.getAudioRecorder();
        initialize();
        sounder.setOnClickListener(this);
        record.setOnClickListener(this);
        trans.setText("Your result shown here");

    }
    public void updateWord(){
        correctImage.setVisibility(View.GONE);

        trans.setText("Your result shown here");

    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.sound){
            if (!mIsRecording) {
                Log.d("record",wordshow.getText().toString());

                String toSpeak = wordshow.getText().toString();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        if(v.getId()==R.id.record){
            if (mIsRecording) {
                mIsRecording = false;
                record.clearAnimation();
                mAudioRecord.stop();
                mStreamingClient.finish();
            } else {
                if (mAudioRecord.getState() == AudioRecord
                        .STATE_INITIALIZED) {
                    // mRecordingBt.setText(R.string.stop_recording);
                    Animation pulse = AnimationUtils.loadAnimation
                            (getApplicationContext(), R.anim.pulse);
                    record.startAnimation(pulse);
                    startRecording();
                } else {
                    Log.i(this.getClass().getSimpleName(), "Not " +
                            "Initialized yet.");
                }
            }
        }
    }
}
