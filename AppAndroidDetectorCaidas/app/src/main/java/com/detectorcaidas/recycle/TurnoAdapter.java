package com.detectorcaidas.recycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.detectorcaidas.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.wear.widget.WearableRecyclerView;

public class TurnoAdapter extends WearableRecyclerView.Adapter<TurnoAdapter.MyViewHolder> {
    private List<Turno> turnos;
    private OnclickListenerTurnos adapterOnClickListenerTurnos;

    public TurnoAdapter(List<Turno> turnos, OnclickListenerTurnos adapterOnClickListenerTurnos){
        this.turnos = turnos;
        this.adapterOnClickListenerTurnos = adapterOnClickListenerTurnos;
    }


    public class MyViewHolder extends WearableRecyclerView.ViewHolder implements View.OnClickListener{

        public TextView textView;
        OnclickListenerTurnos onclickListenerTurnos;

        public MyViewHolder(View itemView, OnclickListenerTurnos onclickListenerTurnos){
            super(itemView);
            this.onclickListenerTurnos = onclickListenerTurnos;
            textView = (TextView) itemView.findViewById(R.id.textNombreTurno);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onclickListenerTurnos.onClickElemento(getAdapterPosition());
        }
    }


    @Override
    public TurnoAdapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_turno_fila,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(vista,adapterOnClickListenerTurnos);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder( MyViewHolder holder, int position) {
        holder.textView.setText(turnos.get(position).getNombre()+ "  --  "+turnos.get(position).getTren());

    }

    @Override
    public int getItemCount() {
        return turnos.size();
    }


    public interface OnclickListenerTurnos{
        void onClickElemento(int position);
    }

}
