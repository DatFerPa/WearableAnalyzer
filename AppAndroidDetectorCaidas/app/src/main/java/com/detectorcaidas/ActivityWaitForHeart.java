package com.detectorcaidas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.detectorcaidas.services.ServiceFallingSensor;

import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivityWaitForHeart extends WearableActivity {

    private static final String TAG = "ActivityWaitForHeart";


    private ImageView imagenCorazon;
    public static boolean isWaitingForHeart;


    public class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            if("listosParaTurno".equals(intent.getStringExtra("data"))) {
                isWaitingForHeart=false;
                MainActivity.isTurnoEmpezado = true;
                finish();
                Toast.makeText(getApplicationContext(),"Su pulso ya ha sido medido",Toast.LENGTH_LONG).show();
            }
        }
    }
    BroadcastReceiver broadcastReceiver = new ActivityWaitForHeart.MyBroadcastReceiver();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isWaitingForHeart = true;
        setContentView(R.layout.layout_activity_wait_for_heart);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.detectorcaidas");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);
        MainActivity.intentService = new Intent(this, ServiceFallingSensor.class);
        startService(MainActivity.intentService);

        imagenCorazon = findViewById(R.id.imagenCorazon);
        AnimationDrawable animation = (AnimationDrawable)imagenCorazon.getDrawable();
        animation.start();



        // Enables Always-on
        setAmbientEnabled();

    }

    @Override
    protected void onPause() {
        Log.d(TAG,"on pause");
        if(ActivityWaitForHeart.isWaitingForHeart){
            Log.d(TAG,"salimos de waiting heart mientras el service estaba activo");
            stopService(MainActivity.intentService);
            isWaitingForHeart=false;
            MainActivity.isTurnoEmpezado = false;
        }
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG,"on destroy");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
