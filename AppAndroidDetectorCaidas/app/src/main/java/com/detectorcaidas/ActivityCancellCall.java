package com.detectorcaidas;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class ActivityCancellCall extends WearableActivity {

    private static final String TAG = "ActivityCancellCall";
    private ImageButton botonOk;
    private ImageButton botonCancel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancell_call);
        botonCancel = findViewById(R.id.botonCancel);
        botonOk = findViewById(R.id.botonOk);
        botonOk.setColorFilter(Color.BLUE);
        botonCancel.setColorFilter(Color.WHITE);
        // Enables Always-on
        setAmbientEnabled();
    }

    public void onClockOk(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:648738746"));
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        }else{
            Toast.makeText(getApplicationContext(),"No se ha podido realizar la llamada",Toast.LENGTH_LONG).show();
        }
        finish();
    }

    public void onClickCancel(View view) {
        finish();
    }
}
