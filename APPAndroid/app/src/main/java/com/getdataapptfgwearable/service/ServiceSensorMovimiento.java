/**
 * @author Fernando Palazuelo Ginzo - UO244588
 */
package com.getdataapptfgwearable.service;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de leer el acelerómetro del dispositvo e ir guardando bloques de datos de 1000 en 1000, para luego guardarlos dentro del sistema
 */
public class ServiceSensorMovimiento extends Service implements SensorEventListener {
    private static final String TAG = "ServiceSensorMovimiento";

    private double[] gravity = new double[3];
    private float[] linear_acceleration = new float[3];
    private int contador;
    List<float[]> lst_linear_acc = new ArrayList<>();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean caida;

    //Control de CPU
    PowerManager.WakeLock wakeLock;

    public ServiceSensorMovimiento() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        caida = intent.getBooleanExtra("movimiento",false);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"GetDataAppTFGWereable:WakeLockNoMovimiento");
        wakeLock.acquire();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        contador = 0;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Método que se encarga de realizar cada una de las lecturas de los sensores
     * @param sensor
     */
    @Override
    public void onSensorChanged(SensorEvent sensor) {
        double alpha = 0.8;
        if(contador>=1000){
            contador = 0;
            Log.d(TAG,"---------------------------------- SE HAN HECHO 1000 LECTURAS DE SI MOVIMIENTO");
            crearFichero();
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
        Toast.makeText(getApplicationContext(),"onDestroy service",Toast.LENGTH_LONG).show();
        Log.d(TAG,"Finalizando servicio movimiento si");
        sensorManager.unregisterListener(this);
        wakeLock.release();
        super.onDestroy();
    }

    /**
     * Método encargado de crear un fichero en el sistema por cada mil lecturas.
     * Este método creará un fichero caidasi o caidano, dependiendo del extra que coloquemos
     * en el Intent al iniciar el Servicio
     */
    private void crearFichero() {
        Log.d(TAG,String.valueOf(lst_linear_acc.size()));
        Log.d(TAG,getApplicationContext().getFilesDir().getPath());
        File fichero;
        if(caida){
            fichero = new File(getApplicationContext().getFilesDir(),"movimientosi"+System.currentTimeMillis()+".txt");
        }
        else{
            fichero = new File(getApplicationContext().getFilesDir(),"movimientono"+System.currentTimeMillis()+".txt");
        }
        Log.d(TAG,"Guardando fichero de Si Movimiento");
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
            for (float[] a : lst_linear_acc) {
                writer.write(a[0] + ";" + a[1] + ";" + a[2] + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
