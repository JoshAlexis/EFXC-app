package com.project.efxcremote;

import java.io.Serializable;

public class Presset implements Serializable {
    private String nombre, descripcion;
    private int pedal1, pedal2, pedal3, pedal4;


    public Presset() {
    }

    public Presset(String nombre, String descripcion, int pedal1, int pedal2, int pedal3, int pedal4) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.pedal1 = pedal1;
        this.pedal2 = pedal2;
        this.pedal3 = pedal3;
        this.pedal4 = pedal4;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPedal1() {
        return pedal1;
    }

    public void setPedal1(int pedal1) {
        this.pedal1 = pedal1;
    }

    public int getPedal2() {
        return pedal2;
    }

    public void setPedal2(int pedal2) {
        this.pedal2 = pedal2;
    }

    public int getPedal3() {
        return pedal3;
    }

    public void setPedal3(int pedal3) {
        this.pedal3 = pedal3;
    }

    public int getPedal4() {
        return pedal4;
    }

    public void setPedal4(int pedal4) {
        this.pedal4 = pedal4;
    }
}
