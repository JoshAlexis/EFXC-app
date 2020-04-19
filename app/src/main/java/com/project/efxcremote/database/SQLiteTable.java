package com.project.efxcremote.database;

import java.util.ArrayList;

public class SQLiteTable {
    public final int TYPE_INTERGER = 1;
    public final int TYPE_TEXT = 2;
    public final int TYPE_REAL = 3;
    private String tableName;
    //Aqui se van guardando los campos ya definidos.
    private ArrayList<Column> columns;
    private String namePrimaryKey;

    public SQLiteTable(String tableName, String namePrimaryKey) {
        this.tableName = tableName;
        this.namePrimaryKey = namePrimaryKey;
        this.columns = new ArrayList<>();
    }

    public void addColumn(String name, int typeData, boolean isNotNull, boolean isUnique){
        Column column = new Column(name,typeData,isNotNull,isUnique);
        if(addColumn(column)){
            System.out.println("Se ha agregado la columna");
        }
    }

    /**
     * Agrega un columna definiendo un nombre y un tipo de dato.
     * @param name - El nombre del campo
     * @param typeData - El tipo de dato del campo.
     */
    public void addColumn(String name, int typeData){
        Column column = new Column(name,typeData);
        if(addColumn(column)){
            System.out.println("Se ha agregado la columna");
        }
    }

    /**
     * Agregar las columnas que se vayan a crear para la
     * base de datos en un ArrayList.
     * @param column - La columna a crear
     * @return
     */
    private boolean addColumn(Column column){
        boolean exito = true;
        if(columns.size()>0){
            for(Column item : columns){
                if(item.getNameColumn().matches(column.getNameColumn())){
                    exito = false;
                    System.out.println("Error - Ha agredado campo con el mismo nombre");
                    break;
                }
            }
        }
        if(!exito)
            return false;
        else{
            columns.add(column);
            return true;
        }
    }

    /**
     * Crea el query que será usado para crear la tabla.
     * @return El query que crea la tabla en la base de datos.
     */
    public String getQuery(){
        String query = "";
        query = String.format("CREATE TABLE %s(%s)",this.tableName,this.generateColumns());
        return query;
    }

    /**
     * Genera el query que define la columnas o campos de la tabla.
     * Se usa dentro de <code>getQuery</code>.
     * @return El query que define las columnas o campos.
     */
    private String generateColumns(){
        String columns = "";
        String format = "%s %s %s %s,";
        columns += String.format("%s INTEGER PRIMARY KEY, ",this.namePrimaryKey);
        for(int x=0;x<this.columns.size();x++){
            Column index = this.columns.get(x);
            if(x == this.columns.size() -1)
                format = "%s %s %s %s";
            columns += String.format(format,index.getNameColumn(),index.getTypeData(),index.isNotNull(),index.isUnique());
        }
        return columns;
    }

    /**
     * Representa cada campo de la tabla.
     */
    private class Column{
        private String nameColumn;
        private int typeData;
        private boolean notNull;
        private boolean unique;

        private Column(String nameColumn, int typeData){
            this.nameColumn = nameColumn;
            this.typeData = typeData;
            notNull = false;
            unique = false;
        }

        private Column(String nameColumn, int typeData, boolean notNull, boolean unique) {
            this.nameColumn = nameColumn;
            this.typeData = typeData;
            this.notNull = notNull;
            this.unique = unique;
        }

        public Column() {
        }

        public String isNotNull(){
            if(this.notNull)
                return "NOT NULL";
            else
                return "";
        }

        public String isUnique(){
            if(this.unique)
                return "UNIQUE";
            else
                return "";
        }

        public String getNameColumn() {
            return nameColumn;
        }

        public void setNameColumn(String nameColumn) {
            this.nameColumn = nameColumn;
        }

        public String getTypeData(){
            String type = "";
            switch (this.typeData){
                case TYPE_TEXT:
                    type = "TEXT";
                    break;
                case TYPE_INTERGER:
                    type = "INTEGER";
                    break;
                case TYPE_REAL:
                    type = "REAL";
                    break;
            }
            return type;
        }

        public void setTypeData(int typeData){
            this.typeData = typeData;
        }
    }
}
