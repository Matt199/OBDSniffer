package com.example.mattkopacz.obdsniffer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView userInfo;
    private BluetoothAdapter mBluetoothAdapter;

    private List<String> nameList = new ArrayList<String>();
    private List<String> adresList = new ArrayList<String>();

    public String adres = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInfo = (TextView) findViewById(R.id.userInfo);

        checkAdapter();
        checkConnectedDevices();
    }

    public void connectButtonClicked(View view){


        onCreateDialog().show();

    }


    // Check if there is bluetooth adapter

    private void checkAdapter(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){

            userInfo.setText("Brak dostepnego adapatera Bluetooth....");


        } else {

            // Jeżeli urządzenie posiada adapter ale nie jest on włączony
            if (!mBluetoothAdapter.isEnabled()) {

                // Każ urzytkownikowi włączyć Bluetooth

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);

            } else {

                // Jeśli jest wszystko ok to powiedz urzytkownikowi, ze jest ok

                userInfo.setText("Wszystko jest ok");

            }


        }

    }

    private void checkConnectedDevices(){

        // Sprawdzam ile urządzeń mam podłączonych za pomocą Bluetooth

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // Jeżeli mam podłączone przynajmniej jedno to..

        if(pairedDevices.size() > 0) {

            // iteruj i dodaj je do listy

            for(BluetoothDevice device : pairedDevices) {

                String deviceName = device.getName();        // Pobierz Nazwę urządzenia
                String deviceAdress = device.getAddress();   // Pobierz adress MAC urządzenia

                adresList.add(deviceAdress);
                nameList.add(deviceName);

            }

        }

    }


    private Dialog onCreateDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Lista urządzeń BT")
                .setSingleChoiceItems(nameList.toArray(new String[nameList.size()]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        userInfo.setText(adresList.get(i)); // Wybieram za pomocą int element z listy

                        // Po wybraniu zmieniam okno

                        adres = adresList.get(i);

                        Intent intent = new Intent(MainActivity.this, ConnectedActivity.class);

                        intent.putExtra("EXTRA_ADRESS", adresList.get(i));

                        // Wyświetl wskaźnik postępu
                        progressDialog = ProgressDialog.show(MainActivity.this, "Conecting...", "Please Wait");


                        // Wyświetl nowe okno

                        startActivity(intent);

                    }
                });

        return builder.create();

    }


}
