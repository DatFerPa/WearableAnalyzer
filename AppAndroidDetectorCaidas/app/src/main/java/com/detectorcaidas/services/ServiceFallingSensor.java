package com.detectorcaidas.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.detectorcaidas.ActivityWaitForHeart;
import com.detectorcaidas.MainActivity;
import com.detectorcaidas.R;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String TAG_MOVIMIENTOS = "tag_movimientos";
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
    private CountDownTimer tiempoHastaLlamada;
    //Tensorflow lite
    Interpreter interpreter;
    //Control de CPU
    PowerManager.WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"GetDataAppTFGWereable:WakeLockNoMovimiento");
        wakeLock.acquire();
        try{
            AssetFileDescriptor fileDescriptor = getApplication().getAssets().openFd("converted_model.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,fileDescriptor.getStartOffset(),fileDescriptor.getDeclaredLength());
            interpreter = new Interpreter(mappedByteBuffer);
        }catch(IOException e){
            Log.e(TAG,e.getMessage());
        }
        grupoHeartRate = new int[] {-1,-1,-1,-1,-1};
        contadorActualAccel =0;
        contadorActualHeart = 0;
        //Creacion de cosas relacionadas con los sensores
        grupoHeartRate = new int[5];
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Crear el sensor del acelerómetro
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        //Crear el sensor del ratio del corazón
        sensorHeart = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this,sensorHeart,SensorManager.SENSOR_DELAY_FASTEST);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double alpha = 0.8;
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER && ActivityWaitForHeart.isWaitingForHeart == false){
            if(contadorActualAccel >= 1000){
                Log.d(TAG,"Se han hecho 1000 lecturas");

                float[][] salida = new float[1][2];
                float[][] entrada = new float[1000][];
                int cont = 0;
                for(float[] bloque:lst_linear_acc){
                    entrada[cont]=bloque;
                    cont++;
                }
                interpreter.run(entrada,salida);
                Log.d(TAG_MOVIMIENTOS,"Salida:  Si movimiento: "+salida[0][0]+" --  no movimiento: "+salida[0][1]);
                lst_linear_acc = new ArrayList<>();
                contadorActualAccel = 0;
                boolean esNoMovimiento = salida[0][0] >= salida[0][1]?false:true;
                if(esNoMovimiento) {
                    hacerAccionesNoMovimiento();
                }else{
                    Log.d(TAG_MOVIMIENTOS,"No se ha detectado falta de movimiento");
                }

            }else {

                    gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

                    linear_acceleration[0] = (float) (sensorEvent.values[0] - gravity[0]);
                    linear_acceleration[1] = (float) (sensorEvent.values[1] - gravity[1]);
                    linear_acceleration[2] = (float) (sensorEvent.values[2] - gravity[2]);
                    Log.d(TAG, "Datos del accelerometro: " + contadorActualAccel + " ---  X: " + linear_acceleration[0] + " - Y: " + linear_acceleration[1] + " - Z: " + linear_acceleration[2]);
                    lst_linear_acc.add(linear_acceleration.clone());
                    contadorActualAccel++;
            }

        }else if(sensorEvent.sensor.getType()==Sensor.TYPE_HEART_RATE){
            if(contadorActualHeart>=5){
                contadorActualHeart = 0;
            }
            Log.d(TAG,"Latidos corazon: "+(int)sensorEvent.values[0]);
            grupoHeartRate[contadorActualHeart] = (int)sensorEvent.values[0];
            contadorActualHeart++;
            if(ActivityWaitForHeart.isWaitingForHeart){
                Intent intent1 = new Intent();
                intent1.setAction("com.detectorcaidas");
                intent1.putExtra("data", "listosParaTurno");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean rateHeartNormal(){
        int cont = 0;
        double media =0;
        for(int oneRate: grupoHeartRate){
            if(oneRate!=-1) {
                media += oneRate;
                cont++;
            }
        }
        if(cont==0){
            media = 0;
            cont = 1;
        }
        media = media/cont;
        Log.d(TAG,"Latidos corazon de media entre "+cont+"   ---->  "+media);
        Toast.makeText(getApplicationContext(),String.valueOf(media),Toast.LENGTH_SHORT).show();
        return ((media<60)||(media>100))?false:true;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"Finalizando servicio de deteccion de caidas");
        sensorManager.unregisterListener(this);
        wakeLock.release();
        super.onDestroy();
    }

    private void makeCallAndBrake(){
        Log.d(TAG, "Activando los frenos");
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:648738746"));

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
            Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        }else{
            Toast.makeText(getApplicationContext(),"No se ha podido realizar la llamada",Toast.LENGTH_LONG).show();
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat hourFormat = new SimpleDateFormat(" HH:mm:ss");
        Date date = new Date();
        MainActivity.textoLogsTurno.append("Finalizando el turno por una emergencia, con fecha " + dateFormat.format(date) + " y con hora " + hourFormat.format(date));
        crearFicheroLogs();
        MainActivity.isTurnoEmpezado = false;
        Intent intent1 = new Intent();
        intent1.setAction("com.detectorcaidas");
        intent1.putExtra("data","finalizar");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
    }

    private void crearFicheroLogs(){
        String url = "https://servidorhombremuerto.herokuapp.com/addLogTurno/";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,response);
                        if("turnoFalseAdd".equals(response)){
                            Log.d(TAG,"guardando el fichero de logs");
                            try{
                                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
                                String nombremaquinista = sharedPreferences.getString(getString(R.string.shared_nombre_maquinista),getString(R.string.shared_maquinista_por_defecto));
                                String nombreturno = sharedPreferences.getString(getString(R.string.shared_nombre_turno),getString(R.string.shared_maquinista_por_defecto));
                                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                DateFormat hourFormat = new SimpleDateFormat(" HH-mm-ss");
                                Date date = new Date();
                                File fichero = new File(getApplicationContext().getFilesDir(),nombremaquinista+" "+nombreturno+", "+dateFormat.format(date)+" "+hourFormat.format(date)+".txt");
                                BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
                                String[] texto_split = MainActivity.textoLogsTurno.toString().split(";");
                                for (String fila :texto_split) {
                                    writer.write(fila+"\n");
                                }
                                writer.close();
                            }catch (IOException e){
                                Log.e(TAG,e.getMessage());
                            }
                        }
                        MainActivity.textoLogsTurno.delete(0,MainActivity.textoLogsTurno.length());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Fallo al subir el fichero de Logs",Toast.LENGTH_LONG).show();
                Log.d(TAG,"guardando el fichero de logs");
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES), Context.MODE_PRIVATE);
                    String nombremaquinista = sharedPreferences.getString(getString(R.string.shared_nombre_maquinista), getString(R.string.shared_maquinista_por_defecto));
                    String nombreturno = sharedPreferences.getString(getString(R.string.shared_nombre_turno), getString(R.string.shared_maquinista_por_defecto));
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    DateFormat hourFormat = new SimpleDateFormat(" HH-mm-ss");
                    Date date = new Date();
                    File fichero = new File(getApplicationContext().getFilesDir(), nombremaquinista + " " + nombreturno + ", " + dateFormat.format(date) + " " + hourFormat.format(date) + ".txt");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
                    String[] texto_split = MainActivity.textoLogsTurno.toString().split(";");
                    for (String fila : texto_split) {
                        writer.write(fila + "\n");
                    }
                    writer.close();
                }catch (IOException e){
                    Log.e(TAG,e.getMessage());
                }
                MainActivity.textoLogsTurno.delete(0,MainActivity.textoLogsTurno.length());
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String>  params = new HashMap<String, String>();

                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                DateFormat hourFormat = new SimpleDateFormat(" HH-mm-ss");
                Date date = new Date();
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
                String nombremaquinista = sharedPreferences.getString(getString(R.string.shared_nombre_maquinista),getString(R.string.shared_maquinista_por_defecto));
                String nombreturno = sharedPreferences.getString(getString(R.string.shared_nombre_turno),getString(R.string.shared_maquinista_por_defecto));
                params.put("nombreMaquinista",nombremaquinista);
                params.put("nombreTurno",nombreturno);
                params.put("fecha",dateFormat.format(date));
                params.put("hora",hourFormat.format(date));
                params.put("contenido",MainActivity.textoLogsTurno.toString());
                return params;
            }
        };

        queue.add(stringRequest);

    }

    private void hacerAccionesNoMovimiento(){
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat hourFormat = new SimpleDateFormat(" HH:mm:ss");
            Date date = new Date();
            MainActivity.textoLogsTurno.append("Detectada falta de movimiento, con fecha " + dateFormat.format(date) + " y con hora " + hourFormat.format(date) +";");
            Log.d(TAG_MOVIMIENTOS,"Falta de movimineto detectada");
            if(rateHeartNormal()==false){
                Log.d(TAG_MOVIMIENTOS,"Caida detectada con latidos fuera de lo normal");
                MainActivity.textoLogsTurno.append("Detectada falta de movimiento con pulsaciones por fuera de lo normal, con fecha " + dateFormat.format(date) + " y con hora " + hourFormat.format(date)+";");
                MainActivity.caidaBool = true;
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
                            Log.d(TAG, "Activando frenos");
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

                //Broadcast movida
                Intent intentBroadcast = new Intent();
                intentBroadcast.setAction("com.detectorcaidas");
                intentBroadcast.putExtra("data","caida");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);

            }else{
                Log.d(TAG_MOVIMIENTOS,"Se ha detectado una falta de movimiento pero hay un ritmo cardiaco correcto");
                MainActivity.textoLogsTurno.append("Las pulsaciones han sido normales, con fecha " + dateFormat.format(date) + " y con hora " + hourFormat.format(date)+";");
            }
    }





}
