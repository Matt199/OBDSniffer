package com.example.mattkopacz.obdsniffer;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.UUID;
import android.os.Handler;
import android.os.Message;
import java.util.logging.LogRecord;

public class ConnectedActivity extends AppCompatActivity {

    private BluetoothAdapter btAdapter;
    private String adress;
    private static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mmSocket;

    private boolean isBTConnected = false;

    private ProgressDialog progress;


    private TextView readValue;

    private ConnectedThread mConnectedThread;


    Handler h;

    final int handlerState = 0;

    public StringBuilder recDataString = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);


        readValue = (TextView) findViewById(R.id.readValue);

        Intent intent = getIntent();

        adress = intent.getStringExtra("EXTRA_ADRESS");

        new ConnectBT().execute();

        h = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == handlerState){

                    byte[] rBuff = (byte[]) msg.obj; // recived message (bytes)

                    String readMessage = new String(rBuff, 0, msg.arg1); // convert that message to string

                    recDataString.append(readMessage);

                    Log.d("Od2", recDataString.toString());

                    int startOfIndex = recDataString.indexOf("41 0C");
                    int endOfIndex = recDataString.indexOf(">");

                    if (endOfIndex > 0) {

                        String wiadomosc = recDataString.substring(startOfIndex + 17, endOfIndex);

                        if (wiadomosc.contains(" ")) {

                            wiadomosc = wiadomosc.replace(" ", "");

                        }

                        BigInteger wartoscBig = new BigInteger(wiadomosc, 16);

                        int wartoscInt = wartoscBig.intValue();

                        double RPM = wartoscInt/4;

                        Log.d("Wiadomosc", String.valueOf(wiadomosc));
                        Log.d("Wartosc BIG", String.valueOf(wartoscBig));
                        Log.d("Wartpsc INT", String.valueOf(wartoscInt));
                        Log.d("Wartosc RPM", String.valueOf(RPM));
                        Log.d("Start Index", String.valueOf(startOfIndex));
                        Log.d("Stop Index", String.valueOf(endOfIndex));

                        readValue.setText(recDataString.toString());

                        recDataString.delete(0, recDataString.length());

                    }

                    //Log.d("Odczyt", readMessage);


                    }
                }

        };

    }



    public void connectionStatusButton(View view) {

        checkELMstatus();

    }

    private void checkELMstatus(){

        if (mmSocket != null){

            try {
                mmSocket.getOutputStream().write("010C\r".getBytes());
            } catch (IOException e) {

                Log.e("Status", String.valueOf(e));
            }

        }

    }




    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){

            mmSocket = socket;

            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {

                tempIn = socket.getInputStream();

            } catch (Exception e) {

                Log.e("Error", "Error occurred when creating input stream", e);
            }

            try {
                tempOut = socket.getOutputStream();
            } catch (Exception e) {

                Log.e("Error", "Error occurred when creating output stream", e );
            }

            mmInStream = tempIn;
            mmOutStream = tempOut;


        }


        public void run() {
            super.run();

            byte [] mmBuffer = new byte[256];
            int numBytes;


            while (true){

                try {

                    numBytes = mmInStream.read(mmBuffer);

                    h.obtainMessage(handlerState, numBytes, -1, mmBuffer).sendToTarget();



                } catch (IOException e) {

                    Log.e("Error", "Error while reading msg", e);
                    break;
                }

            }

        }
    }





    private class ConnectBT extends AsyncTask<Void, Void, Void> {


        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(ConnectedActivity.this, "Connecting...","Please Wait!" );
        }


        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if (mmSocket == null || !isBTConnected) {

                    btAdapter = BluetoothAdapter.getDefaultAdapter();

                    BluetoothDevice device = btAdapter.getRemoteDevice(adress);

                    mmSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);


                    mmSocket.connect();

                    mConnectedThread = new ConnectedThread(mmSocket);
                    mConnectedThread.start();


                }
            } catch (IOException e){

                ConnectSuccess = false;
            }

            return null;

        }




        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!ConnectSuccess) {

                finish();

            } else {

                isBTConnected = true;

            }

            progress.dismiss();
        }
    }
}


