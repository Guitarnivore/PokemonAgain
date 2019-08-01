package com.example.hmt22.pokemongointerface;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MeetingListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] times; //populate first 5 with level indicator stars? Add 5 to position when used
    private final String[] devices;
    private final String joinedMeeting;

    public MeetingListAdapter(Activity context, String[] timesParam, String[] devicesParam, String jM){
        super(context, R.layout.meetings_list_item, timesParam);
        this.context = context;
        this.times = timesParam;
        this.devices = devicesParam;
        this.joinedMeeting = jM;
    }

    public View getView(final int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.meetings_list_item,null, true);

        //rowView.setTag(nameArray[position]);

        TextView time = (TextView)rowView.findViewById(R.id.MeetingTime);
        TextView device = (TextView)rowView.findViewById(R.id.Devices);

        time.setText(times[position]);
        device.setText(devices[position]);

        ToggleButton tb = rowView.findViewById(R.id.JoinButton);
        if (times[position].equals("No meetings available")) tb.setVisibility(View.INVISIBLE);
        else {
            if (joinedMeeting.equals(times[position])) {
                tb.setChecked(true);
            } else {
                tb.setChecked(false);
            }
            tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final boolean b = isChecked;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket s = new Socket(MainActivity.host, MainActivity.port);
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                                if (b) {
                                    writer.write("INSERT,MEETING," + times[position] + "," + MainActivity.username + "," + ViewRaidActivity.raidInfo[0]);
                                } else {
                                    writer.write("DELETE,MEETING," + MainActivity.username + "," + ViewRaidActivity.raidInfo[0]);
                                }

                                writer.flush();
                                writer.close();
                                s.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    t.start();
                }
            });
        }

        return rowView;

    }

}
