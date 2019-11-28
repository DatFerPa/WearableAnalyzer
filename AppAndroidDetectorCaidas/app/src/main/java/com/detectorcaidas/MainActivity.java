package com.detectorcaidas;

import android.Manifest;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.provider.ContactsContract;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
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

    public void onCLickLayoutInicio(View view) {

        getContactList();

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






    private void getContactList() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permisos = {Manifest.permission.READ_CONTACTS};
            requestPermissions(permisos, PackageManager.PERMISSION_GRANTED);

        } else {

            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Log.i(TAG, "Name: " + name);
                            Log.i(TAG, "Phone Number: " + phoneNo);
                        }
                        pCur.close();
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
    }
}

