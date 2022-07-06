package com.newgen.clover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class RWebView extends AppCompatActivity{

    android.webkit.WebView rwebview;
    Intent intentThatCalled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_reusable);
        Log.e("TAGGE", "TEGGE");
        rwebview = (WebView)findViewById(R.id.rWebView);
        intentThatCalled = getIntent();
        if(intentThatCalled.getStringExtra("Type").equals("Recipe"))
        {
            loadRecipe();
        }
        else if(intentThatCalled.getStringExtra("Type").equals("Events"))
        {
            loadEvents();
        }
    }
    public void loadEvents()
    {
        String query = intentThatCalled.getStringExtra("URL");
        Log.e("tag", "URL is "+query);
        rwebview.setWebChromeClient(new WebChromeClient());
        rwebview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;

            }
        });
        // Load the webpage
        rwebview.loadUrl(query);
    }
    public void loadRecipe()
    {
        String query = intentThatCalled.getStringExtra("URL");
        Log.e("tag", "URL is"+query);
        rwebview.setWebChromeClient(new WebChromeClient());
        rwebview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;

            }
        });
        // Load the webpage
        rwebview.loadUrl(query);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && rwebview.canGoBack()) {
            rwebview.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
