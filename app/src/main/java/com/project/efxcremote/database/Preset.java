package com.project.efxcremote.database;

import java.io.Serializable;

public class Preset implements Serializable {
    private long ID;
    private String nombre_preset;
    private String pedal_one;
    private String pedal_two;
    private String pedal_three;
    private String pedal_four;

    public Preset() {
    }

    public Preset(long ID, String nombre_preset, String pedal_one, String pedal_two, String pedal_three, String pedal_four) {
        this.ID = ID;
        this.nombre_preset = nombre_preset;
        this.pedal_one = pedal_one;
        this.pedal_two = pedal_two;
        this.pedal_three = pedal_three;
        this.pedal_four = pedal_four;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getNombre_preset() {
        return nombre_preset;
    }

    public void setNombre_preset(String nombre_preset) {
        this.nombre_preset = nombre_preset;
    }

    public String getPedal_one() {
        return pedal_one;
    }

    public void setPedal_one(String pedal_one) {
        this.pedal_one = pedal_one;
    }

    public String getPedal_two() {
        return pedal_two;
    }

    public void setPedal_two(String pedal_two) {
        this.pedal_two = pedal_two;
    }

    public String getPedal_three() {
        return pedal_three;
    }

    public void setPedal_three(String pedal_three) {
        this.pedal_three = pedal_three;
    }

    public String getPedal_four() {
        return pedal_four;
    }

    public void setPedal_four(String pedal_four) {
        this.pedal_four = pedal_four;
    }
}
