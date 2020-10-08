package com.example.atelierjava;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class Server extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final static int REQUEST_ENABLE_BT = 1;
    private ProgressBar progressBar;
    private static final String TAG = "DownloadService";
    private static final String OUTPUT_FILE_NAME = "DownloadedVideo.mp4";
    private static final String VIDEO_URL = "https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";
    private LocalBroadcastManager localBroadcastManager;
    private Thread sThread;

    //Thread pour la socket server
    private class AcceptThread extends Thread {

        private static final String APP_NAME = "BTChat";
        private  final UUID MY_UUID=UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
        OutputStream mmOutStream = null;


        private final BluetoothServerSocket mmServerSocket;
        private static final String TAG="SERVER SOCKET: ";
        public AcceptThread() {
            //Utiliser un objet temporaire affecté ultérieurement à mmServerSocket qui est finale
            BluetoothServerSocket tmp = null;
            try {

                // MY_UUID est la chaîne UUID de l'application, également utilisée par le code client.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;

            //Continuer à écouter jusqu'à ce qu'une exception se produise ou qu'une socket soit renvoyé.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    //Une connexion a été acceptée. Effectuer le travail associé.
                    // la connexion dans un thread séparé.
                    // Gérer la socket
                    connected(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        // Ferme le socket de connexion et entraîne la fin du thread.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

        private void connected(BluetoothSocket socket) {
            try {
                mmOutStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String rootDir = Environment.getExternalStorageDirectory()
                        + File.separator + "Video";
                File rootFile = new File(rootDir);
                rootFile.mkdir();

                byte[] bytes = getByte(Environment.getExternalStorageDirectory()
                        + File.separator + "Video/" + OUTPUT_FILE_NAME);
                try {
                    mmOutStream.write(bytes);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mmOutStream.close();

                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_layout);

        String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
        startActivityForResult(new Intent(aDiscoverable), 1);

        final Button button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (bluetoothAdapter == null) {
                    // L'appareil ne prend pas en charge Bluetooth
                    System.out.println("This device doesn't support bluetooth");
                }
                // demande à l'utilisateur d'activer blutotooh si blutooth est désactivé sans quitter l'application
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                // Le code s'exécute ici sur le thread principal après que l'utilisateur appuie sur le bouton
                AcceptThread cth = new AcceptThread();
                cth.start();
            }
        });

        final Button buttonDownload = findViewById(R.id.downloadButton);
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(Server.this, "Download clicked", Toast.LENGTH_SHORT).show();
                new DownloadingTask().execute();
            }
        });
    }

    private byte[] getByte(String path) {
        byte[] getBytes = {};
        try {
            File file = new File(path);
            getBytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(getBytes);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getBytes;
    }

    public class DownloadingTask extends AsyncTask<Void, Void, Void> {

        String outputFile;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Download Started", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                String rootDir = Environment.getExternalStorageDirectory()
                        + "/" + "Video";
                File rootFile = new File(rootDir);
                rootFile.mkdir();
                URL url = new URL(VIDEO_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                int status = httpURLConnection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + status
                            + " " + httpURLConnection.getResponseMessage());
                }

                File localFile = new File(rootFile, OUTPUT_FILE_NAME);
                outputFile = "PATH OF LOCAL FILE : " + localFile.getPath();
                if (rootFile.exists()) Log.d(TAG, outputFile);
                if (!localFile.exists()) {
                    localFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(localFile);
                InputStream in = httpURLConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int nbOfPaquetsReceived = 0;
                int len1 = 0;
                try {
                    while ((len1 = in.read(buffer)) > 0) {
                        nbOfPaquetsReceived++;
                        fos.write(buffer, 0, len1);
                    }
                } catch (IOException se) {
                    Log.d(TAG, "Okay");
                }
                fos.close();
                in.close();


            } catch (IOException e) {
                Log.d("Error....", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Download Failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Download Failed");

                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());

            }
            super.onPostExecute(result);
        }
    }

    public void updateProgressBar(int progress) {
        progressBar.setVisibility(View.VISIBLE);
        this.progressBar.setProgress(progress);
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
