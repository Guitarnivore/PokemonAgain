package com.example.hmt22.pokemongointerface;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;

public class ViewRaidActivity extends AppCompatActivity {

    public static String[] raidInfo;
    int numMeetings;
    String joinedMeeting;
    String numDevices;
    String meetingTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_raid);

        /*
        nameArray[position],
        startTimeArray[position],
        Integer.toString(levels[position]),
        gymArray[position],
        Integer.toString(members[position]),
        pokemon[position]
        */

        raidInfo = getIntent().getExtras().getStringArray("RaidInfo");

        TextView tv = findViewById(R.id.Raid);
        TextView pt = findViewById(R.id.PokemonType);
        RatingBar l = findViewById(R.id.ratingBar);

        tv.setText(raidInfo[3]);
        pt.setText(raidInfo[5]);
        joinedMeeting = raidInfo[6];
        l.setRating(Integer.parseInt(raidInfo[2]));
        refresh(findViewById(R.id.meetingsList));
    }

    public void newMeeting(View v) {

        final Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        TimePickerDialog picker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
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
                final String ti = hour + ":" + minutes + ":00";
                joinedMeeting = ti;

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket s = new Socket(MainActivity.host, MainActivity.port);
                            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                            w.write("INSERT,MEETING," + ti + "," + MainActivity.username + "," + raidInfo[0]);
                            w.flush();
                            w.close();
                            s.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh(findViewById(R.id.meetingsList));
                            }
                        });
                    }
                });
                t.start();
            }
        }, hour, minutes, false);
        picker.show();
    }

    private void refresh(View view) {
        final View v = view;
        //Ask for raids
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                try {
                    Socket socket = new Socket(MainActivity.host, MainActivity.port);
                    Boolean b = socket.isConnected();

                    //if statement for connection

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    writer.write("TIMES," + raidInfo[0] + "\n");
                    writer.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = reader.readLine();
                    final int numMeetings = Integer.parseInt(message);
                    setNumMeetings(numMeetings);
                    final String[] MeetingsInfo = new String[numMeetings];

                    int count = 0;
                    while (!(message = reader.readLine()).equals("END")) {
                        Log.d("reading", "Meeting: " + message);
                        MeetingsInfo[count] = message;
                        count++;
                    }
                    writer.close();
                    reader.close();
                    socket.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        String[] numDevicesArray;
                        String[] timesArray;
                        if(numMeetings > 0){

                            numDevicesArray = new String[numMeetings];
                            timesArray = new String[numMeetings];

                            for (int i = 0; i < numMeetings; i++) {
                                String[] r = MeetingsInfo[i].split(",");
                                numDevicesArray[i] = r[0] + " Devices";
                                timesArray[i] = r[1];
                            }
                        } else {
                            numDevicesArray = new String[1];
                            timesArray = new String[1];

                            numDevicesArray[0] = "";
                            timesArray[0] = "No meetings available";
                        }


                        MeetingListAdapter listAdapter = new MeetingListAdapter(ViewRaidActivity.this,timesArray, numDevicesArray, joinedMeeting);
                        ListView listView = (ListView) findViewById(R.id.meetingsList);
                        listView.setAdapter(listAdapter);

                        }
                    });

                }
                catch (Exception ex) {
                    Log.d("RAIDSD", ex.getMessage());
                }
                }
            });
            t.start();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void setNumMeetings(int i) {
        numMeetings = i;
    }
}
