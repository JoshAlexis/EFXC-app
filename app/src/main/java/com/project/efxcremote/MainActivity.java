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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.project.efxcremote.database.DBPresets;
import com.project.efxcremote.database.Preset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    final static String MAC_ADDRESS = "00:18:E4:40:00:06";
    /**
     * Constante para verificar el mensaje recibido desde el handler
     * y obtenido por el hilo en <code>ConnectionThread</code>
     */
    final static int HANDLE_STATE = 0;
    //El Identicador Único Inmutable Universal asociado con el módulo HC-05
    private final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    //Objetos para conexion Bluetooth
    private BluetoothAdapter bAdapter;
    private BluetoothSocket bSocket;
    private BluetoothDevice bDevice;
    //La clase para enviar y recibir datos del arduino
    private ConnectionThread bluetooth;
    private Handler handler;
    //Elementos de la interfaz
    private CheckBox checkPedal1, checkPedal2, checkPedal3, checkPedal4;
    private Button btnOnlinePressets, btnSalir, btnGuardar;
    private EditText txtNombrePresset;
    //Para la base de datos y poder actualizar
    private DBPresets db;
    private Preset savedPreset;
    private long id;
    //Para poder guardar la información recibida desde el arduino
    ArrayList<String> input = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarComponentes();
        iniciarEventos();

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



    /**
     * Asigna los elementos del layout con su respectivo elemento
     * e inicia los atributos necesarios.
     */
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
    //Agregar los listener a los botones
    public void iniciarEventos(){
        this.btnSalir.setOnClickListener(this);
        this.btnOnlinePressets.setOnClickListener(this);
        this.btnGuardar.setOnClickListener(this);
    }
    /*
     * Existen dos métodos dentro del código que utilizan startActivityForResults:
     * 1) Cuando se trata de iniciar el Bluetooth.
     * 2) Cuando se muestra el activity con los presets guardados,
     *
     * Para poder manejar los resultados de esos métodos, se utiliza un requestCode
     * para diferenciarlos. El requestCode 1 corresponde a la activación del Bluetooth,
     * cuando se ha activado el bluetooth por parte del usuario se procede a iniciar el
     * hilo de comunicación con el arduino.
     * El requestCode 2 se para cuando en el listActivity se seleccione la opción "Seleccionar"
     * y poder mostrar la información del preset por pantalla,
     * */
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
    /**
     * Obtiene los valores de los checkbox convertidos a String
     *
     * @return String[] Un arreglo con los valores de los
     * checkboxes. A partir de <code>1 a 4</code> son los
     * valores de los checkbox 1,2,3 y 4 respectivamente.
     */
    private String[] valueFromCheckBoxes(){
        String[] data = new String[5];
        data[1] = checkPedal1.isChecked()? "1" : "0";
        data[2] = checkPedal2.isChecked()? "1" : "0";
        data[3] = checkPedal3.isChecked()? "1" : "0";
        data[4] = checkPedal4.isChecked()? "1" : "0";
        return data;
    }
    //Las funciones que ejecutan los botones
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnSalir:
                finish();
                break;

            case R.id.btnEnviar:
                //Para verificar que se haya enviado la información al arduino
                boolean exito = false;
                //Verificamos si hay conexion con el arduino
                if(bSocket.isConnected() && bluetooth != null){
                    if(txtNombrePresset.getText().toString().equals("")){
                        Toast.makeText(MainActivity.this,"Ingrese nombre preset",Toast.LENGTH_SHORT).show();
                    }else{
                        enviar();
                        /*
                        * Cuando enviamos los datos esperamos a recibir el dato que nos indica que se ha
                        * guardado la información en el arduino
                        * */
                        for(String inputData : input){
                            if(inputData.equals("s")){
                                Toast.makeText(MainActivity.this,"Datos enviados y guardados",Toast.LENGTH_SHORT).show();
                                exito = true;
                                break;
                            }
                        }
                        if(!exito)
                            Toast.makeText(MainActivity.this,"No se ha podido enviar",Toast.LENGTH_LONG).show();
                        if(exito)//Si se guardó almacenamos el preset en la base de datos
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
    //Para enviar los datos al arduino
    public void enviar(){
        String[] data = valueFromCheckBoxes();
        data[0] = "s";
        for(String output : data){ bluetooth.write(output);}
    }
    //para poder guardar en la base de datos
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
                //Con el idx verificamos si se guardó o actualizó la información
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
    //Para llamar al listActivity con los presets guardados
    public void onlinePressets(){
        Intent i = new Intent(MainActivity.this, ListActivity.class);
        startActivityForResult(i,2);
    }
    //Para limpiar los TextViews y reiniciar los checkboxes
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
