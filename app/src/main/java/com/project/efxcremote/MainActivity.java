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
    /**
     * Constante para verificar el mensaje recibido desde el handler
     * y obtenido por el hilo en <code>ConnectionThread</code>
     */
    final static int HANDLE_STATE = 0;
    //El Identicador Único Inmutable Universal asociado con el módulo HC-05
    private final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    //Representa el bluetooth en sí del smartphone
    private BluetoothAdapter bAdapter;
    //Represeta el canal de comunicación por medio de bluetooth
    private BluetoothSocket bSocket;
    //Representa el dispositivo con que se está conectado por medio de bluetooth
    private BluetoothDevice bDevice;
    //La clase para enviar y recibir datos del arduino
    private ConnectionThread bluetooth;
    private Handler handler;
    //Elementos de la interfaz
    private TextView txtRespuesta,txtPedal1, txtPedal2,txtPedal3,txtPedal4;
    private CheckBox chkPedal1,chkPedal2,chkPedal3,chkPedal4;
    private Button btnEncender, btnApagar, btnEnviar, btnLeer, btnLimpiar, btnSalir;
    //Para poder guardar la información recibida desde el arduino
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
                if(bSocket.isConnected() && bluetooth != null)
                    bluetooth.write("e");
                else
                    Toast.makeText(MainActivity.this,"No hay conexión",Toast.LENGTH_SHORT).show();
            }
        });

        btnApagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bSocket.isConnected() && bluetooth != null)
                    bluetooth.write("a");
                else
                    Toast.makeText(MainActivity.this,"No hay conexión",Toast.LENGTH_SHORT).show();
            }
        });
        /*
         * El botón enviar manda un arreglo con los valores
         * de los checkbox por medio de un ciclo. Los valores
         * se guardan en un arreglo de String y se usa un foreach
         * junto al método write
         */
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bSocket.isConnected() && bluetooth != null){
                    String[] data = valueFromCheckBoxes();
                    data[0] = "s";
                    for(String output : data){
                        bluetooth.write(output);
                    }
                }else{
                    Toast.makeText(MainActivity.this,"No hay conexión",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bSocket.isConnected() && bluetooth != null)
                    bluetooth.write("r");
                else
                    Toast.makeText(MainActivity.this,"No hay conexión",Toast.LENGTH_SHORT).show();
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
        /*
        * Por medio del handler se recibe el mensaje enviado
        * por el hilo de ConnectionThread y se guarda la información
        * en un arrayList de tipo String.
        * Antes de agregar el dato se verifica si pertenece a los
        * a los valores válidos de 1 o 0.
        * Al final se muestra la informacion en los TextViews
        * */
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
        //Obtiene el bluetooth del dispositivo, en esta caso del smartphone
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        /*
        * Verificamos si el bluetooth está encendido, si es así llama al método
        * starBluetooth para iniciar la comunicación. En caso contrario se realiza
        * un Intent para solicitar al usuario para que enciendo en bluetooth
        * */
        if(bAdapter.isEnabled()){
            startBluetooth();
        }else{
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
        }
    }
    //Si el usuario enciendo el bluetooth llamará al método para comenzar la comunicación
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK && requestCode == 1){
            startBluetooth();
        }
    }
    /**
     * <p>Con este método se conecta al arduino para enviar y
     * recibir los datos necesarios.
     * <p>Para ello primero verifica si el bluetooth está encendido.
     * Si está encendido, entoces crea un <code>BluetoothSocket</code> temporal
     * y por medio del método <code>getRemoteDevice()</code> se obtiene el
     * dispositivo a conectarse, en este caso el módulo HC-05, a través de su
     * dirección MAC conocida de antemano.
     * <p>Con el <code>BluetoothDevice</code> y el <code>UUID</code> del
     * módulo bluetooth se crea un <code>BluetoothSocket</code> con el método
     * <code>createRfcommSocketToServiceRecord()</code> y asigna al socket temporal.
     * Al socket de la clase se le asigna el <code>BluetoothSocket</code> temporal y se
     * procede a llamar al método <code>connect()</code> para poder establecer la
     * comunicación con el módulo HC-05.
     */
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
    /*
    * El método onResume se llama cuando la activity está en primer plano y se
    * puede interactuar con la app. Es por ello que este momento se inicia
    * el hilo de comunicación con el arduino.
    * Para ello verifica que el socket ya esté creado. Si ya lo está entonces se comprueba
    * si está conectado, si lo está entonces crea un objeto de clase ConnectionThread
    * e inicia el hilo.
    * Si el socket no está creado, lo cual pasa cuando se rechaza encender el bluetooth,
    * se volverá a pedir que encienda el bluetooth hasta que lo haga.
    * */
    @Override
    protected void onResume(){
        super.onResume();
        if(bSocket != null){
            if(!bSocket.isConnected()){
                startBluetooth();
            }
            if(!bAdapter.isEnabled()){
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i,0);
            }
            bluetooth = new ConnectionThread(bSocket,handler);
            bluetooth.start();
        }else{
            if(!bAdapter.isEnabled()){
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i,0);
            }
        }
    }
    /*
    * Con los métodos onPause y onStop se detiene la comunicación con el arduino
    * cuando el usuario deja la aplicación en segundo plano. Cuando la aplicación
    * vuelve a primer plano el método onResume restablece la comunicación
    * */
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
    //Para limpiar los TextViews y reiniciar los checkboxes
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

    /**
     * Obtiene los valores de los checkbox convertidos a String
     *
     * @return String[] Un arreglo con los valores de los
     * checkboxes. A partir de <code>1 a 4</code> son los
     * valores de los checkbox 1,2,3 y 4 respectivamente.
     */
    private String[] valueFromCheckBoxes(){
        String[] data = new String[5];
        data[1] = chkPedal1.isChecked()? "1" : "0";
        data[2] = chkPedal2.isChecked()? "1" : "0";
        data[3] = chkPedal3.isChecked()? "1" : "0";
        data[4] = chkPedal4.isChecked()? "1" : "0";
        return data;
    }
    //Muestra en respectivo TextView el dato recibido por arduino
    private void readInput(){
        switch (input.size()){
            case 1:
                if(input.get(0).equals("s")){
                    txtRespuesta.setText("Datos guardados");
                }else{
                    txtPedal1.setText("Pedal 1 State: " + input.get(0));
                }
                break;
            case 2:
                txtPedal2.setText("Pedal 2 State: " + input.get(1));
                break;
            case 3:
                txtPedal3.setText("Pedal 3 State: " + input.get(2));
                break;
            case 4:
                txtPedal4.setText("Pedal 4 State: " + input.get(3));
                break;
        }
    }
}
