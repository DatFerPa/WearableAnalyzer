package com.getdataapptfgwearable.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


public class ServiceSensorHeart extends Service implements SensorEventListener {

    private static final String TAG = "ServiceSensorHeart";
    private int[] grupoHeartRate;
    private SensorManager sensorManager;
    private Sensor sensor;
    private int contadorActualHeart;
    public ServiceSensorHeart(){}
    PowerManager.WakeLock wakeLock;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Creando servicio");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"GetDataAppTFGWereable:WakeLockNoMovimiento");
        wakeLock.acquire();
        contadorActualHeart = 0;
        grupoHeartRate = new int[] {-1,-1,-1,-1,-1};
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_FASTEST);
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean rateHeartNormal(){
        int cont = 0;
        double media =0;
        for(int oneRate: grupoHeartRate){
            if(oneRate!=-1) {
                media += oneRate;
                cont++;
            }
        }
        if(cont==0){
            media = 0;
            cont = 1;
        }
        media = media/cont;
        Log.d(TAG,"Latidos corazon de media entre "+cont+"   ---->  "+media);
        return ((media<60)||(media>100))?false:true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG,"Pulsaciones normales: "+String.valueOf(rateHeartNormal()));
        String texto = String.valueOf(event.values[0]);
        Toast.makeText(getApplicationContext(),texto,Toast.LENGTH_LONG).show();
        if(contadorActualHeart>=5){
            contadorActualHeart = 0;
        }
        grupoHeartRate[contadorActualHeart]= (int) event.values[0];
        contadorActualHeart++;
        Log.d(TAG,texto);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Finalizando sensor corazon");
        sensorManager.unregisterListener(this);
        wakeLock.release();
        super.onDestroy();
    }
}
