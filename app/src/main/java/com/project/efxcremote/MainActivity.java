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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    final static String MAC_ADDRESS = "00:18:E4:40:00:06";
    final static int HANDLE_STATE = 0;
    private final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter bAdapter;
    private BluetoothSocket bSocket;
    private BluetoothDevice bDevice;
    private ConnectionThread bluetooth;
    private Handler handler;
    private TextView txtRespuesta,txtPedal1, txtPedal2,txtPedal3,txtPedal4;
    private CheckBox chkPedal1,chkPedal2,chkPedal3,chkPedal4;
    private Button btnEncender, btnApagar, btnEnviar, btnLeer, btnLimpiar, btnSalir;
    ArrayList<String> input = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtRespuesta = (TextView) findViewById(R.id.txtRespuesta);
        txtPedal1 = (TextView) findViewById(R.id.txtPedal1);
        txtPedal2 = (TextView) findViewById(R.id.txtPedal2);
        txtPedal3 = (TextView) findViewById(R.id.txtPedal3);
        txtPedal4 = (TextView) findViewById(R.id.txtPedal4);
        chkPedal1 = (CheckBox) findViewById(R.id.chkPedal1);
        chkPedal2 = (CheckBox) findViewById(R.id.chkPedal2);
        chkPedal3 = (CheckBox) findViewById(R.id.chkPedal3);
        chkPedal4 = (CheckBox) findViewById(R.id.chkPedal4);
        btnEncender = (Button) findViewById(R.id.btnEncender);
        btnApagar = (Button) findViewById(R.id.btnApagar);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);
        btnLeer = (Button) findViewById(R.id.btnLeer);
        btnLimpiar = (Button) findViewById(R.id.btnLimpiar);
        btnSalir = (Button) findViewById(R.id.btnSalir);

        btnEncender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.write("e");
            }
        });

        btnApagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.write("a");
            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] data = valueFromCheckBoxes();
                data[0] = "s";
                for(String output : data){
                    bluetooth.write(output);
                }
            }
        });

        btnLeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.write("r");
            }
        });

        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpiar();
            }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == HANDLE_STATE){
                    String inputText = (String)msg.obj;
                    if(inputText.equals("1") || inputText.equals("0"))
                        input.add(inputText);
                }
                readInput();
            }
        };

        bAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!bAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
        }else{
            startBluetooth();
        }
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
        if(!bSocket.isConnected()){
            startBluetooth();
        }
        bluetooth = new ConnectionThread(bSocket,handler);
        bluetooth.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            bSocket.close();
        }catch (IOException e){

        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        try{
            bSocket.close();
        }catch (IOException e){

        }
    }

    private void limpiar(){
        txtRespuesta.setText(R.string.answer);
        txtPedal1.setText(R.string.pedal1state);
        txtPedal2.setText(R.string.pedal2state);
        txtPedal3.setText(R.string.pedal3state);
        txtPedal4.setText(R.string.pedal4state);
        chkPedal1.setChecked(false);
        chkPedal2.setChecked(false);
        chkPedal3.setChecked(false);
        chkPedal4.setChecked(false);
        input.clear();
    }

    private String[] valueFromCheckBoxes(){
        String[] data = new String[5];
        data[1] = chkPedal1.isChecked()? "1" : "0";
        data[2] = chkPedal2.isChecked()? "1" : "0";
        data[3] = chkPedal3.isChecked()? "1" : "0";
        data[4] = chkPedal4.isChecked()? "1" : "0";
        return data;
    }

    private void readInput(){
        switch (input.size()){
            case 1:
                txtPedal1.setText(input.get(0));
                break;
            case 2:
                txtPedal2.setText(input.get(1));
                break;
            case 3:
                txtPedal3.setText(input.get(2));
                break;
            case 4:
                txtPedal2.setText(input.get(3));
                break;
        }
    }
}
