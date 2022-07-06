package com.newgen.clover;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class mute_services extends AppCompatActivity {
    TextView textView;
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mute_services_layout);
        textView = (TextView) findViewById(R.id.mute_s_tv);
            if (isNetworkAvailable()) {
                textView.setText("Connection established.");
            } else {
                textView.setText("No internet connection available. Please connect to an active Internet connection and try again.");
            }
    }
}
