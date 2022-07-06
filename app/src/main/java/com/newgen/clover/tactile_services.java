package com.newgen.clover;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.io.File;
import java.util.concurrent.Future;

public class tactile_services extends AppCompatActivity {

    static final int visualreturn = 1000;

    boolean isMorse = false;
    
    ImageButton audioinput;
    ImageButton visualinput;
    Vibrator v;
    private static String speechSubscriptionKey = "72b02146162b4740b8e51a338748e8a3";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "westeurope";

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tactile_services);
        audioinput = (ImageButton) findViewById(R.id.audioinput);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tact_toolbar);
        setSupportActionBar(toolbar);
        visualinput = (ImageButton) findViewById(R.id.visualinput);
        progressBar = (ProgressBar) findViewById(R.id.tact_progress);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar = new ProgressBar(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tactile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        isMorse = (isMorse)?false:true;
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(300);
        if(isMorse==true)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
               e.printStackTrace();
            }
            v.vibrate(300);
        }
        return super.onOptionsItemSelected(item);
    }

    public void visualInput(View view) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(300);
        Intent i = new Intent(tactile_services.this, blind_services.class);
        startActivityForResult(i, visualreturn);
    }

    public void audioInput(View view) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(300);
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        v.vibrate(300);
        try
        {
            Thread.sleep(300);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioinput.setEnabled(false);
        visualinput.setEnabled(false);
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
                convertStringToVibrations(res);
            }
            else {
                convertStringToVibrations("Error");
            }

            reco.close();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
        }
        audioinput.setEnabled(true);
        visualinput.setEnabled(true);
    }

    @Override
    protected void onPause() {
        if(v!=null)
            v.cancel();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(v!=null)
            v.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(v!=null)
            v.cancel();
        finish();
        super.onBackPressed();
    }

    private void convertStringToVibrations(String arg)
    {
        arg = arg.toLowerCase();
        arg = arg.replace("the primary color appears to be", "");
        if(isMorse==false) {
            arg = arg.replaceAll("and", "∞");
            arg = arg.replaceAll("for", "‣");
            arg = arg.replaceAll("of", "₪");
            arg = arg.replaceAll("the", "α");
            arg = arg.replaceAll("with", "β");
            arg = arg.replaceAll("ch", "γ");
            arg = arg.replaceAll("gh", "δ");
            arg = arg.replaceAll("sh", "ε");
            arg = arg.replaceAll("th", "ζ");
            arg = arg.replaceAll("wh", "η");
            arg = arg.replaceAll("ed", "θ");
            arg = arg.replaceAll("er", "ι");
            arg = arg.replaceAll("ou", "κ");
            arg = arg.replaceAll("ow", "λ");
            arg = arg.replaceAll("en", "μ");
            arg = arg.replaceAll("in", "ν");
            for (int i = 0; i < arg.length(); i++) {
                vibrateLetterBraille(arg.charAt(i));
            }
        }
        else{
            for(int i=0;i<arg.length();i++)
            {
                vibrateLetterMorse(arg.charAt(i));
            }
        }
    }

    private void vibrateLetterMorse(char x)
    {
        switch (x){
            case ' ':{
                vibrateMorse(new int[]{2});
            }
            break;
            case 'a':{
                vibrateMorse(new int[]{0,1});
            }
            break;
            case 'b':{
                vibrateMorse(new int[]{1,0,0,0});
            }
            break;
            case 'c':{
                vibrateMorse(new int[]{1,0,1,0});
            }
            break;
            case 'd':{
                vibrateMorse(new int[]{1,0,0});
            }
            break;
            case 'e':{
                vibrateMorse(new int[]{0});
            }
            break;
            case 'f':{
                vibrateMorse(new int[]{0,0,1,0});
            }
            break;
            case 'g':{
                vibrateMorse(new int[]{1,1,0});
            }
            break;
            case 'h':{
                vibrateMorse(new int[]{0,0,0,0});
            }
            break;
            case 'i':{
                vibrateMorse(new int[]{0,0});
            }
            break;
            case 'j':{
                vibrateMorse(new int[]{0,1,1,1});
            }
            break;
            case 'k':{
                vibrateMorse(new int[]{1,0,1});
            }
            break;
            case 'l':{
                vibrateMorse(new int[]{0,1,0,0});
            }
            break;
            case 'm':{
                vibrateMorse(new int[]{1,1});
            }
            break;
            case 'n':{
                vibrateMorse(new int[]{1,0});
            }
            break;
            case 'o':{
                vibrateMorse(new int[]{1,1,1});
            }
            break;
            case 'p':{
                vibrateMorse(new int[]{0,1,1,0});
            }
            break;
            case 'q':{
                vibrateMorse(new int[]{1,1,0,1});
            }
            break;
            case 'r':{
                vibrateMorse(new int[]{0,1,0});
            }
            break;
            case 's':{
                vibrateMorse(new int[]{0,0,0});
            }
            break;
            case 't':{
                vibrateMorse(new int[]{1});
            }
            break;
            case 'u':{
                vibrateMorse(new int[]{0,0,1});
            }
            break;
            case 'v':{
                vibrateMorse(new int[]{0,0,0,1});
            }
            break;
            case 'w':{
                vibrateMorse(new int[]{0,1,1});
            }
            break;
            case 'x':{
                vibrateMorse(new int[]{1,0,0,1});
            }
            break;
            case 'y':{
                vibrateMorse(new int[]{1,0,1,1});
            }
            break;
            case 'z':{
                vibrateMorse(new int[]{1,1,0,0});
            }
            break;
            case '1':{
                vibrateMorse(new int[]{0,1,1,1,1});
            }
            break;
            case '2':{
                vibrateMorse(new int[]{0,0,1,1,1});
            }
            break;
            case '3':{
                vibrateMorse(new int[]{0,0,0,1,1});
            }
            break;
            case '4':{
                vibrateMorse(new int[]{0,0,0,0,1});
            }
            break;
            case '5':{
                vibrateMorse(new int[]{0,0,0,0,0});
            }
            break;
            case '6':{
                vibrateMorse(new int[]{1,0,0,0,0});
            }
            break;
            case '7':{
                vibrateMorse(new int[]{1,1,0,0,0});
            }
            break;
            case '8':{
                vibrateMorse(new int[]{1,1,1,0,0});
            }
            break;
            case '9':{
                vibrateMorse(new int[]{1,1,1,1,0});
            }
            break;
            case '0':{
                vibrateMorse(new int[]{1,1,1,1,1});
            }
            break;
        }
    }

    private void vibrateLetterBraille(char x)
    {
        switch (x)
        {

            case ' ':{
                vibrateBraille(new int[]{0,0,0,0,0,0});
            }
            break;
            case '∞':{
                vibrateBraille(new int[]{1,1,1,0,1,1});
            }
            break;
            case '‣':{
                vibrateBraille(new int[]{1,1,1,1,1,1});
            }
            break;
            case '₪':{
                vibrateBraille(new int[]{1,0,1,1,1,1});
            }
            break;
            case 'α':{
                vibrateBraille(new int[]{0,1,1,0,1,1});
            }
            break;
            case 'β':{
                vibrateBraille(new int[]{0,1,1,1,1,1});
            }
            break;
            case 'γ':{
                vibrateBraille(new int[]{1,0,0,0,0,1});
            }
            break;
            case 'δ':{
                vibrateBraille(new int[]{1,0,1,0,0,1});
            }
            break;
            case 'ε':{
                vibrateBraille(new int[]{1,1,0,0,0,1});
            }
            break;
            case 'ζ':{
                vibrateBraille(new int[]{1,1,0,1,0,1});
            }
            break;
            case 'η':{
                vibrateBraille(new int[]{1,0,0,1,0,1});
            }
            break;
            case 'θ':{
                vibrateBraille(new int[]{1,1,1,0,0,1});
            }
            break;
            case 'ι':{
                vibrateBraille(new int[]{1,1,1,1,0,1});
            }
            break;
            case 'κ':{
                vibrateBraille(new int[]{1,0,1,1,0,1});
            }
            break;
            case 'λ':{
                vibrateBraille(new int[]{0,1,1,0,0,1});
            }
            break;
            case 'μ':{
                vibrateBraille(new int[]{1,0,0,0,0,0});
            }
            break;
            case 'ν':{
                vibrateBraille(new int[]{0,0,1,0,0,1});
            }
            break;
            case 'b':{
                vibrateBraille(new int[]{0,0,0,1,1,0});
            }
            break;
            case 'c':{
                vibrateBraille(new int[]{1,1,0,0,0,0});
            }
            break;
            case 'd':{
                vibrateBraille(new int[]{1,1,0,1,0,0});
            }
            break;
            case 'e':{
                vibrateBraille(new int[]{1,0,0,1,0,0});
            }
            break;
            case 'f':{
                vibrateBraille(new int[]{1,1,1,0,0,0});
            }
            break;
            case 'g':{
                vibrateBraille(new int[]{1,1,1,1,0,0});
            }
            break;
            case 'h':{
                vibrateBraille(new int[]{1,0,1,1,0,0});
            }
            break;
            case 'i':{
                vibrateBraille(new int[]{0,1,1,0,0,0});
            }
            break;
            case 'j':{
                vibrateBraille(new int[]{0,1,1,1,0,0});
            }
            break;
            case 'k':{
                vibrateBraille(new int[]{1,0,0,0,1,0});
            }
            break;
            case 'l':{
                vibrateBraille(new int[]{1,0,1,0,1,0});
            }
            break;
            case 'm':{
                vibrateBraille(new int[]{1,1,0,0,1,0});
            }
            break;
            case 'n':{
                vibrateBraille(new int[]{1,1,0,1,1,0});
            }
            break;
            case 'o':{
                vibrateBraille(new int[]{1,0,0,1,1,0});
            }
            break;
            case 'p':{
                vibrateBraille(new int[]{1,1,1,0,1,0});
            }
            break;
            case 'q':{
                vibrateBraille(new int[]{1,1,1,1,1,0});
            }
            break;
            case 'r':{
                vibrateBraille(new int[]{1,0,1,1,1,0});
            }
            break;
            case 's':{
                vibrateBraille(new int[]{0,1,1,0,1,0});
            }
            break;
            case 't':{
                vibrateBraille(new int[]{0,1,1,1,1,0});
            }
            break;
            case 'u':{
                vibrateBraille(new int[]{1,0,0,0,1,1});
            }
            break;
            case 'v':{
                vibrateBraille(new int[]{1,0,1,0,1,1});
            }
            break;
            case 'w':{
                vibrateBraille(new int[]{0,1,1,1,0,1});
            }
            break;
            case 'x':{
                vibrateBraille(new int[]{1,1,0,0,1,1});
            }
            break;
            case 'y':{
                vibrateBraille(new int[]{1,1,0,1,1,1});
            }
            break;
            case 'z':{
                vibrateBraille(new int[]{1,0,0,1,1,1});
            }
            break;
            case '1':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,0,0,0,0,0});
            }
            break;
            case '2':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,0,1,0,0,0});
            }
            break;
            case '3':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,1,0,0,0,0});
            }
            break;
            case '4':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,1,0,1,0,0});
            }
            break;
            case '5':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,0,0,1,0,0});
            }
            break;
            case '6':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,1,1,0,0,0});
            }
            break;
            case '7':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,1,1,1,0,0});
            }
            break;
            case '8':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{1,0,1,1,0,0});
            }
            break;
            case '9':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{0,1,1,0,0,0});
            }
            break;
            case '0':{
                vibrateBraille(new int[]{0,1,0,1,1,1});
                vibrateBraille(new int[]{0,1,1,1,0,0});
            }
            break;
            case '.':{
                vibrateBraille(new int[]{0,0,1,1,0,1});
            }
            break;
            case ',':{
                vibrateBraille(new int[]{0,0,1,0,0,0});
            }
            break;
        }
    }

    private void vibrateMorse(int c[]){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        for(int i=0;i<c.length;i++)
        {
            if(c[i]==1)
            {
                v.vibrate(900);
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else if(c[i]==0) {
                v.vibrate(300);
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else if(c[i]==2)
            {
                try {
                    Thread.sleep(1900);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void vibrateBraille(int c[])
    {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        for(int i=0;i<6;i++)
        {
            if(c[i]==1)
            {
                v.vibrate(900);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                v.vibrate(300);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode)
        {
            case visualreturn:{
                if(resultCode== Activity.RESULT_OK){
                    String returnedString = data.getStringExtra("result");
                    convertStringToVibrations(returnedString);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
