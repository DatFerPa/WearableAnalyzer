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
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.detectorcaidas.services.ServiceFallingSensor;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
    private View layoutInicio;
    private View layoutTurno;
    private View layoutLogout;


    private static Intent intentService;



    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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

        pedirPermisos();

        setContentView(R.layout.activity_main);

        top_navigation_drawer = findViewById(R.id.top_navigation_drawer);
        top_navigation_drawer.setAdapter(new NavigationAdapter(this));
        top_navigation_drawer.getController().peekDrawer();
        top_navigation_drawer.addOnItemSelectedListener(this);
        botonInicio = findViewById(R.id.imageButton);
        botonInicio.setColorFilter(Color.GREEN);
        layoutTurno = findViewById(R.id.include_layout_turno);
        layoutInicio = findViewById(R.id.include_layout_inicio);
        layoutLogout = findViewById(R.id.include_layout_logout);
        layoutInicio.setVisibility(View.VISIBLE);
        layoutTurno.setVisibility(View.INVISIBLE);
        layoutLogout.setVisibility(View.INVISIBLE);
        setAmbientEnabled();

    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.detectorcaidas");
        registerReceiver(broadcastReceiver,intentFilter);
        onpause = false;
        Log.d(TAG,"onstart");
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getInt(ServiceFallingSensor.FUERA)==ServiceFallingSensor.ESTAS_FUERA_DE_LA_PRINCIPAL){
            prepareAppInFall();
        }
        if(caidaBool){
            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.fallingicon,null));
        }else{
            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.seat_icon,null));
        }
        super.onStart();
    }

    private void prepareAppInFall(){
        Log.d(TAG,"prepareAppOnFall");
        botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.fallingicon,null));
        stopService(intentService);
        onItemSelected(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        if(caidaBool) {
            Log.d(TAG, "CLick en caida detectada");

            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.seat_icon,null));
            caidaBool = false;
            //esto de momento
            stopService(intentService);
        }else{

            Log.d(TAG, "Click de inicio");

            intentService = new Intent(this, ServiceFallingSensor.class);
            startService(intentService);
        }
    }



    public void clickButtonTurno(View view){
        //movidas para empezar y fnalizar el turno
        Intent intent = new Intent(this,ListTurnoActivity.class);
        startActivity(intent);



    }


    public void clickButtonLogout(View view){
        //borrar shared preferences y devolver a la activity de login
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getString(R.string.shared_nombre_maquinista));
        editor.remove(getString(R.string.shared_nombre_tren));
        editor.remove(getString(R.string.shared_nombre_turno));
        editor.commit();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
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
        Log.d(TAG,"pause");
        onpause = true;
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

}

