package com.detectorcaidas;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.wear.widget.drawer.WearableNavigationDrawerView.WearableNavigationDrawerAdapter;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

public class MainActivity extends WearableActivity implements WearableNavigationDrawerView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private WearableNavigationDrawerView top_navigation_drawer;
    private String[] arrayViews = {"Inicio", "Ajustes"};

    private View layoutInicio;
    private View layoutAjustes;

    private TextView telefonoTextView;
    private TextView contactoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        top_navigation_drawer = findViewById(R.id.top_navigation_drawer);
        top_navigation_drawer.setAdapter(new NavigationAdapter(this));
        top_navigation_drawer.getController().peekDrawer();
        top_navigation_drawer.addOnItemSelectedListener(this);
        // Enables Always-on

        layoutAjustes = findViewById(R.id.layout_base_ajustes);
        layoutInicio = findViewById(R.id.layout_base_incio);
        layoutInicio.setVisibility(View.VISIBLE);
        layoutAjustes.setVisibility(View.INVISIBLE);
        telefonoTextView = findViewById(R.id.numeroTelefContacto);
        contactoTextView = findViewById(R.id.nombreContacto);
        getInfoContact();

        setAmbientEnabled();
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
        Log.d(TAG, "Click de inicio");
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

}

