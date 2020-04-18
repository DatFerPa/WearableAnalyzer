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
import java.util.ArrayList;
import java.util.List;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import org.tensorflow.lite.Interpreter;

public class ServiceFallingSensor extends Service implements SensorEventListener {
    public static final int NOTIFICATION_ID = 101;
    private static final String TAG = "ServiceFallingSensor";

    private String prueba = "1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1";

    //sensores
    private SensorManager sensorManager;
    //acelerometro
    private double[] gravity = new double[3];
    private float[] linear_acceleration = new float[3];
    List<float[]> lst_linear_acc = new ArrayList<>();
    private Sensor accelerometer;
    //corazon
    private Sensor sensorHeart;
    private int[] grupoHeartRate;
    //control de los servicios
    int contadorActualAccel = 0;
    int contadorActualHeart=0;
    public final static int ESTAS_FUERA_DE_LA_PRINCIPAL = 1;
    public final static String SERVICIO = "servicio";
    public final static String FUERA = "fueraPrincipal";
    private String accel_para_enviar = "";
    private CountDownTimer tiempoHastaLlamada;
    //Tensorflow lite
    Interpreter interpreter;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        grupoHeartRate = new int[5];
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Crear el sensor del acelerómetro
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //Crear el sensor del ratio del corazón
        sensorHeart = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this,sensorHeart,SensorManager.SENSOR_DELAY_FASTEST);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double alpha = 0.8;

        if(contadorActualAccel >= 1000 ){
            contadorActualAccel = 0;
            Log.d(TAG, "Empezando uso de red neuronal en android");




            Log.d(TAG, "Finalizando uso de red neuronal en android");
        }


        /*
            Hay que poner un diferenciador entre el sensor del acelerometro y el del pulso
         */

        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

            linear_acceleration[0] = (float) (sensorEvent.values[0] - gravity[0]);
            linear_acceleration[1] = (float) (sensorEvent.values[1] - gravity[1]);
            linear_acceleration[2] = (float) (sensorEvent.values[2] - gravity[2]);

            if(contadorActualAccel == 0){
                accel_para_enviar = accel_para_enviar + linear_acceleration[0]+";"+linear_acceleration[1]+";"+linear_acceleration[2];
            }else{
                accel_para_enviar = ":"+accel_para_enviar + linear_acceleration[0]+";"+linear_acceleration[1]+";"+linear_acceleration[2];
            }

            //aqui vamos a añadir un linear acceleration a la lista
            lst_linear_acc.add(linear_acceleration.clone());

            contadorActualAccel++;

        }else{
            if(contadorActualHeart>5){
                contadorActualHeart = 0;
            }
            grupoHeartRate[contadorActualHeart] = (int)sensorEvent.values[0];
            contadorActualHeart++;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
