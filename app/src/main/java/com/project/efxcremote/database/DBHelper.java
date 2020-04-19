package com.project.efxcremote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "efxc.db";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + DefineTable.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(this.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    /**
     * Añade las columnas o campos de la tabla,
     * @return Un query que para la creación de la tabla
     */
    private String createTable(){
        SQLiteTable tablePresets = new SQLiteTable(DefineTable.TABLE_NAME,DefineTable.ID);
        tablePresets.addColumn(DefineTable.NOMBRE_PRESET,tablePresets.TYPE_TEXT);
        tablePresets.addColumn(DefineTable.PEDAL_ONE,tablePresets.TYPE_TEXT);
        tablePresets.addColumn(DefineTable.PEDAL_TWO,tablePresets.TYPE_TEXT);
        tablePresets.addColumn(DefineTable.PEDAL_THREE,tablePresets.TYPE_TEXT);
        tablePresets.addColumn(DefineTable.PEDAL_FOUR,tablePresets.TYPE_TEXT);
        return tablePresets.getQuery();
    }
}
