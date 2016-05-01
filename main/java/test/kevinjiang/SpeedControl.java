package test.kevinjiang.com.skateboard;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class SpeedControl extends AppCompatActivity {

    private ProgressDialog progress;
    private boolean isBtConnected = false;
    public static final int baseSpeed = 1500;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;

    SeekBar speedBar;
    TextView textview;
    Button disconnect;
    String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newInt = getIntent();
        address = newInt.getStringExtra(MainActivity.EXTRA_ADDRESS);

        setContentView(R.layout.activity_speed_control);

        speedBar = (SeekBar)findViewById(R.id.seekBar);
        textview = (TextView)findViewById(R.id.textView);
        disconnect = (Button)findViewById(R.id.button);

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg("Disconnected.");
                isBtConnected = false;
                finish();
                try {
                    btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        new ConnectBT().execute();

        textview.setText("Speed: " + ((speedBar.getProgress()*4) + baseSpeed) + "/ 1900");
        speedBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int percent_speed;
                    int speed;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        percent_speed = progress;
                        speed = ((progress * 5) + baseSpeed);

                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write((byte)progress);
                            } catch (IOException e) {
                                msg("Error");
                            }
                        }

                        textview.setText("Speed: " + speed + "/ 2000");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seekBar.setProgress(0);
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write((byte)0);
                            } catch (IOException e) {
                                msg("Error");
                            }
                        }
                        textview.setText("Speed: " + ((seekBar.getProgress() * 5) + baseSpeed) + "/ 2000");
                    }
                }
        );
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(SpeedControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
