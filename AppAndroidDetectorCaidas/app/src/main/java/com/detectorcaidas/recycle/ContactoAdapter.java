package com.detectorcaidas.recycle;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.detectorcaidas.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableRecyclerView;

public class ContactoAdapter extends WearableRecyclerView.Adapter<ContactoAdapter.MyViewHolder> {

    private List<Contacto> contactos;

    public ContactoAdapter(List<Contacto> contactos) {
        this.contactos = contactos;
    }


    public static class MyViewHolder extends WearableRecyclerView.ViewHolder{
        public TextView textView;
        public MyViewHolder( View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textNombreContacto);
        }
    }


    @Override
    public ContactoAdapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_contact_fila,parent, false);
        MyViewHolder viewHolder = new MyViewHolder(vista);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder( MyViewHolder holder, int position) {
        holder.textView.setText(contactos.get(position).getNombre());
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }
}
