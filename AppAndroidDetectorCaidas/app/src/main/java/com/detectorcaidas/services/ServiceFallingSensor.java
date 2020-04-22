package com.detectorcaidas.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
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
import com.detectorcaidas.MainActivity;
import com.detectorcaidas.R;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.tensorflow.lite.Interpreter;

public class ServiceFallingSensor extends Service implements SensorEventListener {
    public static final int NOTIFICATION_ID = 101;
    private static final String TAG = "ServiceFallingSensor";

    private String prueba = "1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1";
    private boolean si = false;
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
    int contadorActualAccel;
    int contadorActualHeart;
    public final static int ESTAS_FUERA_DE_LA_PRINCIPAL = 1;
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
        //Crear el interpreter
        try{
            AssetFileDescriptor fileDescriptor = getApplication().getAssets().openFd("converted_model.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,fileDescriptor.getStartOffset(),fileDescriptor.getDeclaredLength());
            interpreter = new Interpreter(mappedByteBuffer);
        }catch(IOException e){
            Log.e(TAG,e.getMessage());
        }
        contadorActualAccel =1;
        contadorActualHeart = 0;
        //Creacion de cosas relacionadas con los sensores
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

            if(contadorActualAccel >= 1000 && si== false){
                si =true;
                float[][] salida = new float[1][2];
                float[][] entrada = new float[100][];
                int cont = 0;
                for(float[] bloque:lst_linear_acc){
                    entrada[cont]=bloque;
                    cont++;
                }
                interpreter.run(entrada,salida);
                Log.d(TAG,"Salida:  CaidaSi: "+salida[0][0]+" --  CaidaNo: "+salida[0][1]);
                lst_linear_acc = new ArrayList<>();
                contadorActualAccel = 1;
                boolean esCaida = salida[0][0] >= salida[0][1]?true:false;

                if(contadorActualAccel >1000){
                    Log.d(TAG,"Si me he caido");
                    //Modificar por comprobacion del pulso
                    if(rateHeartNormal()==false){

                        Log.d(TAG,"Caida detectada con latidos fuera de lo normal");
                        MainActivity.caidaBool = true;

                        //Broadcast movida
                        Intent intentBroadcast = new Intent();
                        intentBroadcast.setAction("com.detectorcaidas");
                        intentBroadcast.putExtra("data","caida");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);

                        //Creando las notificaciones
                        int requestID = (int) System.currentTimeMillis();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.CANAL_NOTIFICACION_ID);
                        notificationBuilder.setContentTitle("¿Se encuentra usted bien?");
                        notificationBuilder.setContentText("Pulse para cancelar el freno de emergencia");
                        notificationBuilder.setSmallIcon(R.drawable.walkingicon);
                        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                        notificationBuilder.setVibrate(new long[]{500,500});
                        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.walkingicon));
                        notificationBuilder.setContentIntent(pendingIntent);
                        Notification notification = notificationBuilder.build();

                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                        notificationManagerCompat.notify(NOTIFICATION_ID, notification);

                        //cambiar para que el tiempo sea menor
                        tiempoHastaLlamada  = new CountDownTimer(30000, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                Log.d(TAG, "time to finish: " + millisUntilFinished);
                            }

                            @Override
                            public void onFinish() {
                                if (MainActivity.caidaBool) {
                                    Log.d(TAG, "movidas de llamadas");
                                    makeCallAndBrake();
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
                        }else {
                            Log.d(TAG, "seguinmos dentro de la app");
                            Intent intent1 = new Intent();
                            intent1.setAction("com.detectorcaidas");
                            intent1.putExtra("data", "caida");
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
                        }
                        tiempoHastaLlamada.start();
                    }
                }else{
                    Log.d(TAG,"No me he caido");
                    Intent intent1 = new Intent();
                    intent1.setAction("com.detectorcaidas");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
                }

            }else {
                contadorActualAccel++;
            }

        }else{
            if(contadorActualHeart>5){
                contadorActualHeart = 0;
            }
            Log.d(TAG,"Latidos corazon: "+(int)sensorEvent.values[0]);
            grupoHeartRate[contadorActualHeart] = (int)sensorEvent.values[0];
            contadorActualHeart++;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean rateHeartNormal(){
        int cont = 1;
        double media =0;
        for(int oneRate: grupoHeartRate){
            if(oneRate!=0) {
                media += oneRate;
                cont++;
            }
        }
        media = media/cont;
        Log.d(TAG,"Latidos corazon de media entre "+cont+"   ---->  "+media);
        Toast.makeText(getApplicationContext(),String.valueOf(media),Toast.LENGTH_SHORT).show();

        return ((media<30)||(media>100))?false:true;

    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"Finalizando servicio de deteccion de caidas");
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    private void makeCallAndBrake(){
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
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
    }



}
