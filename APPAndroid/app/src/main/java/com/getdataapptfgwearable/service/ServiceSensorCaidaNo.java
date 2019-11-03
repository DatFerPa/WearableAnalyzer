package com.getdataapptfgwearable.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceSensorCaidaNo extends Service implements SensorEventListener {

    private static final String TAG = "ServiceSensorCaidaNo";
    private int contador;
    private double[] gravity = new double[3];
    private float[] linear_acceleration = new float[3];

    List<float[]> lst_linear_acc = new ArrayList<>();

    //Sensores
    private SensorManager sensorManager;
    private Sensor accelerometer;


    public ServiceSensorCaidaNo() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
            Aqui vamos a hacer lo de crear el SensorManager
         */
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        contador = 1;

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent sensor) {
        double alpha = 0.8;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensor.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensor.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensor.values[2];

        linear_acceleration[0] = (float) (sensor.values[0] - gravity[0]);
        linear_acceleration[1] = (float) (sensor.values[1] - gravity[1]);
        linear_acceleration[2] = (float) (sensor.values[2] - gravity[2]);

        Log.d(TAG, "Datos del accelerometro: X: " + linear_acceleration[0] + " - Y: " + linear_acceleration[1] + " - Z: " + linear_acceleration[2]);

        //aqui vamos a añadir un linear acceleration a la lista
        lst_linear_acc.add(linear_acceleration.clone());

        if (contador >= 1000) {
            crearFicheroCaidaNo();
            lst_linear_acc = new ArrayList<>();
            contador = 1;
        } else {
            contador++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Finalizando servicio");
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    private void crearFicheroCaidaNo() {

        Log.d(TAG, String.valueOf(lst_linear_acc.size()));

        Log.d(TAG, getApplicationContext().getFilesDir().getPath());

        File fichero = new File(getApplicationContext().getFilesDir(), "caidano" + System.currentTimeMillis() + ".txt");
        Log.d(TAG, "Guardando fichero de No caida");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
            for (float[] a : lst_linear_acc) {

                //Log.d(TAG,"Datos del accelerometro para fichero de no caida: X: "+a[0]+" - Y: "+a[1]+" - Z: "+a[2]);
                writer.write(a[0] + ";" + a[1] + ";" + a[2] + "\n");

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}