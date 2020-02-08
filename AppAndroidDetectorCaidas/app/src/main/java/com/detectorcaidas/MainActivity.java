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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
    private WearableNavigationDrawerView top_navigation_drawer;
    private String[] arrayViews = {"Inicio", "Ajustes"};
    private ImageButton botonInicio;
    private View layoutInicio;
    private View layoutAjustes;
    private TextView telefonoTextView;
    private TextView contactoTextView;


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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.detectorcaidas");
        registerReceiver(broadcastReceiver,intentFilter);
        setContentView(R.layout.activity_main);

        top_navigation_drawer = findViewById(R.id.top_navigation_drawer);
        top_navigation_drawer.setAdapter(new NavigationAdapter(this));
        top_navigation_drawer.getController().peekDrawer();
        top_navigation_drawer.addOnItemSelectedListener(this);
        botonInicio = findViewById(R.id.imageButton);
        botonInicio.setColorFilter(Color.GREEN);
        layoutAjustes = findViewById(R.id.layout_base_ajustes);
        layoutInicio = findViewById(R.id.layout_base_incio);
        layoutInicio.setVisibility(View.VISIBLE);
        layoutAjustes.setVisibility(View.INVISIBLE);
        telefonoTextView = findViewById(R.id.numeroTelefContacto);
        contactoTextView = findViewById(R.id.nombreContacto);
        getInfoContact();
        setAmbientEnabled();

    }

    @Override
    protected void onStart() {
        Log.d(TAG,"onstart");
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getInt(ServiceFallingSensor.FUERA)==ServiceFallingSensor.ESTAS_FUERA_DE_LA_PRINCIPAL){
            prepareAppInFall();
        }
        if(caidaBool){
            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.fallingicon,null));
        }else{
            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.walkingicon,null));
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
        telefonoTextView.setText(sharedPreferences.getString(getString(R.string.shared_telefono_contacto),getString(R.string.contactoDefecto)));
        contactoTextView.setText(sharedPreferences.getString(getString(R.string.shared_nombre_contacto),getString(R.string.telefonoDefecto)));
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

    private  void getInfoContact(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.ID_SHARED_PREFERENCES),Context.MODE_PRIVATE);

        if(sharedPreferences.contains(getString(R.string.shared_nombre_contacto))
            && sharedPreferences.contains(getString(R.string.shared_telefono_contacto))){

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.shared_nombre_contacto),getString(R.string.contactoDefecto));
            editor.putString(getString(R.string.shared_telefono_contacto),getString(R.string.telefonoDefecto));
            editor.commit();
        }

        telefonoTextView.setText(sharedPreferences.getString(getString(R.string.shared_telefono_contacto),getString(R.string.contactoDefecto)));
        contactoTextView.setText(sharedPreferences.getString(getString(R.string.shared_nombre_contacto),getString(R.string.telefonoDefecto)));
    }



    @Override
    public void onItemSelected(int pos) {

        Log.d(TAG, "Cambio de pestaña de navigation drawer");
        if (pos == 0) {
            layoutInicio.setVisibility(View.VISIBLE);
            layoutAjustes.setVisibility(View.INVISIBLE);
        } else {
            layoutInicio.setVisibility(View.INVISIBLE);
            layoutAjustes.setVisibility(View.VISIBLE);
        }
    }

    public void clickButtonInicio(View view) {

        if(caidaBool) {
            Log.d(TAG, "CLick en caida detectada");

            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.walkingicon,null));
            caidaBool = false;
        }else{

            Log.d(TAG, "Click de inicio");

            intentService = new Intent(this, ServiceFallingSensor.class);
            startService(intentService);
        }
    }


    public void clickModificarContacto(View view) {
        Log.d(TAG, "Click modificar contacto");
        Intent intent = new Intent(this, ListContactsActivity.class);
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
            return (pos == 0) ? context.getDrawable(getResources().getIdentifier("walkingicon", "drawable", getPackageName())) : context.getDrawable(getResources().getIdentifier("settings", "drawable", getPackageName()));
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

}

