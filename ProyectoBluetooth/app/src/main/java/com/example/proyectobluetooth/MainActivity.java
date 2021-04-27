package com.example.proyectobluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import java.io.IOError;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button  conectar, desconectar, ledOff, ledOn;
    TextView mVerifBluetooth;
    ImageView mBluetoothIMG, led;
    Switch bth_en;

    String direccion = "";
    BluetoothAdapter mBlueAdapter;
    BluetoothSocket socket;
    UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Conexion conectorBTH;
    boolean activate = false;
    boolean flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conectar = (Button) findViewById(R.id.conectar);
        desconectar = (Button) findViewById(R.id.desconectar);
        ledOn = (Button) findViewById(R.id.ledON);
        ledOff = (Button) findViewById(R.id.ledOFF);
        mVerifBluetooth = (TextView) findViewById(R.id.bthActivo);
        mBluetoothIMG = (ImageView) findViewById(R.id.imagen);
        led = (ImageView) findViewById(R.id.led);
        bth_en = (Switch) findViewById(R.id.toggle);

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        estados();

        bth_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bth_en.isChecked()){
                    mBlueAdapter.enable();
                    mVerifBluetooth.setText("Activo");
                    desconectar.setEnabled(true);
                    conectar.setEnabled(true);
                    mBluetoothIMG.setImageResource(R.drawable.ic_baseline_bluetooth_24);
                    ledOff.setEnabled(true);
                    ledOn.setEnabled(true);
                    activate = true;
                }else{
                    mBlueAdapter.disable();
                    mVerifBluetooth.setText("Desactivo");
                    desconectar.setEnabled(false);
                    conectar.setEnabled(false);
                    mBluetoothIMG.setImageResource(R.drawable.ic_baseline_bluetooth_disabled_24);
                    ledOff.setEnabled(false);
                    ledOn.setEnabled(false);
                    flag = false;
                }
            }
        });

        ledOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag){
                    conectorBTH.write("1");
                }else{
                    showToast("No se puede encender el LED porque no hay ningun dispostivo conectado");
                }
            }
        });

        ledOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag){
                    conectorBTH.write("0");
                }else{
                    showToast("No se puede encender el LED porque no hay ningun dispostivo conectado");
                }
            }
        });

        conectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> dispositivosConectados = mBlueAdapter.getBondedDevices();

                for (BluetoothDevice dispositivo : dispositivosConectados){
                    if (dispositivo.getName().equals("HC-05")){
                        direccion = dispositivo.getAddress();
                        flag = true;
                    }
                }
                if (flag){
                    onResume();
                }else{
                    showToast("No se encontro el dispositivo");
                }

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(conectorBTH.getStatus()){
                            led.setImageResource(R.drawable.ic_baseline_flash_on_24);
                        }else{
                            led.setImageResource(R.drawable.ic_baseline_flash_off_24);
                        }
                        handler.postDelayed(this, 1000);
                    }
                };

                handler.postDelayed(runnable, 1000);
            }
        });
        desconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    showToast("No se encuentra ningun dispositivo conectado");
                }
                flag = false;
                activate = false;
            }
        });
    }
    private void estados(){
        if (mBlueAdapter == null) {
            mVerifBluetooth.setText("El dispositivo no tiene Bluetooth");
            desconectar.setEnabled(false);
            conectar.setEnabled(false);
        } else {
            if (mBlueAdapter.isEnabled()) {
                bth_en.setChecked(true);
                mVerifBluetooth.setText("Activo");
                desconectar.setEnabled(true);
                conectar.setEnabled(true);
                mBluetoothIMG.setImageResource(R.drawable.ic_baseline_bluetooth_24);
                ledOff.setEnabled(true);
                ledOn.setEnabled(true);
            } else {
                bth_en.setChecked(false);
                mVerifBluetooth.setText("Desactivo");
                desconectar.setEnabled(false);
                conectar.setEnabled(false);
                mBluetoothIMG.setImageResource(R.drawable.ic_baseline_bluetooth_disabled_24);
                ledOff.setEnabled(false);
                ledOn.setEnabled(false);
            }
        }
    }

    public void onResume(){
        super.onResume();
        if(activate){
            BluetoothDevice device = mBlueAdapter.getRemoteDevice(direccion);

            try {
                socket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);;
            }catch (IOException e){
                showToast("La creacion del socket fallo");
            }
            try{
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conectorBTH = new Conexion(socket);
            conectorBTH.start();
        }
    }

    private void showToast(String s){
        Toast t = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        t.show();
    }
}