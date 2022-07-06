package com.newgen.clover;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
//To do//
                            return;
                        }

// Get the Instance ID token//
                        String token = task.getResult().getToken();
                        Log.d("TAG", token);

                    }
                });
    }

    public void blind(View view) {
        Intent i = new Intent(MainActivity.this, blind_services.class);
        startActivity(i);
    }

    public void deaf(View view) {
        Intent i = new Intent(MainActivity.this, auditory_impairment.class);
        startActivity(i);
    }

    public void mute(View view) {
        Intent i = new Intent(MainActivity.this, mute_services.class);
        startActivity(i);
    }

    public void deafblind(View view) {
        Intent i = new Intent(MainActivity.this, tactile_services.class);
        startActivity(i);
    }

    public void AIREVEAL(View view) {
        Intent i = new Intent(MainActivity.this, CloverAI.class);
        startActivity(i);
    }
}
