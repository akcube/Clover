package com.newgen.clover;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String result = remoteMessage.getNotification().getBody();
        if (result.contains("Codex007a11")) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:36005322"));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            MyFirebaseMessagingService.this.startActivity(intent);
        }
        else {
            Log.e("TAG", result);
            Synthesizer m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
            if (m_syn == null) {
                // Create Text To Speech Synthesizer.
                m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
            }
            m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
            Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
            m_syn.SetVoice(v, null);
            m_syn.SpeakToAudio(result);
        }
    }
}
