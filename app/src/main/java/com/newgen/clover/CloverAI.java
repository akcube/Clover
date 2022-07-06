package com.newgen.clover;

import android.app.Activity;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

public class CloverAI extends AppCompatActivity {

    TextView textbox;
    String globstring;

    boolean hasFlash;
    private Synthesizer m_syn;


    private static String speechSubscriptionKey = "72b02146162b4740b8e51a338748e8a3";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "westeurope";

    LinearLayout progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clover_ai_layout);
        textbox = (TextView) findViewById(R.id.displayResult);
        progressBar = (LinearLayout) findViewById(R.id.progressbarai);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void micClick(View view) {
        textbox.setText("");

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
                globstring = res.toLowerCase();
                processdata(res);
            }
            else {
                textbox.setText("Error recognizing" + System.lineSeparator() + result.toString());
            }
            reco.close();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    public static Boolean spellOrNot(final String str)
    {
        String [] strA = str.split(" ", 2);
        String firstWord = strA[0];
        if(firstWord.contains("spell"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static String spellB(final String str)
    {
        String[] letters = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
        String finalWord = str;
        finalWord = finalWord.replaceAll("a","a ");
        finalWord = finalWord.replaceAll("b","b ");
        for(int i = 0; i<26;i++)
        {
            String letter = letters[i];
            finalWord = finalWord.replaceAll(letter," "+letter);
        }
        finalWord = finalWord.replaceAll(" ","-");
        return finalWord;
    }

    public void speakText(String input)
    {
        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
        }
        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);
        input = input.replace("&", "and");
        input = input.replace(":"," ");
        m_syn.SpeakToAudio(input);
    }

    public void processdata(String txt2speechResult){
        txt2speechResult=txt2speechResult.toLowerCase();
        if (spellOrNot(txt2speechResult)&&txt2speechResult!=null||txt2speechResult.contains("how")&&txt2speechResult.contains("spell")||txt2speechResult.contains("spell")&&txt2speechResult.contains("can you"))
        {
            String[]split=txt2speechResult.split("spell", 2);
            String v2txt = split[1];
            v2txt = v2txt.replace("spell", "");
            v2txt = spellB(v2txt);
            v2txt = v2txt.replaceAll("--","-");
            speakText(v2txt);
            textbox.setText(v2txt);
        }
        else if (txt2speechResult.contains("nearest") || txt2speechResult.contains("nearby")||txt2speechResult.contains("closeby")||txt2speechResult.contains("closest")) {
            LocationManager lm = (LocationManager)CloverAI.this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}
            if(!gps_enabled && !network_enabled) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(CloverAI.this);
                dialog.setMessage("Location/GPS not enabled. Please enable Location/GPS to use this function");
                dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        CloverAI.this.startActivity(myIntent);
                        //get gps
                    }
                });
                dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }
            else{
                String v2txt = txt2speechResult;
                String[] splitWords = new String[]{"0", "Could not understand"};
                if(txt2speechResult.contains("nearby"))
                {
                    splitWords = txt2speechResult.split("nearby");
                }
                else if(txt2speechResult.contains("nearest"))
                {
                    splitWords = txt2speechResult.split("nearest");
                }
                else if(txt2speechResult.contains("closeby"))
                {
                    splitWords = txt2speechResult.split("closeby");
                }else if(txt2speechResult.contains("closest"))
                {
                    splitWords = txt2speechResult.split("closest");
                }
                else if(txt2speechResult.contains("close by"))
                {
                    splitWords = txt2speechResult.split("close by");
                }
                v2txt = splitWords[1];
                if (!v2txt.contains("Could not understand"))
                {
                    speakText("Trying to locate nearby "+v2txt);
                    Uri uri = Uri.parse("geo:0,0?q="+Uri.encode(v2txt));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    CloverAI.this.startActivity(intent);
                }
                else
                {
                    speakText("What should I search for?");
                }
                        /*Intent openPlacesDecoder = new Intent(CloverAI.this, PlacesDecoder.class);
                        openPlacesDecoder.putExtra("v2txt", v2txt);
                        startActivity(openPlacesDecoder);
                        */
            }
        }
        else if (txt2speechResult.contains("do you love me"))
        {
            speakText("I hardly think now is the time for emotion");
        }
        else if(txt2speechResult.contains("set")&&txt2speechResult.contains("timer"))
        {
            String v2txt = txt2speechResult;
            int minutes = 0;
            int hours = 0;
            if(v2txt.contains("hour")&&v2txt.contains("minute"))
            {
                for (int i = 0; i < 999; i++)
                {
                    v2txt = v2txt.replaceAll(" ","");
                    if(v2txt.contains(String.valueOf(i).toString()+"m"))
                    {
                        minutes = i;
                    }
                    if(v2txt.contains(String.valueOf(i).toString()+"h"))
                    {
                        hours = i;
                    }
                }
            }
            else
            {
                if(v2txt.contains("hour"))
                {
                    for (int i = 0; i < 999; i++)
                    {
                        v2txt = v2txt.replaceAll(" ","");
                        if(v2txt.contains(String.valueOf(i).toString()+"h"))
                        {
                            hours = i;
                        }
                    }
                }
                else if(v2txt.contains("minute"))
                {
                    for (int i = 0; i < 999; i++)
                    {
                        v2txt = v2txt.replaceAll(" ","");
                        if(v2txt.contains(String.valueOf(i).toString()+"m"))
                        {
                            minutes = i;
                        }
                    }
                }
            }
            int seconds = hours * 3600 + minutes * 60;
            Intent setTimer = new Intent(AlarmClock.ACTION_SET_TIMER);
            setTimer.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            setTimer.putExtra(AlarmClock.EXTRA_LENGTH, seconds);
            CloverAI.this.startActivity(setTimer);
        }
        else if (!txt2speechResult.contains("open") && txt2speechResult.contains("what's the time") ||!txt2speechResult.contains("open") && txt2speechResult.contains("whats the time") ||!txt2speechResult.contains("open") && txt2speechResult.contains("what is the time today") ||!txt2speechResult.contains("open") && txt2speechResult.contains("time update")||txt2speechResult.contains("time")&& !txt2speechResult.contains("search")&& !txt2speechResult.contains("call")&& !txt2speechResult.contains("email")&& !txt2speechResult.contains("play")&& !txt2speechResult.contains("music")&&!txt2speechResult.contains("open") ||!txt2speechResult.contains("open") &&txt2speechResult.contains("what's the date") || !txt2speechResult.contains("open") &&txt2speechResult.contains("whats the date") ||!txt2speechResult.contains("open") && txt2speechResult.contains("what is the date today") ||!txt2speechResult.contains("open") && txt2speechResult.contains("date update")||txt2speechResult.contains("date")&& !txt2speechResult.contains("search")&& !txt2speechResult.contains("call")&& !txt2speechResult.contains("email")&& !txt2speechResult.contains("play")&& !txt2speechResult.contains("music")&&!txt2speechResult.contains("open")) {
            String time;
            String sec;
            String day;
            String month;
            String year;
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            SimpleDateFormat secformat = new SimpleDateFormat("ss");
            Calendar c = Calendar.getInstance();
            SimpleDateFormat yearformat = new SimpleDateFormat("yyyy");
            year = yearformat.format(c.getTime());
            day = dayFormat.format(c.getTime());
            SimpleDateFormat monthformat = new SimpleDateFormat("MMMM");
            sec = secformat.format(c.getTime());
            month = monthformat.format(c.getTime());
            SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm a");
            time = timeformat.format(c.getTime());
            String display = "The time is " + time + " & " + sec + " seconds. And the date is " + day + " " + month + ", " + year;
            speakText(display);
        }
        else if (txt2speechResult.contains("search")&& !txt2speechResult.contains("send what") || txt2speechResult.contains("what is")&& !txt2speechResult.contains("send what") || txt2speechResult.contains("who is")&& !txt2speechResult.contains("send what") || txt2speechResult.contains("how do")&& !txt2speechResult.contains("send what")|| txt2speechResult.contains("what's")&& !txt2speechResult.contains("send what")|| txt2speechResult.contains("who's")) {
            String l = txt2speechResult.toLowerCase();
            if(l.contains("search yahoo")||l.contains("yahoo search")&& !l.contains("search yahoo search"))
            {
                String query = l;
                query = query.replace("search", "");
                query = query.replace("yahoo", "");
                query = Uri.encode(query);
                String url = "http://search.yahoo.com/search?p=" + query + "";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
            else if(l.contains("search bing")||l.contains("bing search")&& !l.contains("search bing search"))
            {
                String query = l;
                query = query.replace("search", "");
                query = query.replace("bing", "");
                query = Uri.encode(query);
                String url = "http://www.bing.com/search?q=" + query + "&go=Submit&qs=ds&form=QBLH";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
            else if(l.contains("search google")||l.contains("google search")&& !l.contains("search google search"))
            {
                String query = l;
                query = query.replace("search", "");
                query = query.replace("google", "");
                speakText("searching "+query);
                Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                search.putExtra(SearchManager.QUERY, query);
                startActivity(search);
            }
            else {
                String v2txt = txt2speechResult;

                v2txt = v2txt.replace("search", "");

                speakText("searching "+v2txt);
                Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                search.putExtra(SearchManager.QUERY, v2txt);
                startActivity(search);
            }
        }
        else if (txt2speechResult.contains("call")||txt2speechResult.contains("telephone")) {

            String v2txt = txt2speechResult;
            if (txt2speechResult.contains("call")) {
                String[] strA = txt2speechResult.split("call", 2);
                v2txt = strA[1];
            }
            else if(txt2speechResult.contains("telephone"))
            {
                String[] strA = txt2speechResult.split("telephone", 2);
                v2txt = strA[1];
            }
            speakText("Calling "+v2txt);
            Intent search = new Intent(CloverAI.this, dialup.class);
            search.putExtra("phnoforq", v2txt);
            startActivity(search);
        }

        else if (txt2speechResult.contains("dial")) {
            String v2txt = txt2speechResult;
            String[] strA = txt2speechResult.split("dial", 2);
            v2txt = strA[1];
            v2txt = v2txt.replace("dial", "");
            v2txt = v2txt.replaceAll("to", "2");
            v2txt = v2txt.replaceAll("too", "2");
            v2txt = v2txt.replaceAll("w", "double");
            v2txt = v2txt.replaceAll("one", "1");
            v2txt = v2txt.replaceAll("two", "2");
            v2txt = v2txt.replaceAll("three", "3");
            v2txt = v2txt.replaceAll("four", "4");
            v2txt = v2txt.replaceAll("five", "5");
            v2txt = v2txt.replaceAll("six", "6");
            v2txt = v2txt.replaceAll("seven", "7");
            v2txt = v2txt.replaceAll("eight", "8");
            v2txt = v2txt.replaceAll("nine", "9");
            v2txt = v2txt.replaceAll("zero", "0");
            v2txt = v2txt.replaceAll("double one", "11");
            v2txt = v2txt.replaceAll("double two", "22");
            v2txt = v2txt.replaceAll("double three", "33");
            v2txt = v2txt.replaceAll("double four", "44");
            v2txt = v2txt.replaceAll("double five", "55");
            v2txt = v2txt.replaceAll("double six", "66");
            v2txt = v2txt.replaceAll("double seven", "77");
            v2txt = v2txt.replaceAll("double eight", "88");
            v2txt = v2txt.replaceAll("double nine", "99");
            v2txt = v2txt.replaceAll("double zero", "00");
            v2txt = v2txt.replaceAll("triple one", "111");
            v2txt = v2txt.replaceAll("triple two", "222");
            v2txt = v2txt.replaceAll("triple three", "333");
            v2txt = v2txt.replaceAll("triple four", "444");
            v2txt = v2txt.replaceAll("triple five", "555");
            v2txt = v2txt.replaceAll("triple six", "666");
            v2txt = v2txt.replaceAll("triple seven", "777");
            v2txt = v2txt.replaceAll("triple eight", "888");
            v2txt = v2txt.replaceAll("triple nine", "999");
            v2txt = v2txt.replaceAll("triple zero", "000");
            String v3txt = v2txt.replaceAll("-", " ");
            v3txt = v2txt.replaceAll(" ", "-");
            v2txt = v2txt.replaceAll("-", "");
            v2txt = v2txt.replaceAll(" ", "");
            speakText("Dialling "+v3txt);
            Intent dial = new Intent(Intent.ACTION_DIAL);
            dial.setData(Uri.parse("tel:" + v2txt));
            startActivity(dial);
        }
        else if (txt2speechResult.contains("camera")) {

            String v2txt = txt2speechResult;
            v2txt = v2txt.replace("camera", "");
            speakText("Opening Camera");
            Intent camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(camera);

        }
        else if (txt2speechResult.contains("navigate") || txt2speechResult.contains("direction")) {
            String v2txt = txt2speechResult;
            if(txt2speechResult.contains("direction"))
            {
                String[] strA = txt2speechResult.split("direction", 2);
                v2txt = strA[1];
            }
            else if(txt2speechResult.contains("directions"))
            {
                String[] strA = txt2speechResult.split("directions", 2);
                v2txt = strA[1];
            }
            else if(txt2speechResult.contains("navigate to"))
            {
                String[] strA = txt2speechResult.split("navigate to", 2);
                v2txt = strA[1];
            }
            else if(txt2speechResult.contains("navigate"))
            {
                String[] strA = txt2speechResult.split("navigate", 2);
                v2txt = strA[1];
            }
            v2txt = v2txt.replaceAll("give me directions", "");
            v2txt = v2txt.replaceAll("give me direction", "");
            v2txt = v2txt.replaceAll("navigate me", "");
            v2txt = v2txt.replaceAll("give me direction", "");
            v2txt = v2txt.replaceAll(" navigate", "");
            v2txt = v2txt.replaceAll(" direction", "");
            v2txt = v2txt.replaceAll(" the", "");
            v2txt = v2txt.replaceAll(" jarvis", "");
            v2txt = v2txt.replaceAll(" j.a.r.v.i.s.", "");
            v2txt = v2txt.replaceAll(" j.a.r.v.i.s", "");
            v2txt = v2txt.replaceAll("could you please", "");
            v2txt = v2txt.replace(" to", "");
            v2txt = v2txt.replace(" a", "");
            Uri uri = Uri.parse("google.navigation:?q="+Uri.encode(v2txt));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            CloverAI.this.startActivity(intent);
        }
        else if (txt2speechResult.contains("open")) {
            String[] strA = txt2speechResult.split("open", 2);
            String v2txt = txt2speechResult;
            String v3txt = txt2speechResult;
            v2txt = v2txt.replace("open", "");
            v2txt = v2txt.replace(" ", "");
            v2txt = v2txt.replace("", "");
            v2txt = strA[1];
            List<ApplicationInfo> applicationsInfo;
            PackageManager manager = getPackageManager();
            boolean isMatch = false;
            ApplicationInfo appInfo = null;
            String appName = null;

            try {
                applicationsInfo = manager.getInstalledApplications(0);
                if (applicationsInfo == null)
                    throw new PackageManager.NameNotFoundException();

                for (int i = 0; i < applicationsInfo.size(); i++) {
                    appInfo = applicationsInfo.get(i);

                    appName = (String) manager.getApplicationLabel(appInfo);

                    if (appName.toLowerCase().contains(v2txt.toLowerCase())) {
                        isMatch = true;
                        break;
                    }
                }
                if (isMatch) {
                    speakText("Opening application " + appName);
                    Intent i = manager.getLaunchIntentForPackage(appInfo.packageName);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                } else {
                    Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
                    i.putExtra(SearchManager.QUERY, v3txt);
                    startActivity(i);
                }


            } catch (PackageManager.NameNotFoundException e) {
                speakText("I'm sorry, cannot find application " + v2txt);
            }
        }
        else if(txt2speechResult.contains("what food can i make with")||txt2speechResult.contains("what recipe can i make with")||txt2speechResult.contains("what food can be made with")||txt2speechResult.contains("what recipe can be made with")||txt2speechResult.contains("search for recipies"))
        {
            String v2txt= txt2speechResult;
            v2txt = v2txt.replaceAll("what food can i make with", "");
            v2txt = v2txt.replaceAll("what recipe can i make with", "");
            v2txt = v2txt.replaceAll("what food can be made with", "");
            v2txt = v2txt.replaceAll("what food can be made with", "");
            v2txt = v2txt.replaceAll("search for recipies", "");
            v2txt = v2txt.replaceAll(" "," ");
            v2txt = v2txt.replaceAll(".", "");
            v2txt = v2txt.replaceAll("\\?", "");
            String url = "https://www.allrecipes.com/search/results/?wt="+v2txt+"&sort=re";
            Intent intent = new Intent(CloverAI.this, RWebView.class);
            intent.putExtra("Type", "Recipe");
            intent.putExtra("URL", url);
            speakText("Take a look");
            CloverAI.this.startActivity(intent);
            CloverAI.this.startActivity(intent);
        }
        else if(txt2speechResult.contains("switch on")||txt2speechResult.contains("enable")||txt2speechResult.contains("on"))
        {
            if(txt2speechResult.contains("bluetooth"))
            {
                speakText("Bluetooth is enabled");
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                mBluetoothAdapter.enable();
            }
            if(txt2speechResult.contains("wifi")||txt2speechResult.contains("wi-fi"))
            {
                speakText("Wifi is enabled");
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
            }
            if(txt2speechResult.contains("location")||txt2speechResult.contains("gps"))
            {
                speakText("Here are the settings");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                CloverAI.this.startActivity(intent);
            }
            if(txt2speechResult.contains("data"))
            {

                speakText("Here are the settings");
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                CloverAI.this.startActivity(intent);
            }
            if(txt2speechResult.contains("aeroplane")||txt2speechResult.contains("airplane"))
            {

                speakText("Here are the settings");
                Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                CloverAI.this.startActivity(intent);
            }
        }
        else if(txt2speechResult.contains("switch off") ||txt2speechResult.contains("disable")||txt2speechResult.contains("off"))
        {
            if(txt2speechResult.contains("bluetooth"))
            {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.disable();
                    speakText("Bluetooth is now disabled");
                }
                else
                {
                    speakText("Bluetooth was already disabled");
                }
            }
            if(txt2speechResult.contains("wifi")||txt2speechResult.contains("wi-fi"))
            {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean wifiEnabled = wifiManager.isWifiEnabled();
                if(wifiEnabled=true)
                {
                    wifiManager.setWifiEnabled(false);
                    speakText("Wifi is now disabled");
                }
            }
            if(txt2speechResult.contains("location")||txt2speechResult.contains("gps"))
            {
                speakText("Here are the settings");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                CloverAI.this.startActivity(intent);
            }
            if(txt2speechResult.contains("data"))
            {

                speakText("Here are the settings");
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                CloverAI.this.startActivity(intent);
            }
            if(txt2speechResult.contains("aeroplane")||txt2speechResult.contains("airplane"))
            {

                speakText("Here are the settings");
                Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                CloverAI.this.startActivity(intent);
            }
        }
    }

}
