/**
 * @author Fernando Palazuelo Ginzo - UO244588
 */

package com.detectorcaidas.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que se encarga de crear los registros de los turnos
 */
public class ServiceRegistroGenerator extends Service {
    private static final String TAG = "ServiceRegistroGenerator";
    public static boolean emergencia;
    public ServiceRegistroGenerator() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        emergencia = intent.getBooleanExtra("emergencia",false);
        crearFicheroLogs();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     *  Función que crea el fichero de registro de un turno y lo envia al servidor
     */
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
                                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES), Context.MODE_PRIVATE);
                                String nombremaquinista = sharedPreferences.getString(getString(R.string.shared_nombre_maquinista),getString(R.string.shared_maquinista_por_defecto));
                                String nombreturno = sharedPreferences.getString(getString(R.string.shared_nombre_turno),getString(R.string.shared_maquinista_por_defecto));
                                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                DateFormat hourFormat = new SimpleDateFormat(" HH-mm-ss");
                                Date date = new Date();
                                File fichero;
                                if(emergencia) {
                                    fichero  = new File(getApplicationContext().getFilesDir(), nombremaquinista + "," + nombreturno + "," + dateFormat.format(date) + " " + hourFormat.format(date) + ",emergencia.txt");
                                }else{
                                    fichero = new File(getApplicationContext().getFilesDir(), nombremaquinista + "," + nombreturno + "," + dateFormat.format(date) + " " + hourFormat.format(date) + ".txt");
                                }
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
                        stopSelf();
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
                    File fichero = new File(getApplicationContext().getFilesDir(), nombremaquinista + "," + nombreturno + "," + dateFormat.format(date) + " " + hourFormat.format(date) + ",emergencia.txt");
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
                stopSelf();
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
                params.put("emergencia","emergencia");
                return params;
            }
        };

        queue.add(stringRequest);

    }

}
