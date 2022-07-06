package com.newgen.clover;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Future;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class auditory_impairment extends AppCompatActivity {

    EditText textbox;
    private static String speechSubscriptionKey = "72b02146162b4740b8e51a338748e8a3";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "westeurope";

    public static final String DIRECTORY_NAME_TEMP = "AudioTemp";
    public static final int REPEAT_INTERVAL = 1;
    VisualizerView visualizerView;
    private MediaRecorder recorder = null;
    File audioDirTemp;
    private boolean isRecording = false;
    private Handler handler; // Handler for updating the visualizer
    // private boolean recording; // are we currently recording?

    ImageButton audiovisualizer;
    ImageButton audiorecognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auditory_impairment_layout);
        textbox = (EditText) findViewById(R.id.recogtextbox);
        int requestCode = 5; // unique code for the permission request
        ActivityCompat.requestPermissions(auditory_impairment.this, new String[]{RECORD_AUDIO, INTERNET, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, requestCode);

        visualizerView = (VisualizerView) findViewById(R.id.visualizerview);
        audiorecognizer = (ImageButton) findViewById(R.id.audiorecog);
        audiovisualizer = (ImageButton) findViewById(R.id.audiovisualizer);

        audioDirTemp = new File(Environment.getExternalStorageDirectory(),
                DIRECTORY_NAME_TEMP);
        //audioDirTemp = new File(path, "audio_file.mp3");
        if (audioDirTemp.exists()) {
            //deleteFilesInDir(audioDirTemp);
        } else {
            audioDirTemp.mkdirs();
        }

        // create the Handler for visualizer update
        handler = new Handler();
    }

    private void releaseRecorder() {
        if (recorder != null) {
            isRecording = false; // stop recording
            handler.removeCallbacks(updateVisualizer);
            visualizerView.clear();
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording) // if we are already recording
            {
                // get the current amplitude
                int x = recorder.getMaxAmplitude();
                visualizerView.addAmplitude(x); // update the VisualizeView
                visualizerView.invalidate(); // refresh the VisualizerView
                handler.postDelayed(this, 40);
                // update in 40 milliseconds
            }
            else {
                //handler.postDelayed(this, REPEAT_INTERVAL);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseRecorder();
    }


    public void recogtext(View view) {
        textbox.setText("");
        isRecording = false;
        releaseRecorder();
        audiovisualizer.setEnabled(false);
        audiorecognizer.setEnabled(false);
        try {
            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
            assert(config != null);

            SpeechRecognizer reco = new SpeechRecognizer(config);
            assert(reco != null);

            Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            assert(task != null);

            // Note: this will block the UI thread, so eventually, you want to
            //        register for the event (see full samples)
            SpeechRecognitionResult result = task.get();
            assert(result != null);

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                StringBuilder sb = new StringBuilder();
                boolean parse = false;
                for(int i=0;i<result.toString().length();i++)
                {
                    char curchar = result.toString().charAt(i);
                    if(parse==true)
                        sb.append(curchar);
                    if(curchar=='<')
                        parse=true;
                    if(curchar=='>')
                        parse=false;
                }
                String res = sb.toString();
                res = res.replace(">", "");
                textbox.setText(res);
            }
            else {
                textbox.setText("Error recognizing" + System.lineSeparator() + result.toString());
            }

            reco.close();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
        }
        audiovisualizer.setEnabled(true);
        audiorecognizer.setEnabled(true);
    }

    public void dfw(View view) {
        if (!isRecording) {
            isRecording = true;
            recorder = new MediaRecorder();

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioDirTemp + "/audio_file" + ".mp3");

            MediaRecorder.OnErrorListener errorListener = null;
            recorder.setOnErrorListener(errorListener);
            MediaRecorder.OnInfoListener infoListener = null;
            recorder.setOnInfoListener(infoListener);

            try {
                recorder.prepare();
                recorder.start();
                isRecording = true; // we are currently recording
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Log.e("Error1", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Error2", e.getMessage());
            }

            handler.post(updateVisualizer);
        } else {
            isRecording = false;
            releaseRecorder();
        }
    }
}

