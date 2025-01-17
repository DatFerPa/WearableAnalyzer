/**
 * @author Fernando Palazuelo Ginzo - UO244588
 */

package com.detectorcaidas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Actividad que se encarga de gestionar los incios de sesión en la aplicación
 */
public class LoginActivity extends WearableActivity {
    private static final String TAG = "LoginActivity";

    private TextView textoMaquinista;
    private ImageButton botonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
        if(sharedPreferences.contains(getString(R.string.shared_nombre_maquinista))){
            Log.d(TAG,"Login con shared preferences");
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else {
            Log.d(TAG,"Login sin shared preferences");
            setContentView(R.layout.activity_login);
            textoMaquinista = findViewById(R.id.textMaquinista);
            botonLogin = findViewById(R.id.buttonLogin);

            // Enables Always-on
            setAmbientEnabled();
        }
    }

    /**
     *  Función que se encarga de identificar un maquinista
     * @param view
     */
    public void onClickLogin(View view) {

        botonLogin.setClickable(false);
        textoMaquinista.setClickable(false);

        String url = "https://servidorhombremuerto.herokuapp.com/login/";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,response);
                        botonLogin.setClickable(true);
                        textoMaquinista.setClickable(true);
                        if(response.equals("si")){
                            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.shared_nombre_maquinista),textoMaquinista.getText().toString());
                            editor.commit();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(),"El maquinista no se encuentra en el sistema",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Fallo en el servidor. Intentelo más tarde",Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String>  params = new HashMap<String, String>();
                String nombre = textoMaquinista.getText().toString();
                params.put("nombre",nombre);
                return params;
            }
        };

        queue.add(stringRequest);




    }
}
