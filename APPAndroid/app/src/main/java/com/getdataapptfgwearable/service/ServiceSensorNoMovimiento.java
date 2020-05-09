package com.getdataapptfgwearable.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import com.getdataapptfgwearable.MainActivity;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ServiceSensorNoMovimiento extends Service implements SensorEventListener {

    private static final String TAG = "ServiceSensorNoMovimiento";
    private int contador;
    private double[] gravity = new double[3];
    private float[] linear_acceleration = new float[3];
    List<float[]> lst_linear_acc = new ArrayList<>();
    private SensorManager sensorManager;
    private Sensor accelerometer;

    //Control de CPU
    PowerManager.WakeLock wakeLock;

    Interpreter interpreter;



    public ServiceSensorNoMovimiento() { }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"GetDataAppTFGWereable:WakeLockNoMovimiento");
        wakeLock.acquire();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        contador = 0;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);


        try{
            AssetFileDescriptor fileDescriptor = getApplication().getAssets().openFd("converted_model.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,fileDescriptor.getStartOffset(),fileDescriptor.getDeclaredLength());
            interpreter = new Interpreter(mappedByteBuffer);
        }catch(IOException e){
            Log.e(TAG,e.getMessage());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent sensor) {
        double alpha = 0.8;
        if (contador >= 1000) {
            contador = 0;
            Log.d(TAG,"---------------------------------- SE HAN HECHO 1000 LECTURAS DE NO MOVIMIENTO");
            MainActivity.listaDeListas.add(lst_linear_acc);
            //crearFicheroCaidaNo();

            float[][] salida = new float[1][2];
            float[][] entrada = new float[1000][];

            int cont = 0;
            for(float[] bloque:lst_linear_acc){
                entrada[cont]=bloque;
                cont++;
            }
            interpreter.run(entrada,salida);
            Log.d(TAG,"Salida:  Si movimiento: "+salida[0][0]+" --  no movimiento: "+salida[0][1]);


            lst_linear_acc = new ArrayList<>();
        }
        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensor.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensor.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensor.values[2];
        linear_acceleration[0] = (float) (sensor.values[0] - gravity[0]);
        linear_acceleration[1] = (float) (sensor.values[1] - gravity[1]);
        linear_acceleration[2] = (float) (sensor.values[2] - gravity[2]);
        Log.d(TAG, contador+" - "+"Datos del accelerometro: X: " + linear_acceleration[0] + " - Y: " + linear_acceleration[1] + " - Z: " + linear_acceleration[2]);
        lst_linear_acc.add(linear_acceleration.clone());
        contador++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Finalizando movimiento caida no");
        sensorManager.unregisterListener(this);
        wakeLock.release();
        super.onDestroy();
    }

    private void crearFicheroCaidaNo() {
        Log.d(TAG, String.valueOf(lst_linear_acc.size()));
        Log.d(TAG, getApplicationContext().getFilesDir().getPath());

        Log.d(TAG, "Guardando fichero de No Movimiento");
        try {
            File fichero = new File(getApplicationContext().getFilesDir(), "movimientono" + System.currentTimeMillis() + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
            for (float[] a : lst_linear_acc) {
                writer.write(a[0] + ";" + a[1] + ";" + a[2] + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"algo revento",Toast.LENGTH_LONG).show();
        }
    }
}
