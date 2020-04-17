package com.detectorcaidas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.detectorcaidas.recycle.Turno;
import com.detectorcaidas.recycle.TurnoAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

public class ListTurnoActivity extends WearableActivity implements TurnoAdapter.OnclickListenerTurnos{

    private static final String TAG = "ListTurnoActivity";
    private WearableRecyclerView wrView;
    private TurnoAdapter turnoAdapter;
    private List<Turno> turnos;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,intent.getStringExtra("data"));
            if(intent.getStringExtra("data").equals("finalizar")) {
                unregisterReceiver(this);
                finish();
                Intent intent1 = new Intent();
                intent1.setAction("com.detectorcaidas");
                intent1.putExtra("data", "caida");
                sendBroadcast(intent1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_turnos);
        turnos = getTurnos();
        wrView = findViewById(R.id.recycler_view_turnos);
        wrView.setHasFixedSize(true);
        wrView.setEdgeItemsCenteringEnabled(true);
        CustomScrollingLayoutCallbacks customScrollingLayoutCallbacks = new CustomScrollingLayoutCallbacks();
        wrView.setLayoutManager(new WearableLinearLayoutManager(this,customScrollingLayoutCallbacks));
        turnoAdapter = new TurnoAdapter(turnos,this);
        wrView.setAdapter(turnoAdapter);
        // Enables Always-on
        setAmbientEnabled();
    }



    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        finish();
    }

    @Override
    public void onClickElemento(int position){
        /*
            hacer cosa con los shared preferencesy guardar el turnos actual
         */
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.shared_nombre_turno),turnos.get(position).getNombre());
        editor.putString(getString(R.string.shared_nombre_tren),turnos.get(position).getTren());
        editor.commit();
        finish();
    }

    private List<Turno> getTurnos() {
        final List<Turno> turnos = new ArrayList<>();

        String url = "https://servidorhombremuerto.herokuapp.com/turnos/";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] turnosString = response.split(":");
                        for(String turnoString : turnosString){
                            String[] turnoStringCorte = turnoString.split(";");
                            Turno turn = new Turno(turnoStringCorte[0],turnoStringCorte[1]);
                            turnos.add(turn);
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

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
                String nombre = sharedPreferences.getString(getString(R.string.shared_nombre_maquinista),getString(R.string.shared_maquinista_por_defecto));
                params.put("nombre",nombre);
                return params;
            }
        };

        queue.add(stringRequest);

        return turnos;
    }


    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        finish();
    }




    /*
        Deformación circular de la lista
     */
    public class CustomScrollingLayoutCallbacks extends WearableLinearLayoutManager.LayoutCallback{
        private static final float MAX_ICON_PROGRESS = 0.65f;
        private float progressToCenter;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            // Figure out % progress from top to bottom
            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
            // Adjust to the maximum scale
            progressToCenter = Math.min(progressToCenter, MAX_ICON_PROGRESS);

            child.setScaleX(1 - progressToCenter);
            child.setScaleY(1 - progressToCenter);
        }
    }

}
