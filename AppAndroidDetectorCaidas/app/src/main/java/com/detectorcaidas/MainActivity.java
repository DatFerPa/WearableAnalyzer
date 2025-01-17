/**
 * @author Fernando Palazuelo Ginzo - UO244588
 */


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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.detectorcaidas.services.ServiceFallingSensor;
import com.detectorcaidas.services.ServiceRegistroGenerator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.wear.widget.drawer.WearableNavigationDrawerView.WearableNavigationDrawerAdapter;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

/**
 * Activity que se encarga de ofrecer las funciones para el menú principal
 */
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

    public static StringBuffer textoLogsTurno;

    public static Intent intentService;

    public static boolean isTurnoEmpezado;

    /**
     * Broadcast Receiver que permite comunicar los servicios con la MainActivity
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";

        /**
         *  Función que se encarga de gestionar las llamadas a los broadcast receivers
         * @param context Contexto de la aplicación
         * @param intent Intent que crear la comunicación
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if("caida".equals(intent.getStringExtra("data"))) {
                prepareAppInFall();
            }
            if("finalizar".equals(intent.getStringExtra("data"))){
                botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.seat_icon,null));
                botonTurno.setText(R.string.empezar_turno);
            }
        }
    }
    BroadcastReceiver broadcastReceiver = new MyBroadcastReceiver();

    /**
     * Función que se llama para crear la app
     * @param savedInstanceState
     */
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
        textoLogsTurno = new StringBuffer();
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


    /**
     * Función que prepara el menú cuando se ha detectado una caida
     */
    private void prepareAppInFall(){
        Log.d(TAG,"prepareAppOnFall");
        botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.fallingicon,null));
        stopService(intentService);
        onItemSelected(0);
    }


    @Override
    protected void onStart() {
        Log.d(TAG,"on Start");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"on Resume");
        onpause = false;
        ActivityWaitForHeart.isWaitingForHeart = false;
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

    }

    /**
     *  Función que se encarga de pedir los permisos al usuario
     */
    private void pedirPermisos(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED ) {
            String[] permisos = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.BODY_SENSORS};
            requestPermissions(permisos, PackageManager.PERMISSION_GRANTED);
        }
    }


    /**
     * Función que nos permite cambiar entre elementos cuando seleccionamos uno en el menú de selección
     * @param pos Posición del elemento seleccionado en el menú para cambiar entre ventanas
     */
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

    /**
     * Función que nos permite parar la alarma por falta de movimiento
     * @param view
     */
    public void clickButtonInicio(View view) {
        if(isTurnoEmpezado && caidaBool){
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat hourFormat = new SimpleDateFormat(" HH:mm:ss");
            Date date = new Date();
            textoLogsTurno.append( "Se evitado el frenado de emergencia, con fecha " + dateFormat.format(date) + " y con hora " + hourFormat.format(date)+";");
            intentService = new Intent(this, ServiceFallingSensor.class);
            startService(MainActivity.intentService);
            botonInicio.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.seat_icon,null));
            caidaBool = false;
            Toast.makeText(getApplicationContext(),"Cancelando frenos de emergencia",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Función que nos permite empezar y finalizar turnos
     * @param view
     */
    public void clickButtonTurno(View view){


        if(!isTurnoEmpezado ) {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,Manifest.permission.BODY_SENSORS)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, ListTurnoActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(),"Acepte los permisos para empezar un turno", Toast.LENGTH_LONG).show();
            }
        }else{
            stopService(intentService);
            isTurnoEmpezado = false;
            botonTurno.setText(R.string.empezar_turno);
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat hourFormat = new SimpleDateFormat(" HH:mm:ss");
            Date date = new Date();
            textoLogsTurno.append( "Turno finalizado con fecha " + dateFormat.format(date) + " y con hora " + hourFormat.format(date));
            Intent intent = new Intent(getApplicationContext(), ServiceRegistroGenerator.class);
            intent.putExtra("emergencia",false);
            startService(intent);
        }


    }

    /**
     * Función que nos permite cerrar la sesión como maquinista
     * @param view
     */
    public void clickButtonLogout(View view){
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

    /**
     * Clase que genera el menú para cambiar entre ventanas
     */
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
        Log.d(TAG,"on pause");
        onpause = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}

