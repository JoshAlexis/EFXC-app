package com.project.efxcremote;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectionThread extends Thread {
        private InputStream inStream;
        private OutputStream outStream;
        private Handler handler;
        private BluetoothSocket bSocket;

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
