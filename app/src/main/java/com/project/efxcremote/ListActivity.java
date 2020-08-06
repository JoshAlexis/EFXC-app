package com.project.efxcremote;

import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.project.efxcremote.database.DBPresets;
import com.project.efxcremote.database.Preset;

import java.util.ArrayList;
//La clase hereda desde ListActivity ya que se utiliza un ListView como layout principal.
public class ListActivity extends android.app.ListActivity implements View.OnClickListener {

    private Button btnRegresar;
    //Para poder guardar los presets de la base de datos
    private ArrayList<Preset> presets;
    private DBPresets dbPresets;
    private CustomArrayAdapter adapter;
    private final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        iniciarComponentes();
        cargarPresets();
        adapter = new CustomArrayAdapter(context,R.layout.layout_presset,presets);
        setListAdapter(adapter);
    }
    /**
     * Asigna los elementos del layout con su respectivo elemento
     * e inicia los atributos necesarios.
     */
    public void iniciarComponentes(){
        btnRegresar = (Button) findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(this);
        dbPresets = new DBPresets(context);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    //Se consultan todos los registros y se guardan en local
    private void cargarPresets(){
        dbPresets.openDatabase();
        presets = dbPresets.allPresets();
        dbPresets.close();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnRegresar:
                finish();
                break;
        }
    }
    //ArrayAdapte personalizado para mostrar la información
    private class CustomArrayAdapter extends ArrayAdapter<Preset>{
        private Context context;
        private int resourceId;
        private ArrayList<Preset> presets;

        public CustomArrayAdapter(Context context, int resourceId, ArrayList<Preset> presets){
            super(context,resourceId,presets);
            this.context = context;
            this.resourceId = resourceId;
            this.presets = presets;
        }

        @Override
        public View getView(final int position, View converView, ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(this.resourceId, parent, false);
            //Asignando a su respectivo elemento del layout
            TextView lblNombre = (TextView) view.findViewById(R.id.lblTituloPresset);
            Button btnPedal1 = (Button) view.findViewById(R.id.btnPedal1);
            Button btnPedal2 = (Button) view.findViewById(R.id.btnPedal2);
            Button btnPedal3 = (Button) view.findViewById(R.id.btnPedal3);
            Button btnPedal4 = (Button) view.findViewById(R.id.btnPedal4);
            Button btnSelec = (Button) view.findViewById(R.id.btnSeleccionar);
            Button btnEliminar = (Button) view.findViewById(R.id.btnBorrar);
            //Añadiendo la informacion de cada registro a los elementos gráficos
            lblNombre.setText(presets.get(position).getNombre_preset());
            btnPedal1.setBackground(presets.get(position).getPedal_one().equals("1")?
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btnactive,null) :
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btninactive,null));

            btnPedal2.setBackground(presets.get(position).getPedal_two().equals("1")?
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btnactive,null) :
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btninactive,null));

            btnPedal3.setBackground(presets.get(position).getPedal_three().equals("1")?
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btnactive,null) :
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btninactive,null));

            btnPedal4.setBackground(presets.get(position).getPedal_four().equals("1")?
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btnactive,null) :
                    ResourcesCompat.getDrawable(getResources(),R.drawable.btninactive,null));
            //Envia el registro seleccionado al MainActivity
            btnSelec.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("preset",presets.get(position));
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK,intent);
                finish();
            });
            //Elimina un registro y actualiza la vista
            btnEliminar.setOnClickListener(v->{
                dbPresets.openDatabase();
                System.out.println(presets.get(position).getID());
                dbPresets.deletePreset(presets.get(position).getID());
                dbPresets.close();
                presets.remove(position);
                notifyDataSetChanged();
            });
            return view;
        }
    }
}
