package com.detectorcaidas;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.ImageButton;
import android.widget.TextView;

public class ActivityCancellCall extends WearableActivity {

    private TextView mTextView;
    private ImageButton botonOk;
    private ImageButton botonCancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancell_call);

        mTextView = (TextView) findViewById(R.id.text);
        botonCancel = findViewById(R.id.botonCancel);
        botonOk = findViewById(R.id.botonOk);
        // Enables Always-on
        setAmbientEnabled();
    }
}
