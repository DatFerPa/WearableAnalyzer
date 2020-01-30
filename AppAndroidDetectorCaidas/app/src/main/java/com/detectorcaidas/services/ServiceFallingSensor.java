package com.detectorcaidas.services;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.detectorcaidas.MainActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

public class ServiceFallingSensor extends Service implements SensorEventListener {
    private String prueba = "1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1";
    private static final String TAG = "ServiceFallingSensor";
    //sensores
    private double[] gravity = new double[3];
    private float[] linear_acceleration = new float[3];
    List<float[]> lst_linear_acc = new ArrayList<>();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    //control de los servicios
    int contadorActual = 0;
    public final static int ESTAS_FUERA_DE_LA_PRINCIPAL = 1;
    public final static String SERVICIO = "servicio";
    public final static String FUERA = "fueraPrincipal";
    private String accel_para_enviar = "";


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensor) {
        double alpha = 0.8;

        if(contadorActual >= 1000 ){
            contadorActual = 0;
            Log.d(TAG, "preparando la llamada al servidor");


            /*
                Aqui tiene que ir lo de la peticion al servidor
                COn la respuesta, tenemos que hacer lo de relanzar la app o no,
                 en funcion de si esta fura de foco
             */

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://neuralnetworkmobile.herokuapp.com/hasfallen/";




            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {


                            if(ProcessLifecycleOwner.get().getLifecycle().getCurrentState()== Lifecycle.State.CREATED) {
                                Log.d(TAG, "tamos fuera de la app");


                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                                intent.putExtra(FUERA, ESTAS_FUERA_DE_LA_PRINCIPAL);
                                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                                Log.d(TAG, response);
                            }else{
                                Log.d(TAG, "seguinmos dentro de la app");
                                Log.d(TAG, response);

                                Intent intent1 = new Intent();
                                intent1.setAction("com.detectorcaidas");
                                intent1.putExtra("data","cosi");
                                sendBroadcast(intent1);
                                //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                            }


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                protected Map<String, String> getParams(){
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("accel",prueba);
                    return params;
                }
            };


            queue.add(stringRequest);



/*
            Intent intent1 = new Intent();
            intent1.setAction("com.detectorcaidas");
            intent1.putExtra("data","cosi");
            sendBroadcast(intent1);
*/
            Log.d(TAG, "llamada al servidor hecha");


        }



        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensor.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensor.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensor.values[2];

        linear_acceleration[0] = (float) (sensor.values[0] - gravity[0]);
        linear_acceleration[1] = (float) (sensor.values[1] - gravity[1]);
        linear_acceleration[2] = (float) (sensor.values[2] - gravity[2]);

        if(contadorActual == 0){
            accel_para_enviar = accel_para_enviar + linear_acceleration[0]+";"+linear_acceleration[1]+";"+linear_acceleration[2];
        }else{
            accel_para_enviar = ":"+accel_para_enviar + linear_acceleration[0]+";"+linear_acceleration[1]+";"+linear_acceleration[2];
        }

        //aqui vamos a añadir un linear acceleration a la lista
        lst_linear_acc.add(linear_acceleration.clone());

        contadorActual++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Finalizando servicio de deteccion de caidas");
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }
}
