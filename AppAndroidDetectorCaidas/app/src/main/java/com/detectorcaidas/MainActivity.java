package com.detectorcaidas;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import androidx.wear.widget.drawer.WearableNavigationDrawerView.WearableNavigationDrawerAdapter;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

public class MainActivity extends WearableActivity implements WearableNavigationDrawerView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private WearableNavigationDrawerView top_navigation_drawer;
    private String[] arrayViews = {"Inicio","Ajustes"} ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        top_navigation_drawer = findViewById(R.id.top_navigation_drawer);
        top_navigation_drawer.setAdapter(new NavigationAdapter(this));


        top_navigation_drawer.getController().peekDrawer();
        top_navigation_drawer.addOnItemSelectedListener(this);
        // Enables Always-on

        setAmbientEnabled();
    }


    @Override
    public void onItemSelected(int pos) {
        Log.d(TAG,"Cambio de pestaña de navigation drawer");
        //aqui vamos a poner lo que tiene que cambiar en el linear layout

    }



    private final class NavigationAdapter extends WearableNavigationDrawerAdapter {

        private final Context context;

        NavigationAdapter(Context context){
            this.context = context;
        }

        @Override
        public CharSequence getItemText(int pos) {
            return arrayViews[pos];
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            return context.getDrawable(getResources().getIdentifier("settings", "drawable", getPackageName()));
        }

        @Override
        public int getCount() {
            return arrayViews.length;
        }
    }
/*
<LinearLayout
                android:id="@+id/linear_layout"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical" />
*/
}

