package com.getdataapptfgwearable;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean activo;

    /*
        Cosas para probar
    */
    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializacion del sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent sensor) {
        /*
        Log.d(TAG,"Datos del accelerometro: X: "+ sensor.values[0]+" - Y: "+sensor.values[1]+" - Z: "+sensor.values[2]);
        */

        double alpha = 0.8;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensor.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensor.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensor.values[2];

        linear_acceleration[0] = sensor.values[0] - gravity[0];
        linear_acceleration[1] = sensor.values[1] - gravity[1];
        linear_acceleration[2] = sensor.values[2] - gravity[2];

        Log.d(TAG,"Datos del accelerometro: X: "+ linear_acceleration[0]+" - Y: "+linear_acceleration[1]+" - Z: "+linear_acceleration[2]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(activo){
            activo = false;
            sensorManager.unregisterListener(this);
        }
    }

    public void onClickGetData(View view) {
        if(activo){
            //sensor activo se apoaga
            activo = false;
            sensorManager.unregisterListener(this);
            Log.d(TAG,"Finalizando la lectura de datos");
        }else{
            Log.d(TAG,"Empezando la lectura de datos");
            activo = true;
            sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }
}
