package com.example.hmt22.pokemongointerface;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // if already in database recieve "ALREADY_EXISTS"

    public void createAccount(View view){
        Log.d("boop", "Clicked");

        TextView user = findViewById(R.id.username);
        TextView pass = findViewById(R.id.password);
        TextView dev = findViewById(R.id.numDevices);

        final String u = user.getText().toString();
        final String p = pass.getText().toString();
        final String d = dev.getText().toString();

        MainActivity.username = u;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("boop", "running thread");
                    Socket s = new Socket(MainActivity.host, MainActivity.port);
                    Log.d("boop", "socket made");

                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    Log.d("boop", "reader/writer made");


                    Log.d("boop", u);
                    Log.d("boop", p);
                    Log.d("boop", d);


                    //"NEW_PLAYER,username,password,num devices"
                    w.write("NEW_PLAYER," + u + "," +
                            p + ","  +
                            d +"\n");
                    w.flush();

                    final String response = r.readLine();

                    Log.d("account", "New Account");
                    s.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!response.equals("ALREADY_EXISTS")) {
                                Intent intent  = new Intent(CreateAccountActivity.this, SignInActivity.class);
                                startActivity(intent);
                            } else {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(CreateAccountActivity.this);
                                builder1.setMessage("Username already exists");
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

}
