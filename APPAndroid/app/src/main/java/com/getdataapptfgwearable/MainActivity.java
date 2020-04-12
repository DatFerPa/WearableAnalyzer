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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.getdataapptfgwearable.service.ServiceSensorNoMovimiento;
import com.getdataapptfgwearable.service.ServiceSensorSiMovimiento;
import com.getdataapptfgwearable.service.ServiceSensorHeart;

import java.util.HashMap;
import java.util.Map;


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
        //probando el sacar los datos
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://servidorhombremuerto.herokuapp.com/turnos/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null) {
                    Log.d(TAG, error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String>  params = new HashMap<String, String>();
                params.put("nombre","Fer");
                return params;
            }
        };

        queue.add(stringRequest);

        /*
      if(activo){
          Log.d(TAG,"Finalizando la lectura de datos no caida");
          activo = false;
          botonSi.setVisibility(View.VISIBLE);
          stopService(intent);
      }else{
          Log.d(TAG,"Empezando la lectura de datos no caida");
          activo=true;
          botonSi.setVisibility(View.INVISIBLE);
          intent = new Intent(this, ServiceSensorHeart.class);
          startService(intent);
      }
         */

    }

    public void onCLickCaidaSi(View view){
        //probando el login
        String url = "https://servidorhombremuerto.herokuapp.com/login/";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null) {
                    Log.d(TAG, error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String>  params = new HashMap<String, String>();
                params.put("nombre","Fer");
                return params;
            }
        };

        queue.add(stringRequest);


        /*
        if(activo){
            activo = false;
            Log.d(TAG,"Finalizando la lectura de datos si caida");
            botonNo.setVisibility(View.VISIBLE);
            stopService(intent);
        }else{
            Log.d(TAG,"Empezando la lectura de datos si caida");
            activo = true;
            botonNo.setVisibility(View.INVISIBLE);
            intent = new Intent(this, ServiceSensorNoMovimiento.class);
            startService(intent);
        }
         */
    }

}
