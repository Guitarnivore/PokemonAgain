package com.example.hmt22.pokemongointerface;

import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class AddRaidActivity extends AppCompatActivity{

    TimePickerDialog picker;
    TextView tv;
    String selectedGym;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_raid);
        tv = findViewById(R.id.TimeInput);
        tv.setInputType(InputType.TYPE_NULL);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                picker = new TimePickerDialog(AddRaidActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hour = Integer.toString(hourOfDay);
                        String minutes = Integer.toString(minute);
                        if(hourOfDay < 10){
                            hour = "0" + hourOfDay;

                        }

                        if(minute < 10){
                            minutes = "0" + minute;

                        }
                        tv.setText(hour + ":" + minutes + ":00" );
                    }
                }, hour, minutes, false);
                picker.show();
            }
        });

        final Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("stringy", parent.getItemAtPosition(position).toString());
                selectedGym = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Populate gym list
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(MainActivity.host, MainActivity.port);
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));

                    w.write("GYM_NAMES,\n");
                    w.flush();

                    ArrayList<String> gymsA = new ArrayList<>();
                    String m;
                    while (!(m = r.readLine()).equals("END")) {
                        gymsA.add(m);
                    }

                    // Creating adapter for spinner
                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(AddRaidActivity.this, android.R.layout.simple_spinner_item, gymsA);
                    // Drop down layout style - list view with radio button
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // attaching data adapter to spinner
                            spinner.setAdapter(dataAdapter);

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

    public void newRaid(View view) {
        EditText ETpokemonType = findViewById(R.id.PokemonType);
        final String pokemonType = ETpokemonType.getText().toString();
        EditText ETraidTime = findViewById(R.id.TimeInput);
        final String raidTime = ETraidTime.getText().toString();
        RatingBar RBraidLevel = findViewById(R.id.RaidLevel);
        final String raidLevel = Integer.toString(Math.round(RBraidLevel.getRating()));

        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("RAIDSDD", "Creating Socket");
                        Socket socket = new Socket(MainActivity.host, MainActivity.port);
                        Boolean b = socket.isConnected();

                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        bw.write("INSERT,RAID," + raidTime + "," + raidLevel + "," + pokemonType + "," + selectedGym);

                        bw.flush();
                        bw.close();
                        socket.close();

                        finish();

                    } catch (Exception ex) {
                    }
                }
            });
            t.start();
        } catch (Exception ex) {

        }
    }

}