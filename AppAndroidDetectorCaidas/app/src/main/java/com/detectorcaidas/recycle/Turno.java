package com.detectorcaidas.recycle;

import androidx.annotation.NonNull;

public class Turno {
    private String nombre;
    private String tren;

    public Turno(String nombre, String tren) {
        this.nombre = nombre;
        this.tren = tren;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTren() {
        return tren;
    }

    @Override
    public String toString() {
        return "Nombre turnos: "+this.nombre+" -- Tren del turno: "+this.tren;
    }
}