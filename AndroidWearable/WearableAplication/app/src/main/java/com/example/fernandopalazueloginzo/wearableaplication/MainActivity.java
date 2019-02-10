package com.example.fernandopalazueloginzo.wearableaplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends WearableActivity implements SensorEventListener {
    private  SensorManager mSensorManager;
    private  Sensor mAccelerometer;
    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;
    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];
    private StringBuilder sb;
    private boolean active = false;

//hay que limitar las lecturas de los sensores, que si no esta wea peta
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sb = new StringBuilder();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);

    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        final double alpha = 0.8;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        sb.append("===============================\n");
        sb.append("timestamp"+System.currentTimeMillis());
        sb.append("Valor de los linear\n");
        sb.append("linear de la x: "+ linear_acceleration[0]+"\n");
        sb.append("linear de la y: "+ linear_acceleration[1]+"\n");
        sb.append("linear de la z: "+ linear_acceleration[2]+"\n");


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void leerDatos(View view) {
        if(active){
            active = false;
            mSensorManager.unregisterListener(this);
            System.out.println("Se acaban de leer los datos");
            System.out.println("Todo el chorro de datos:");
            //System.out.println(sb.toString());

        }else{
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            active = true;
            System.out.println("Empezamos a leer los datos");
        }
    }
}
