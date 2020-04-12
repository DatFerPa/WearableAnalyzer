package com.detectorcaidas.services;


import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.CountDownTimer;
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
import com.detectorcaidas.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

public class ServiceFallingSensor extends Service implements SensorEventListener {
    public static final int NOTIFICATION_ID = 101;


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
    private CountDownTimer tiempoHastaLlamada;


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
                            Log.d(TAG,"respuesta fuera");

                            //ademas de que la respuesta sea que nos hemos caido
                            if(!MainActivity.caidaBool) {
                                Log.d(TAG,"respuesta dentro");
                                MainActivity.caidaBool = true;
                                int requestID = (int) System.currentTimeMillis();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.CANAL_NOTIFICACION_ID);
                                notificationBuilder.setContentTitle("¿Se encuentra usted bien?");
                                notificationBuilder.setContentText("Pulse para cancelar la llamada de emergencia");
                                notificationBuilder.setSmallIcon(R.drawable.walkingicon);
                                notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                                notificationBuilder.setVibrate(new long[]{500,500});
                                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.walkingicon));
                                notificationBuilder.setContentIntent(pendingIntent);
                                Notification notification = notificationBuilder.build();

                                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                                notificationManagerCompat.notify(NOTIFICATION_ID, notification);

                                tiempoHastaLlamada = new CountDownTimer(30000, 500) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        Log.d(TAG, "time to finish: " + millisUntilFinished);
                                    }

                                    @Override
                                    public void onFinish() {
                                        if (MainActivity.caidaBool) {
                                            Log.d(TAG, "movidas de llamadas");
                                            makeCallAndSMS();
                                            Intent intent1 = new Intent();
                                            intent1.setAction("com.detectorcaidas");
                                            intent1.putExtra("data", "recuperar");
                                            sendBroadcast(intent1);
                                            //ya no nos habremos caido
                                            MainActivity.caidaBool = false;
                                        }
                                    }
                                };

                                if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {

                                    Log.d(TAG, "tamos fuera de la app");

                                    Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                                    intent2.putExtra(FUERA, ESTAS_FUERA_DE_LA_PRINCIPAL);
                                    intent2.setAction(Intent.ACTION_MAIN);
                                    intent2.addCategory(Intent.CATEGORY_LAUNCHER);
                                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    startActivity(intent2);
                                    Log.d(TAG, response);
                                } else if(MainActivity.onpause) {
                                    Intent intent1 = new Intent();
                                    intent1.setAction("com.detectorcaidas");
                                    intent1.putExtra("data", "finalizar");
                                    sendBroadcast(intent1);
                                }else {
                                    Log.d(TAG, "seguinmos dentro de la app");
                                    Log.d(TAG, response);
                                    Intent intent1 = new Intent();
                                    intent1.setAction("com.detectorcaidas");
                                    intent1.putExtra("data", "caida");
                                    sendBroadcast(intent1);
                                }
                                //Nos acabamos de caer

                                tiempoHastaLlamada.start();
                            }
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
                    params.put("accel",prueba);
                    return params;
                }
            };


            queue.add(stringRequest);
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

    private void makeCallAndSMS(){
        Log.d(TAG, "Realizar llamada desde el wearable");
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:648738746"));
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        }else{
            Toast.makeText(getApplicationContext(),"No se ha podido realizar la llamada",Toast.LENGTH_LONG).show();
        }
        Intent intent1 = new Intent();
        intent1.setAction("com.detectorcaidas");
        intent1.putExtra("data","recuperar");
        sendBroadcast(intent1);
    }



}
