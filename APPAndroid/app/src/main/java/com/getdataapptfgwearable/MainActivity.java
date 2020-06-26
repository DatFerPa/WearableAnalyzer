/**
 * @author Fernando Palazuelo Ginzo - UO244588
 */
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

import com.getdataapptfgwearable.service.ServiceSensorMovimiento;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity {

    private static final String TAG = "MainActivity";
    private boolean activo;
    private Button botonSi;
    private Button botonNo;
    public static Intent intent;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getStringExtra("data"));
            Toast.makeText(getApplicationContext(), intent.getStringExtra("data"), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            String[] permisos = {Manifest.permission.BODY_SENSORS};
            requestPermissions(permisos, PackageManager.PERMISSION_GRANTED);
        }
        botonSi = (Button) findViewById(R.id.buttonMovimientoSI);
        botonNo = (Button) findViewById(R.id.buttonMovimientoNo);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.getdataapptfgwearable");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Método encargado de inciar la lectura para crear ficheros con
     * @param view
     */
    public void onClickNoMovimiento(View view) {

        if (activo) {
            Toast.makeText(getApplicationContext(), "click", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Finalizando la lectura de datos no movimiento");
            activo = false;
            botonSi.setVisibility(View.VISIBLE);
            stopService(intent);
            Toast.makeText(getApplicationContext(), "click 2", Toast.LENGTH_LONG).show();

        } else {
            Log.d(TAG, "Empezando la lectura de datos no movimiento");
            Toast.makeText(getApplicationContext(), "lectura", Toast.LENGTH_LONG).show();
            activo = true;
            botonSi.setVisibility(View.INVISIBLE);
            intent = new Intent(this, ServiceSensorMovimiento.class);
            intent.putExtra("movimiento",false);
            startService(intent);

        }
    }

        public void onCLickSiMovimiento (View view){
            if (activo) {
                Toast.makeText(getApplicationContext(), "click", Toast.LENGTH_LONG).show();
                activo = false;
                Log.d(TAG, "Finalizando la lectura de datos si movimiento");
                botonNo.setVisibility(View.VISIBLE);
                stopService(intent);
                Toast.makeText(getApplicationContext(), "click 2", Toast.LENGTH_LONG).show();

            } else {
                Log.d(TAG, "Empezando la lectura de datos si movimiento");
                Toast.makeText(getApplicationContext(), "lectura", Toast.LENGTH_LONG).show();
                activo = true;
                botonNo.setVisibility(View.INVISIBLE);
                intent = new Intent(this, ServiceSensorMovimiento.class);
                intent.putExtra("movimiento",true);
                Toast.makeText(getApplicationContext(), "click foreground", Toast.LENGTH_LONG).show();
                Log.d(TAG, "click foreground");
                startService(intent);

            }


        }
}
