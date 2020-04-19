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

    ArrayList<String> input = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarComponentes();
        iniciarEventos();
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

        /*handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == HANDLE_STATE){
                    String inputText = (String)msg.obj;
                    if(inputText.equals("1") || inputText.equals("0"))
                        input.add(inputText);
                }
                //readInput();
            }
        };

        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bAdapter.isEnabled()){
            startBluetooth();
        }else{
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
        }*/

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void iniciarEventos(){
        this.btnSalir.setOnClickListener(this);
        this.btnOnlinePressets.setOnClickListener(this);
        this.btnGuardar.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK && requestCode == 1){
            startBluetooth();
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
            bluetooth = new ConnectionThread(bSocket,handler);
            bluetooth.start();
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
                enviar();
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

    public void onlinePressets(){
        Intent i = new Intent(MainActivity.this, ListActivity.class);
        startActivity(i);
    }


    /*private void readInput(){
        switch (input.size()){
            case 1:
                if(input.get(0).equals("s")){
                    txtRespuesta.setText("Datos guardados");
                }else{
                    txtPedal1.setText(input.get(0));
                }
                break;
            case 2:
                txtPedal2.setText(input.get(1));
                break;
            case 3:
                txtPedal3.setText(input.get(2));
                break;
            case 4:
                txtPedal4.setText(input.get(3));
                break;
        }
    }*/
}
