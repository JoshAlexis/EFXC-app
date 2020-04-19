package com.project.efxcremote.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBPresets {
    private Context context;
    private DBHelper helper;
    private SQLiteDatabase db;
    private String[] columns = {
            DefineTable.ID,
            DefineTable.NOMBRE_PRESET,
            DefineTable.PEDAL_ONE,
            DefineTable.PEDAL_TWO,
            DefineTable.PEDAL_THREE,
            DefineTable.PEDAL_FOUR
    };

    public DBPresets(Context context){
        this.context = context;
        this.helper = new DBHelper(context);
    }

    public void openDatabase(){
        db = helper.getWritableDatabase();
    }

    /**
     *  Agrega un registro a la base de datos.
     * @param preset - El objeto tipo Preset para guardar los datos.
     * @return long - El id del registro agregado, regresa 0 si no se pudo agregar.
     */
    public long insertPreset(Preset preset){
        ContentValues values = new ContentValues();
        values.put(DefineTable.NOMBRE_PRESET, preset.getNombre_preset());
        values.put(DefineTable.PEDAL_ONE, preset.getPedal_one());
        values.put(DefineTable.PEDAL_TWO, preset.getPedal_two());
        values.put(DefineTable.PEDAL_THREE, preset.getPedal_three());
        values.put(DefineTable.PEDAL_FOUR, preset.getPedal_four());
        return db.insert(DefineTable.TABLE_NAME, null, values);
    }

    /**
     * Actualiza un registro de la base de datos.
     * @param preset - El objeto con la información actualizada.
     * @param id - El id del registro a modificar.
     * @return long - el id del registro actualizado, regresa 0 si no se pudo actualizar.
     */
    public long updatePreset(Preset preset, long id){
        ContentValues values = new ContentValues();
        values.put(DefineTable.NOMBRE_PRESET, preset.getNombre_preset());
        values.put(DefineTable.PEDAL_ONE, preset.getPedal_one());
        values.put(DefineTable.PEDAL_TWO, preset.getPedal_two());
        values.put(DefineTable.PEDAL_THREE, preset.getPedal_three());
        values.put(DefineTable.PEDAL_FOUR, preset.getPedal_four());
        String where = DefineTable.ID + "=" + id;
        return db.update(DefineTable.TABLE_NAME, values, where, null);
    }

    /**
     * Eliminar un registro de la base de datos.
     * @param id - El id del elemento a eliminar.
     * @return long - El id del registro eliminado, regresar 0 si no se pudo eliminar.
     */
    public long deletePreset(long id){
        String where = DefineTable.ID + "=" + id;
        return db.delete(DefineTable.TABLE_NAME,where,null);
    }

    /**
     * <p>Obtiene un registro de la base de datos. Es privado ya que es usado
     * por otros métodos dentro de la clase.
     * @param cursor - Objeto de tipo <code>Cursor</code> para poder obtener los datos
     *               de los registro de la base de datos.
     * @return Preset - Un objeto con la información correspondiente a un registro de la base de datos.
     */
    private Preset readPreset(Cursor cursor){
        Preset preset = new Preset();
        preset.setID(cursor.getInt(0));
        preset.setNombre_preset(cursor.getString(1));
        preset.setPedal_one(cursor.getString(2));
        preset.setPedal_two(cursor.getString(3));
        preset.setPedal_three(cursor.getString(4));
        preset.setPedal_four(cursor.getString(5));
        return preset;
    }

    /**
     * Consulta un registro específico.
     * @param id - El id de elemento específico a buscar.
     * @return Un objeto tipo Preset con la información del registro específico.
     */
    public Preset getPreset(long id){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                DefineTable.TABLE_NAME,
                columns,
                DefineTable.ID + "= ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null);
        cursor.moveToFirst();
        Preset preset = this.readPreset(cursor);
        cursor.close();
        return preset;
    }

    /**
     * Consulta todos los registros de la base de datos.
     * @return Un arrayList de tipo Preset con todos los registros de la base
     *         de datos.
     */
    public ArrayList<Preset> allPresets(){
        ArrayList<Preset> presets = new ArrayList<>();
        Cursor cursor = db.query(
                DefineTable.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );
        if(cursor != null){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                presets.add(readPreset(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return presets;
    }
    /*
     * Cierra la "conexión" con la base de datos (SQLite es de tipo
     * transaccional).
     * */
    public void close(){
        helper.close();
    }
}
