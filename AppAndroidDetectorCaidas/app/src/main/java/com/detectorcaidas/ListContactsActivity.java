package com.detectorcaidas;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

import com.detectorcaidas.recycle.Contacto;
import com.detectorcaidas.recycle.ContactoAdapter;
import com.detectorcaidas.utils.ContactLister;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

public class ListContactsActivity extends WearableActivity {

    private static final String TAG = "ListContactActivity";

    private WearableRecyclerView wrView;
    private ContactoAdapter contactoAdapter;
    private List<Contacto> contactos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_contacts);
        contactos = new ArrayList<>();
        getContactList();
        /*
        wrView = findViewById(R.id.recycler_view_contactos);
        wrView.setHasFixedSize(true);
        wrView.setEdgeItemsCenteringEnabled(true);
        CustomScrollingLayoutCallback customScrollingLayoutCallback = new CustomScrollingLayoutCallback();
        wrView.setLayoutManager(new WearableLinearLayoutManager(this,customScrollingLayoutCallback));
        // wrView.setLayoutManager(new WearableLinearLayoutManager(this));
        contactoAdapter = new ContactoAdapter(contactos);
        wrView.setAdapter(contactoAdapter);
        */

    }

    public class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
        /** How much should we scale the icon at most. */
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


    public  void getContactList() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permisos = {Manifest.permission.READ_CONTACTS};
            requestPermissions(permisos, PackageManager.PERMISSION_GRANTED);

        } else {

            ContentResolver cr = getContentResolver();
            ContactLister contactLister = new ContactLister();
            contactLister.getListaDeContactos(cr);
            contactLister.getContactos();
        }
    }
}
