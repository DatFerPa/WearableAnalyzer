package com.getdataapptfgwearable;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.getdataapptfgwearable.service.ServiceSensorNoMovimiento;
import com.getdataapptfgwearable.service.ServiceSensorSiMovimiento;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity  {

    private static final String TAG = "MainActivity";
    private boolean activo;
    private Button botonSi;
    private Button botonNo;
    public static Intent intent;
    public static List<List<float[]>> listaDeListas;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,intent.getStringExtra("data"));
            Toast.makeText(getApplicationContext(),intent.getStringExtra("data"),Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)!= PackageManager.PERMISSION_GRANTED){
            String[] permisos = {Manifest.permission.BODY_SENSORS};
            requestPermissions(permisos, PackageManager.PERMISSION_GRANTED);
        }
        botonSi = (Button) findViewById(R.id.buttonMovimientoSI);
        botonNo = (Button) findViewById(R.id.buttonMovimientoNo);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.getdataapptfgwearable");
        registerReceiver(broadcastReceiver,intentFilter);
        listaDeListas = new ArrayList<>();
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
          crearFichero("movimientono");
      }else{
          Log.d(TAG,"Empezando la lectura de datos no caida");
          activo=true;
          botonSi.setVisibility(View.INVISIBLE);
          intent = new Intent(this, ServiceSensorNoMovimiento.class);
          startService(intent);
      }
    }

    public void onCLickCaidaSi(View view){

        if(activo){
            activo = false;
            Log.d(TAG,"Finalizando la lectura de datos si caida");
            botonNo.setVisibility(View.VISIBLE);
            stopService(intent);
            crearFichero("movimientosi");
        }else{
            Log.d(TAG,"Empezando la lectura de datos si caida");
            activo = true;
            botonNo.setVisibility(View.INVISIBLE);
            intent = new Intent(this, ServiceSensorSiMovimiento.class);
            startService(intent);
        }
    }

    private void crearFichero(String nombre){
        for(List<float[]> lista:listaDeListas){
            Log.d(TAG, String.valueOf(lista.size()));
            Log.d(TAG, getApplicationContext().getFilesDir().getPath());
            File fichero = new File(getApplicationContext().getFilesDir(), nombre + System.currentTimeMillis() + ".txt");
            Log.d(TAG, "Guardando fichero de "+nombre);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
                for (float[] a : lista) {
                    writer.write(a[0] + ";" + a[1] + ";" + a[2] + "\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
