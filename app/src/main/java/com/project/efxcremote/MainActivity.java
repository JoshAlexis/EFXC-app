package com.project.efxcremote;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.project.efxcremote.database.DBPresets;
import com.project.efxcremote.database.Preset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    final static String MAC_ADDRESS = "00:18:E4:40:00:06";
    final static int HANDLE_STATE = 0;
    private final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter bAdapter;
    private BluetoothSocket bSocket;
    private BluetoothDevice bDevice;
    private ConnectionThread bluetooth;
    private Handler handler;
    private CheckBox checkPedal1, checkPedal2, checkPedal3, checkPedal4;
    private Button btnOnlinePressets, btnSalir, btnGuardar;
    private EditText txtNombrePresset;
    private DBPresets db;
    private Preset savedPreset;
    private long id;

    ArrayList<String> input = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarComponentes();
        iniciarEventos();

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == HANDLE_STATE){
                    String inputText = (String)msg.obj;
                    if(inputText.equals("1") || inputText.equals("0"))
                        input.add(inputText);
                }
            }
        };

        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bAdapter.isEnabled()){
            startBluetooth();
        }else{
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
        }
    }



    // Métodos
    public void iniciarComponentes(){
        checkPedal1 = (CheckBox) findViewById(R.id.checkPedal1);
        checkPedal2 = (CheckBox) findViewById(R.id.checkPedal2);
        checkPedal3 = (CheckBox) findViewById(R.id.checkPedal3);
        checkPedal4 = (CheckBox) findViewById(R.id.checkPedal4);
        txtNombrePresset = (EditText) findViewById(R.id.txtPressetName);
        btnOnlinePressets = (Button) findViewById(R.id.btnOnlinePressets);
        btnSalir = (Button) findViewById(R.id.btnSalir);
        btnGuardar = (Button) findViewById(R.id.btnEnviar);
        db = new DBPresets(MainActivity.this);
        savedPreset = null;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void iniciarEventos(){
        this.btnSalir.setOnClickListener(this);
        this.btnOnlinePressets.setOnClickListener(this);
        this.btnGuardar.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);
        if(resultCode == RESULT_OK && requestCode == 1){
            startBluetooth();
        }else if(resultCode == RESULT_OK && requestCode == 2){
            if(intent != null){
                Bundle bundle = intent.getExtras();
                Preset preset = (Preset) bundle.getSerializable("preset");
                savedPreset = preset;
                txtNombrePresset.setText(preset.getNombre_preset());
                checkPedal1.setChecked(preset.getPedal_one().equals("1"));
                checkPedal2.setChecked(preset.getPedal_two().equals("1"));
                checkPedal3.setChecked(preset.getPedal_three().equals("1"));
                checkPedal4.setChecked(preset.getPedal_four().equals("1"));
                id = preset.getID();
            }
        }
    }

    private void startBluetooth(){
        if(bAdapter.isEnabled()){
            BluetoothSocket temp;
            bDevice = bAdapter.getRemoteDevice(MAC_ADDRESS);
            try{
                temp = bDevice.createRfcommSocketToServiceRecord(BT_UUID);
                bSocket = temp;
                bSocket.connect();
                Toast.makeText(MainActivity.this,"Conectado a " + bDevice.getName(),Toast.LENGTH_SHORT).show();
            }catch (IOException e){
                Toast.makeText(MainActivity.this,"Error en el socket",Toast.LENGTH_SHORT).show();
                try{
                    bSocket.close();
                }catch (IOException e2){
                    Toast.makeText(MainActivity.this,"Error al cerrar socket",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(bSocket != null){
            if(!bSocket.isConnected()){
                startBluetooth();
            }
            if(!bAdapter.isEnabled()){
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i,1);
            }
            bluetooth = new ConnectionThread(bSocket,handler);
            bluetooth.start();
        }else{
            if(!bAdapter.isEnabled()){
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i,1);
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(bSocket != null){
            try{
                bSocket.close();
            }catch (IOException e){

            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(bSocket != null){
            try{
                bSocket.close();
            }catch (IOException e){

            }
        }
    }

    private String[] valueFromCheckBoxes(){
        String[] data = new String[5];
        data[1] = checkPedal1.isChecked()? "1" : "0";
        data[2] = checkPedal2.isChecked()? "1" : "0";
        data[3] = checkPedal3.isChecked()? "1" : "0";
        data[4] = checkPedal4.isChecked()? "1" : "0";
        return data;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnSalir:
                finish();
                break;

            case R.id.btnEnviar:
                boolean exito = false;
                if(bSocket.isConnected() && bluetooth != null){
                    if(txtNombrePresset.getText().toString().equals("")){
                        Toast.makeText(MainActivity.this,"Ingrese nombre preset",Toast.LENGTH_SHORT).show();
                    }else{
                        enviar();
                        for(String inputData : input){
                            if(inputData.equals("s")){
                                Toast.makeText(MainActivity.this,"Datos enviados y guardados",Toast.LENGTH_SHORT).show();
                                exito = true;
                                break;
                            }
                        }
                        if(!exito)
                            Toast.makeText(MainActivity.this,"No se ha podido enviar",Toast.LENGTH_LONG).show();
                        if(exito)
                            saveInDatabase();
                    }

                }else{
                    Toast.makeText(MainActivity.this,"No hay conexión",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnOnlinePressets:
                onlinePressets();
                break;
        }
    }

    public void enviar(){
        String[] data = valueFromCheckBoxes();
        data[0] = "s";
        for(String output : data){ bluetooth.write(output);}
    }

    private void saveInDatabase(){
        if(txtNombrePresset.getText().toString().equals("")){
            Toast.makeText(MainActivity.this,"Ingrese un nombre",Toast.LENGTH_SHORT).show();
        }else{
            db.openDatabase();
            Preset preset = new Preset();
            preset.setNombre_preset(txtNombrePresset.getText().toString());
            preset.setPedal_one(valueFromCheckBoxes()[1]);
            preset.setPedal_two(valueFromCheckBoxes()[2]);
            preset.setPedal_three(valueFromCheckBoxes()[3]);
            preset.setPedal_four(valueFromCheckBoxes()[4]);
            if(savedPreset == null){
                long idx = db.insertPreset(preset);
                if(idx > 0){
                    Toast.makeText(MainActivity.this,"Se ha guardado el preset",Toast.LENGTH_SHORT).show();
                    limpiar();
                }else{
                    Toast.makeText(MainActivity.this,"No se ha guardado el preset",Toast.LENGTH_SHORT).show();
                }
            }else{
                long idx = db.updatePreset(preset,id);
                System.out.println(preset.getID());
                if(idx > 0){
                    Toast.makeText(MainActivity.this,"Se ha actualizado el preset",Toast.LENGTH_SHORT).show();
                    limpiar();
                }else{
                    Toast.makeText(MainActivity.this,"No se ha actualizado el preset",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onlinePressets(){
        Intent i = new Intent(MainActivity.this, ListActivity.class);
        startActivityForResult(i,2);
    }

    private void limpiar(){
        checkPedal1.setChecked(false);
        checkPedal2.setChecked(false);
        checkPedal3.setChecked(false);
        checkPedal4.setChecked(false);
        txtNombrePresset.setText("");
        id = 0;
        savedPreset = null;
    }
}
