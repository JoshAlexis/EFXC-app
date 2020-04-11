package com.project.efxcremote;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>ConnectionThread es la clase con la que se envían y reciben datos
 *    desde el módulo bluetooth de arduino hacia la app. Es necesario que
 *    sea un hilo ya que arduno envía un byte a la vez cuando la información
 *    puede ser de más bytes, y en cierta manera no se sabe "cuando" se
 *    van a recibir los datos. Con el hilo se puede estar verificando y recibiendo
 *    todos los bytes que envía el arduino.
 */
public class ConnectionThread extends Thread {
        /**
         * El InputStream que devuelve el socket con <code>getInputStream</code>
         */
        private InputStream inStream;
        /**
         * El OutputStrems de devuelve el socket con <code>getOutputStream</code>
         */
        private OutputStream outStream;
        private Handler handler;
        /**
         * Es el BluetoothSocket conectado al modulo Bluetooth de arduino
         */
        private BluetoothSocket bSocket;

        /**
         * Constructor.
         * <p>Recibe un <code>BluetoothSocket</code> y un <code>Handler</code> los cuales son
         *    creados al iniciar la aplicación, con lo cual se establecerá
         *    el envío y recibo de datos.
         * <p>Dentro del constructor se obtienen los streams conectados con el socket y los
         *    cuales son asignados a la clase.
         *
         * @param bSocket Socket de comunicación entre el modulo bluetooth HC-05 y la aplicación
         * @param handler handler para el envío de datos al hilo main
         * @see BluetoothSocket#getInputStream()
         * @see BluetoothSocket#getOutputStream()
         */
        public ConnectionThread(BluetoothSocket bSocket, Handler handler){
            this.bSocket = bSocket;
            this.handler = handler;
            InputStream inStream = null;
            OutputStream outStream = null;
            try{
                inStream = this.bSocket.getInputStream();
                outStream = this.bSocket.getOutputStream();
            }catch (IOException e){
                System.out.println("Error en Streams " + e.getMessage());
            }
            this.inStream = inStream;
            this.outStream = outStream;
        }
        /**
         * <p>Dentro del hilo lo que se hace es obtener por medio del InputStream
         * los bytes recibidos desde arduino. El resultado de la lectura del InputStream es
         * utilizado para crear un String con contendrá la información recibida.
         *
         * <p>Teniendo en cuenta que se está trabajando con hilos y se tiene que enviar
         * la información del hilo creado por <code>ConnectionThread</code> al hilo
         * que se crea en el <code>MainActivity</code> se utiliza un <code>Handler</code> y
         * a través del método <code>obtainMessage</code>
         * se obtiene un objeto <code>Message</code> que será enviado y procesado
         * por el hilo main
         * <p>El parámetro <code>what</code> () dentro de <code>obtainMessage</code>
         * es para asignarle un codigo para identificar de qué es el mensaje. El último parámetro es la
         * información que queremos enviar. El método
         */
        @Override
        public void run(){
            byte[] buffer = new byte[256];
            int bytes;
            while(true){
                try{
                    bytes = inStream.read(buffer);
                    String message = new String(buffer, 0, bytes);
                    handler.obtainMessage(0, bytes, -1,message).sendToTarget();
                }catch (IOException e){
                    System.out.println("Error al recibir datos " + e.getMessage());
                    break;
                }
            }
            System.out.println("Hilo finalizado");
        }

        /**
         *  El método <code>write</code> es el encargado de enviar la información desde la
         *  app hasta el arduino por medio de un <code>OutputStram</code>.
         * @param text Es la información que se enviará al arduino. En este caso es un <code>String</code>
         */
        public void write(String text){
            System.out.println(text);
            try{
                System.out.println("Escribiendo bytes");
                outStream.flush();
                outStream.write(text.getBytes());
                outStream.flush();
            }catch (IOException e){
                System.out.println("Error al enviar datos " + e.getMessage());
            }
        }
}
