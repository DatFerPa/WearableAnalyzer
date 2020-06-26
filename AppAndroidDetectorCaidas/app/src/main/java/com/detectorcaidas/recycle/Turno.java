/**
 * @author Fernando Palazuelo Ginzo - UO244588
 */

package com.detectorcaidas.recycle;

/**
 * Clase de guarda la información correspondiente a un turno
 */
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
