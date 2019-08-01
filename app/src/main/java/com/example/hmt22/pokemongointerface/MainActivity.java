package com.example.hmt22.pokemongointerface;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {

    public static boolean locationOK;
    public static LocationManager locationManager;

    public static String host = "cms-r1-24.cms.waikato.ac.nz";
    public static int port = 45525;
    public static String username;

    public void createAccount(View view){
        Intent intent  = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    public void signIn(View view){

        TextView un = findViewById(R.id.userName);
        TextView pw = findViewById(R.id.password);

        username = un.getText().toString();
        final String pass = pw.getText().toString();


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(host, port);
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    w.write("LOGIN," + username + "," + pass + "\n");
                    w.flush();

                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String response = r.readLine();
                    final boolean b = response.equals("true");
                    Log.d("asdf", "Signing in");
                    s.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (b) {
                                Intent intent  = new Intent(MainActivity.this, SignInActivity.class);
                                startActivity(intent);
                            } else {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                                builder1.setMessage("Incorrect login details");
                                builder1.setCancelable(true);

                                builder1.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                            }
                        }
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Location permissions
        locationOK = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 42);
        } else {
            locationOK = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 42) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationOK = true;
            }
        }
    }


}
