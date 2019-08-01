package com.example.hmt22.pokemongointerface;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

import static com.example.hmt22.pokemongointerface.MainActivity.locationManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class YourRaidsTabFragment extends Fragment implements LocationListener{

    String[] raidInfo;
    public static String[] raidIDs;
    public static String[] startTimes;
    public static String[] raidLevels;
    public static String[] numRaiders;
    public static String[] gyms;
    public static String[] pokemon;
    public static String[] meetings;
    public static ListView listView;
    public static CustomListAdapter listAdapter;

    public YourRaidsTabFragment() {
        // Required empty public constructor
    }

    /*
    public void addToYourRaids(String name, String des, Integer raiders, Integer img){
        raids.add(name);
        raidDescriptions.add(des);
        numRaiders.add(raiders);
        images.add(img);

        return;
    }
    */

    @Override
    public void onResume() {
        super.onResume();
        View v = getActivity().findViewById(R.id.your_raid_layout);
        refresh(v);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        String[] pr = raids.toArray(new String[raids.size()]);
        String[] pd = raidDescriptions.toArray(new String[raidDescriptions.size()]);
        Integer[] pm = numRaiders.toArray(new Integer[numRaiders.size()]);
        Integer[] pi = images.toArray(new Integer[images.size()]);


        CustomListAdapter listAdapter = new CustomListAdapter(this.getActivity(),pr, pd, pm, pi);
        listView = (ListView) getView().findViewById(R.id.list);
        listView.setAdapter(listAdapter);
        */

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_your_raids_tab, container, false);


    }

    static int numRaids;

    private void refresh(View view) {
        final View v = view;
        //Ask for raids
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                try {
                    Log.d("RAIDSDD", "Creating Socket");
                    Socket socket = new Socket(MainActivity.host, MainActivity.port);
                    Boolean b = socket.isConnected();

                    //if statement for connection

                    Log.d("RAIDSDD", b.toString());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    writer.write("MY_RAIDS," + MainActivity.username + "\n");
                    writer.flush();

                    Log.d("RAIDSD", "Got here");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String openBool = (socket.isConnected()) ? "true" : "false";
                    Log.d("RAIDSD", openBool);
                    String message = reader.readLine();
                    Log.d("RAIDSD", message);
                    final int numRaids = Integer.parseInt(message);
                    setNumRaids(numRaids);
                    raidInfo = new String[numRaids];

                    int count = 0;
                    while (!(message = reader.readLine()).equals("END")) {
                        raidInfo[count] = message;
                        count++;
                    }
                    Log.d("RAIDSD", "DONE");
                    writer.close();
                    reader.close();
                    socket.close();

                    if(YourRaidsTabFragment.numRaids > 0) {
                        raidIDs = new String[YourRaidsTabFragment.numRaids];
                        startTimes = new String[YourRaidsTabFragment.numRaids];
                        numRaiders = new String[YourRaidsTabFragment.numRaids];
                        raidLevels = new String[YourRaidsTabFragment.numRaids];
                        gyms = new String[YourRaidsTabFragment.numRaids];
                        pokemon = new String[YourRaidsTabFragment.numRaids];
                        meetings = new String[YourRaidsTabFragment.numRaids];

                        for (int i = 0; i < YourRaidsTabFragment.numRaids; i++) {
                            String[] r = raidInfo[i].split(",");
                            raidIDs[i] = r[0];
                            startTimes[i] = r[1];
                            numRaiders[i] = r[6];
                            raidLevels[i] = r[3];
                            gyms[i] = r[5];
                            pokemon[i] = r[4];
                            meetings[i] = r[7];
                        }
                    } else {
                        raidIDs = new String[1];
                        startTimes = new String[1];
                        numRaiders = new String[1];
                        raidLevels = new String[1];
                        gyms = new String[1];
                        pokemon = new String[1];
                        meetings = new String[1];


                        raidIDs[0] = ("");
                        startTimes[0] = ("Find a raid in the All Raids tab");
                        numRaiders[0] = "0";
                        raidLevels[0] = "0";
                        gyms[0] = "No Raids Available";
                        pokemon[0] = "";
                        meetings[0] = "FALSE";

                        YourRaidsTabFragment.numRaids = 1;
                    }

                    Looper.prepare();
                    final Location playerLoc = getPlayerLocation();
                    final Location[] gymLocations = new Location[gyms.length];

                    for (int i = 0; i < gyms.length; i++) {
                        if (!gyms[i].equals("No Raids Available")) {
                            socket = new Socket(MainActivity.host, MainActivity.port);

                            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            writer.write("SELECT,GYM," + "name = \"" + gyms[i] + "\"\n");
                            writer.flush();

                            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String result = reader.readLine();
                            Log.d("DEBUGGING", result);
                            reader.close();

                            //Extract gym location information
                            String[] gymInfo = result.split(",");

                            final Location gymLocation = new Location("PokeRaids");
                            gymLocation.setLatitude(Double.parseDouble(gymInfo[1]));
                            gymLocation.setLongitude(Double.parseDouble(gymInfo[2]));

                            gymLocations[i] = gymLocation;

                            socket.close();
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        String[] pr = raidIDs;
                        String[] pd = startTimes;
                        Integer[] pl = new Integer[YourRaidsTabFragment.numRaids];
                        Integer[] pm = new Integer[YourRaidsTabFragment.numRaids];
                        String[] pg = new String[YourRaidsTabFragment.numRaids];
                        String[] pp = pokemon;
                        String[] distances = new String[pg.length];
                        for (int i = 0; i < YourRaidsTabFragment.numRaids; i++) {
                            pl[i] = Integer.parseInt(raidLevels[i]);
                            Log.d("RAIDSDC", "here is my string");
                            pm[i] = Integer.parseInt(numRaiders[i]);
                            pg[i] = gyms[i];
                            if (!gyms[i].equals("No Raids Available")) distances[i] = Integer.toString(Math.round(playerLoc.distanceTo(gymLocations[i])));
                        }

                        listAdapter = new CustomListAdapter(getActivity(),pr, pd, pm, pg, pl, pp, meetings, distances);
                        listView = (ListView) getView().findViewById(R.id.your_list);
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

    private void setNumRaids(int i) {
        numRaids = i;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("DEBUGGING", "Location changed");
            locationManager.removeUpdates(this);
        }
    }

    public Location getPlayerLocation() {
        final LocationListener locListen = this;
        try {
            if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Get player location
                final Location lastLocation;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListen);
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return lastLocation;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}
