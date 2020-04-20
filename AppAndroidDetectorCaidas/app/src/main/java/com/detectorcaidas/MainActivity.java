package com.detectorcaidas;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.detectorcaidas.services.ServiceFallingSensor;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.wear.widget.drawer.WearableNavigationDrawerView.WearableNavigationDrawerAdapter;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

public class MainActivity extends WearableActivity implements WearableNavigationDrawerView.OnItemSelectedListener {

    //notificaciones
    public static final String CANAL_NOTIFICACION_ID = "canal_notificacion_id";
    public static final String NOMBRE_CANAL_NOTIFICACION = "canal de notificaciones";

    private static final String TAG = "MainActivity";
    public static boolean caidaBool;
    public static boolean onpause;
    private WearableNavigationDrawerView top_navigation_drawer;
    private String[] arrayViews = {"Inicio", "Turno","Cerrar sesión"};
    private ImageButton botonInicio;
    private Button botonTurno;
    private View layoutInicio;
    private View layoutTurno;
    private View layoutLogout;


    public static Intent intentService;


    public static boolean isTurnoEmpezado;


//luego borrar
    BroadcastReceiver aa = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,intent.getStringExtra("data"));
            if(intent.getStringExtra("data").equals("caida")) {
                prepareAppInFall();
            }
            if(intent.getStringExtra("data").equals("recuperar")){
                intentService = new Intent(getApplicationContext(), ServiceFallingSensor.class);
                startService(intentService);
            }
        }
    };


    public class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast que funciona¿?");
            if("caida".equals(intent.getStringExtra("data"))) {
                Log.d(TAG, "Broadcast que funciona que te cagas");
            }

        }
    }
    BroadcastReceiver broadcastReceiver = new MyBroadcastReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(TAG,"Creando el canal de las notificaciones");
            NotificationChannel notificationChannel = new NotificationChannel(CANAL_NOTIFICACION_ID,NOMBRE_CANAL_NOTIFICACION, NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.enableLights(true);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setVibrationPattern(new long[] { 500,500,500,500,500 });
            //Sets whether notifications from these Channel should be visible on Lockscreen or not
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
/*
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.detectorcaidas");
        Intent one =registerReceiver(broadcastReceiver,intentFilter);

 */

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.detectorcaidas");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);

        pedirPermisos();

        setContentView(R.layout.activity_main);

        top_navigation_drawer = findViewById(R.id.top_navigation_drawer);
        top_navigation_drawer.setAdapter(new NavigationAdapter(this));
        top_navigation_drawer.getController().peekDrawer();
        top_navigation_drawer.addOnItemSelectedListener(this);
        botonInicio = findViewById(R.id.botonInicial);
        botonInicio.setColorFilter(Color.GREEN);
        botonTurno = findViewById(R.id.buttonTurno);
        layoutTurno = findViewById(R.id.include_layout_turno);
        layoutInicio = findViewById(R.id.include_layout_inicio);
        layoutLogout = findViewById(R.id.include_layout_logout);
        layoutInicio.setVisibility(View.VISIBLE);
        layoutTurno.setVisibility(View.INVISIBLE);
        layoutLogout.setVisibility(View.INVISIBLE);
        setAmbientEnabled();

    }



    private void prepareAppInFall(){
        Log.d(TAG,"prepareAppOnFall");
        botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.fallingicon,null));
        stopService(intentService);
    }

    @Override
    protected void onStart() {
        Log.d(TAG,"on Start");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        onpause = false;
        Bundle extras = getIntent().getExtras();
        if(isTurnoEmpezado){
            botonTurno.setText(R.string.finalizar_turno);
            top_navigation_drawer.setCurrentItem(0,false);
        }
        if(extras != null && extras.getInt(ServiceFallingSensor.FUERA)==ServiceFallingSensor.ESTAS_FUERA_DE_LA_PRINCIPAL){
            prepareAppInFall();
        }
        if(caidaBool){
            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.fallingicon,null));
        }else{
            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.seat_icon,null));
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
    }

    private void pedirPermisos(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED  &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED ) {

            String[] permisos = {Manifest.permission.READ_CONTACTS,Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,Manifest.permission.READ_PHONE_STATE};
            requestPermissions(permisos, PackageManager.PERMISSION_GRANTED);
        }
    }


    @Override
    public void onItemSelected(int pos) {

        Log.d(TAG, "Cambio de pestaña de navigation drawer");
        if (pos == 0) {
            layoutInicio.setVisibility(View.VISIBLE);
            layoutTurno.setVisibility(View.INVISIBLE);
            layoutLogout.setVisibility(View.INVISIBLE);
        } else if(pos == 1) {
            layoutInicio.setVisibility(View.INVISIBLE);
            layoutTurno.setVisibility(View.VISIBLE);
            layoutLogout.setVisibility(View.INVISIBLE);
        }else{
            layoutInicio.setVisibility(View.INVISIBLE);
            layoutTurno.setVisibility(View.INVISIBLE);
            layoutLogout.setVisibility(View.VISIBLE);
        }
    }



    public void clickButtonInicio(View view) {

        if(isTurnoEmpezado && caidaBool){
            /*
                Aqui va a ir la movida de que si hay caida, pase x tiempo tengamos
                que aceptar que estamos bien para para la cuenta a atras y el aviso al
                servidor o lo que se que hagamos.
             */


        }
    }


    public void clickButtonTurno(View view){
        //movidas para empezar y fnalizar el turno
        if(!isTurnoEmpezado) {
            Intent intent = new Intent(this, ListTurnoActivity.class);
            startActivity(intent);
        }else{
            //quitar el service
            stopService(intentService);
            botonTurno.setText(R.string.empezar_turno);

        }

    }


    public void clickButtonLogout(View view){
        //borrar shared preferences y devolver a la activity de login
        Log.d(TAG,"Logout antes de borrar las shared preferences");
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        Log.d(TAG,"Logout despues de borrar las shared preferences");
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }




    private final class NavigationAdapter extends WearableNavigationDrawerAdapter {

        private final Context context;

        NavigationAdapter(Context context) {
            this.context = context;
        }

        @Override
        public CharSequence getItemText(int pos) {
            return arrayViews[pos];
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            //return (pos == 0) ? context.getDrawable(getResources().getIdentifier("walkingicon", "drawable", getPackageName())) : context.getDrawable(getResources().getIdentifier("settings", "drawable", getPackageName()));
            if(pos == 0){
                return context.getDrawable(getResources().getIdentifier("seat_icon", "drawable", getPackageName()));
            }else if(pos == 1){
                return context.getDrawable(getResources().getIdentifier("cartera", "drawable", getPackageName()));
            }else{
                return context.getDrawable(getResources().getIdentifier("exit", "drawable", getPackageName()));
            }
        }

        @Override
        public int getCount() {
            return arrayViews.length;
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        botonInicio.setColorFilter(Color.WHITE);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        botonInicio.setColorFilter(Color.GREEN);
    }

    @Override
    protected void onPause() {
        onpause = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}

