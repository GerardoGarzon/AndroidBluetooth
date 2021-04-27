package com.example.proyectobluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Conexion extends Thread{
    Handler bluetoothIn = new Handler();
    final int handlerState = 0;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    boolean isOn = false;

    public Conexion(BluetoothSocket socket) {

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[256];
        int bytes;
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                if(readMessage.equals("1")){
                    isOn = true;
                }else{
                    isOn = false;
                }
                System.out.println(readMessage);
            } catch (IOException e) {
                break;
            }
        }
    }

    public boolean getStatus(){
        return isOn;
    }

    //Envio de trama
    public void write(String input) {
        try {
            mmOutStream.write(input.getBytes());
        } catch (IOException e) {
            System.out.println("La Conexión fallo");
        }
    }
}