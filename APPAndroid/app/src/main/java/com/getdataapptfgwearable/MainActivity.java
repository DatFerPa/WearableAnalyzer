package com.getdataapptfgwearable;

import android.content.Context;
import android.content.Intent;
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

import com.getdataapptfgwearable.service.ServiceSensorCaidaNo;
import com.getdataapptfgwearable.service.ServiceSensorCaidaSi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity  {

    private static final String TAG = "MainActivity";

    private int iteracionesParaFichero = 1000;
    /*
        Cosas para probar
    */


    private boolean activo;
    private Button botonSi;
    private Button botonNo;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setAmbientEnabled();

        botonSi = (Button) findViewById(R.id.buttonCaidaSI);
        botonNo = (Button) findViewById(R.id.buttonCaidaNo);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void onClickCaidaNo(View view) {
      if(activo){
          Log.d(TAG,"Finalizando la lectura de datos no caida");
          activo = false;
          botonSi.setVisibility(View.VISIBLE);
          stopService(intent);
      }else{
          Log.d(TAG,"Empezando la lectura de datos no caida");
          activo=true;
          botonSi.setVisibility(View.INVISIBLE);
          intent = new Intent(this, ServiceSensorCaidaNo.class);
          startService(intent);
      }
    }

    public void onCLickCaidaSi(View view){
        if(activo){
            activo = false;
            Log.d(TAG,"Finalizando la lectura de datos si caida");
            botonNo.setVisibility(View.VISIBLE);
            stopService(intent);
        }else{
            Log.d(TAG,"Empezando la lectura de datos si caida");
            activo = true;
            botonNo.setVisibility(View.INVISIBLE);
            intent = new Intent(this, ServiceSensorCaidaSi.class);
            startService(intent);
        }
    }

}
