package com.detectorcaidas.recycle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.detectorcaidas.R;
import java.util.List;
import androidx.wear.widget.WearableRecyclerView;

public class ContactoAdapter extends WearableRecyclerView.Adapter<ContactoAdapter.MyViewHolder> {

    private List<Contacto> contactos;
    private OnClickListenerContactos adapteronClickListenerContactos;
    public ContactoAdapter(List<Contacto> contactos , OnClickListenerContactos adapteronClickListenerContactos) {
        this.contactos = contactos;
        this.adapteronClickListenerContactos = adapteronClickListenerContactos;
    }


    public class MyViewHolder extends WearableRecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        OnClickListenerContactos onClickListenerContactos;
        public MyViewHolder( View itemView, OnClickListenerContactos onClickListenerContactos) {
            super(itemView);
            this.onClickListenerContactos = onClickListenerContactos;
            textView = (TextView) itemView.findViewById(R.id.textNombreContacto);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListenerContactos.onClickElemento(getAdapterPosition());
        }
    }


    @Override
    public ContactoAdapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_contact_fila,parent, false);
        MyViewHolder viewHolder = new MyViewHolder(vista,adapteronClickListenerContactos);
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


    public interface OnClickListenerContactos{
        void onClickElemento(int position);
    }
}
