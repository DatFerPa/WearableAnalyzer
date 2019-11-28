package com.detectorcaidas;


import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import com.detectorcaidas.recycle.Contacto;
import com.detectorcaidas.recycle.ContactoAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

public class ListContactsActivity extends WearableActivity {

    private WearableRecyclerView wrView;
    private ContactoAdapter contactoAdapter;
    private List<Contacto> contactos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_contacts);
        contactos = new ArrayList<>();
        generateContactos();
        wrView = findViewById(R.id.recycler_view_contactos);
        wrView.setHasFixedSize(true);
        wrView.setEdgeItemsCenteringEnabled(true);
        wrView.setLayoutManager(new WearableLinearLayoutManager(this));
        contactoAdapter = new ContactoAdapter(contactos);
        wrView.setAdapter(contactoAdapter);



    }

    private void generateContactos(){

        for(int i = 0; i <100; i++){
            contactos.add(new Contacto("nombre", String.valueOf(i)));
        }

    }
}
