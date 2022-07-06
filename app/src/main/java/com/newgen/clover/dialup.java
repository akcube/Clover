package com.newgen.clover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class dialup extends Activity {
    public static final int CONTACT_QUERY_LOADER = 0;
    public static final String QUERY_KEY = "query";
    String query;
    Intent intentthatcalled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.dialup_l);
        intentthatcalled = getIntent();
        query = intentthatcalled.getStringExtra("phnoforq");
        startQuery();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        query = intent.getStringExtra("phnoforq");
        startQuery();
        Log.e("DU", "onNewIntent is called");
        Log.e("DU","intent extra ="+intent.getStringExtra("phnoforq")) ;
    }

    private void startQuery()
    {
        // We need to create a bundle containing the query string to send along to the
        // LoaderManager, which will be handling querying the database and returning results.
        if(query!="")
        {
            Log.e("DU","startQuery has launched");
            Log.e("DU", "query = " + query);
            Bundle bundle = new Bundle();
            bundle.putString(QUERY_KEY, query);

            ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(this);

            // Start the loader with the new query, and an object that will handle all callbacks.
            getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle,
                    loaderCallbacks);
        }
    }
}
