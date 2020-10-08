

package com.example.atelierjava;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

/**
 * Exemple d'activité montrant comment se connecter au réseau et récupérer des données brutes
 * HTML. Il utilise un fragment qui encapsule les opérations réseau sur une AsyncTask.
 * Cet exemple utilise un TextView pour afficher la sortie.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        else {
            //L'autorisation a déjà été accordée
            Log.d("MAIN_ACTIVITY", "WRITE PERMISSION ALREADY GRANTED");
        }
    }

    public void goToServerActivity(View view) {
        Intent intent = new Intent(this, Server.class);
        startActivity(intent);
    }

    public void goToClientActivity(View view) {
        Intent intent = new Intent(this, DeviceList.class);
        startActivity(intent);
    }
}
