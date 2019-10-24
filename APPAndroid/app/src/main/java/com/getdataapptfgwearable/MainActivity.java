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
import android.widget.Button;
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
    private boolean caida;
    private int iteracionesParaFichero = 1000;
    /*
        Cosas para probar
    */
    private double[] gravity = new double[3];
    private float[] linear_acceleration = new float[3];
    List<float[]> lst_linear_acc = new ArrayList<>();
    private Button botonSi;
    private Button botonNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializacion del sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Enables Always-on
        setAmbientEnabled();

        botonSi = (Button) findViewById(R.id.buttonCaidaSI);
        botonNo = (Button) findViewById(R.id.buttonCaidaNo);
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
        if(!caida && contador >= iteracionesParaFichero){
            crearFicheroCaidaNo();
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
        /*
            La idea es ir haciendo lecturas de 1000 en 1000 (por ejemplo), y asi una vez se hagan 1000 lecturas,
            guardamos un fichero
         */
        if(activo){
            //sensor activo se apoaga
            activo = false;
            sensorManager.unregisterListener(this);
            Log.d(TAG,"Finalizando la lectura de datos no caida");
            botonSi.setVisibility(View.VISIBLE);
        }else{

            lst_linear_acc = new ArrayList<>();
            Log.d(TAG,"Empezando la lectura de datos no caida");
            activo = true;
            caida = false;
            contador = 1;
            botonSi.setVisibility(View.INVISIBLE);
            sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void onCLickCaidaSi(View view){
        /*
            Mi idea es que demos al boton de caida si, es que se empieze a hacer el registro de los datos
            una vez le demos otra vez al boton, dejaremos de hacer el registro de datos y cojeremos los ultimos x valores para hacer
            el fichero de la caida.
            problemas:
                quiza la cantidad de datos sean pocos por ficheros.
                problema a que quiza no se recoja bien toda la curva de la caida
         */
        if(activo){
            crearFicheroCaidaSi();
            activo = false;
            sensorManager.unregisterListener(this);
            Log.d(TAG,"Finalizando la lectura de datos si caida");
            botonNo.setVisibility(View.VISIBLE);

        }else{
            lst_linear_acc = new ArrayList<>();
            Log.d(TAG,"Empezando la lectura de datos si caida");
            activo = true;
            caida = true;
            contador = 1;
            botonNo.setVisibility(View.INVISIBLE);
            sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private void crearFicheroCaidaSi() {
        /*
            ¿Que pasa si el numero de datos es menor? -> Tengo que añadir mas datos por detras al array
         */
        Log.d(TAG,String.valueOf(lst_linear_acc.size()));

        Log.d(TAG,getApplicationContext().getFilesDir().getPath());

        File fichero = new File(getApplicationContext().getFilesDir(),"caidasi"+System.currentTimeMillis()+".txt");

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
            //al reves
            //int i = lst_linear_acc.size()-1;i>=lst_linear_acc.size()-iteracionesParaFichero;i--
            for(int i = lst_linear_acc.size()-iteracionesParaFichero;i<=lst_linear_acc.size()-1;i++){

                float[] datoConcreto = lst_linear_acc.get(i);
                Log.d(TAG,"Datos del accelerometro: X: "+datoConcreto[0]+" - Y: "+datoConcreto[1]+" - Z: "+datoConcreto[2]);
                writer.write(datoConcreto[0]+";"+datoConcreto[1]+";"+datoConcreto[2]+"\n");

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void crearFicheroCaidaNo(){
        Log.d(TAG,String.valueOf(lst_linear_acc.size()));

       Log.d(TAG,getApplicationContext().getFilesDir().getPath());

        File fichero = new File(getApplicationContext().getFilesDir(),"caidano"+System.currentTimeMillis()+".txt");
        Log.d(TAG,"Guardando fichero de No caida");
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
            for(float[]a : lst_linear_acc ){

                //Log.d(TAG,"Datos del accelerometro para fichero de no caida: X: "+a[0]+" - Y: "+a[1]+" - Z: "+a[2]);
                writer.write(a[0]+";"+a[1]+";"+a[2]+"\n");

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
