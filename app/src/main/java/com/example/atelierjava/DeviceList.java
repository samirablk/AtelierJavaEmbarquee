package com.example.atelierjava;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class DeviceList extends AppCompatActivity {


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;


    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66"); //TODO
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> itemsAdapter;
    private ArrayList<BluetoothDevice> discoveredList;
    private ArrayList<BluetoothDevice> pairedList;

    private static final String TAG = "Client";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    final BroadcastReceiver receiver = new BroadcastReceiver() { //DISCOVERY
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Found a fucking device: " + device.getName());
                itemsAdapter.add(device.getName() + "\n" + device.getAddress());
                discoveredList.add(device);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (itemsAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    itemsAdapter.add(noDevices);
                }
                Toast.makeText(DeviceList.this, "End Discovery", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        discoveredList = new ArrayList<>();
        pairedList = new ArrayList<>();

        setResult(Activity.RESULT_CANCELED);

        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                discover();
                v.setVisibility(View.GONE);
            }
        });

        ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.device_name);
        itemsAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(itemsAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            bluetoothAdapter.cancelDiscovery();

            // Obtenir l'adresse MAC de l'appareil, qui correspond aux 17 derniers caractères de la vue
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Créer le résultat Intent et inclure l'adresse MAC
            Intent intent = new Intent(DeviceList.this, Pairing.class);
            intent.putExtra("SELECTED DEVICE", discoveredList.get(arg2));
            startActivity(intent);
        }
    };

    private void discover() {
        Log.d(TAG, "discover()");

        setProgressBarIndeterminateVisibility(true); //TODO
        setTitle(R.string.scanning);
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(receiver);
    }

}
