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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean activo;
    private int contador;

    /*
        Cosas para probar
    */
    private double[] gravity = new double[3];
    private float[] linear_acceleration = new float[3];
    List<float[]> lst_linear_acc = new ArrayList<>();

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

        linear_acceleration[0] = (float) (sensor.values[0] - gravity[0]);
        linear_acceleration[1] = (float) (sensor.values[1] - gravity[1]);
        linear_acceleration[2] = (float) (sensor.values[2] - gravity[2]);

        Log.d(TAG,"Iteracion: "+contador+"Datos del accelerometro: X: "+ linear_acceleration[0]+" - Y: "+linear_acceleration[1]+" - Z: "+linear_acceleration[2]);

        //aqui vamos a añadir un linear acceleration a la lista
        lst_linear_acc.add(linear_acceleration.clone());
        if(contador >= 1000){
            crearFichero();
            lst_linear_acc = new ArrayList<>();
            contador =1;
        }else{contador++;}
        
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

    public void onClickCaidaNo(View view) {
        if(activo){
            //sensor activo se apoaga
            activo = false;
            sensorManager.unregisterListener(this);
            Log.d(TAG,"Finalizando la lectura de datos");
            Log.d(TAG,"Escribiendo en el fichero");


        }else{

            lst_linear_acc = new ArrayList<>();
            Log.d(TAG,"Empezando la lectura de datos");
            activo = true;
            contador = 1;
            sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void onCLickCaidaSi(View view){



    }

    private void crearFichero(){
        Log.d(TAG,String.valueOf(lst_linear_acc.size()));

       Log.d(TAG,getApplicationContext().getFilesDir().getPath());

        File fichero = new File(getApplicationContext().getFilesDir(),"prueba1.txt");

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
            for(float[]a : lst_linear_acc ){

                Log.d(TAG,"Datos del accelerometro: X: "+a[0]+" - Y: "+a[1]+" - Z: "+a[2]);
                writer.write(a[0]+";"+a[1]+";"+a[2]+"\n");

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
