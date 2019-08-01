package com.example.hmt22.pokemongointerface;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import static com.example.hmt22.pokemongointerface.MainActivity.locationManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllRaidsTabFragment extends Fragment implements LocationListener {

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

    public AllRaidsTabFragment() {
        // Required empty public constructor
    }


    @Override
    public void onResume() {
        super.onResume();
        View v = getActivity().findViewById(R.id.current_layout);
        refresh(v);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Pull existing raids from server, assign to arrays

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_raids_tab, container, false);
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

                        writer.write("RAID_REFRESH,"+ MainActivity.username + "\n");
                        writer.flush();

                        Log.d("RAIDSD", "Got here");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String openBool = (socket.isConnected()) ? "true" : "false";
                        Log.d("RAIDSD", openBool);
                        String message = reader.readLine();
                        Log.d("RAIDSD", message);
                        int numRaids = Integer.parseInt(message);
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

                        Log.d("RAIDSD", Integer.toString(AllRaidsTabFragment.numRaids));
                        if(AllRaidsTabFragment.numRaids > 0){
                            raidIDs = new String[AllRaidsTabFragment.numRaids];
                            startTimes = new String[AllRaidsTabFragment.numRaids];
                            numRaiders = new String[AllRaidsTabFragment.numRaids];
                            raidLevels = new String[AllRaidsTabFragment.numRaids];
                            gyms = new String[AllRaidsTabFragment.numRaids];
                            pokemon = new String[AllRaidsTabFragment.numRaids];
                            meetings = new String[AllRaidsTabFragment.numRaids];

                            for (int i = 0; i < AllRaidsTabFragment.numRaids; i++) {
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

                            raidIDs[0] = ("No Raids Available");
                            startTimes[0] = ("Select \"Add Raid\" to create a new raid");
                            numRaiders[0] = "0";
                            raidLevels[0] = "0";
                            gyms[0] = "";
                            pokemon[0] = "";
                            meetings[0] = "";
                        }

                        Looper.prepare();
                        final Location playerLoc = getPlayerLocation();
                        Log.d("DEBUGGING", playerLoc.toString());

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
                                Integer[] pl = new Integer[AllRaidsTabFragment.numRaids];
                                Integer[] pm = new Integer[AllRaidsTabFragment.numRaids];
                                String[] pg = new String[AllRaidsTabFragment.numRaids];
                                String[] pp = pokemon;
                                String[] distances = new String[pg.length];
                                for (int i = 0; i < AllRaidsTabFragment.numRaids; i++) {
                                    pl[i] = Integer.parseInt(raidLevels[i]);
                                    Log.d("RAIDSDC", "raidlevel " + Integer.toString(pl[i]));
                                    pm[i] = Integer.parseInt(numRaiders[i]);
                                    pg[i] = gyms[i];
                                    distances[i] = Integer.toString(Math.round(playerLoc.distanceTo(gymLocations[i])));
                                }

                                listAdapter = new CustomListAdapter(getActivity(),pr, pd, pm, pg, pl, pp, meetings, distances);
                                listView = (ListView) getView().findViewById(R.id.list);
                                listView.setAdapter(listAdapter);

                                Button btn = (Button) v.findViewById(R.id.AddRaidButton);

                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(getActivity(), AddRaidActivity.class);
                                        startActivity(intent);
                                    }
                                });
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
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
