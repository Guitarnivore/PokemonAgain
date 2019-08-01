package com.example.hmt22.pokemongointerface;

//
// https://appsandbiscuits.com/listview-tutorial-android-12-ccef4ead27cc
//

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] gymArray; //populate first 5 with level indicator stars? Add 5 to position when used
    private final String[] nameArray;
    private final String[] startTimeArray;
    private final Integer[] members;
    private final Integer[] levels;
    private final String[] pokemon;
    private final String[] meeting;
    private final String[] distances;

    public CustomListAdapter(Activity context, String[] nameArrayParam,
                             String[] infoArrayParam, Integer[] membersParam,
                             String[] gymNameArrayParam,
                             Integer[] levelsArrayparam,
                             String[] pokeArrayParam,
                             String[] meetingParam,
                             String[] distancesParam){
        super(context, R.layout.list_item, nameArrayParam);

        this.context = context;
        this.gymArray = gymNameArrayParam;
        this.nameArray = nameArrayParam;
        this.startTimeArray = infoArrayParam;
        this.members = membersParam;
        this.levels = levelsArrayparam;
        this.pokemon = pokeArrayParam;
        this.meeting = meetingParam;
        this.distances = distancesParam;
        //hello

    }

    public View getView(final int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item,null, true);

        rowView.setTag(nameArray[position]);

        TextView raidTitle = (TextView)rowView.findViewById(R.id.listTitle);
        TextView startTime = (TextView)rowView.findViewById(R.id.listDescription);
        TextView membersIn = (TextView)rowView.findViewById(R.id.membersIn);
        TextView distance = (TextView) rowView.findViewById(R.id.distance);
        RatingBar level = (RatingBar)rowView.findViewById(R.id.RaidLevel);

        raidTitle.setText(gymArray[position] + " - " + pokemon[position]);
        startTime.setText(startTimeArray[position]);
        membersIn.setText(members[position] + " Raiders");
        level.setRating(levels[position]);
        if (!gymArray[position].equals("No Raids Available")) {
            distance.setText(distances[position] + "m from you");
        }
        else {
            distance.setText("");
            level.setVisibility(View.INVISIBLE);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewRaidActivity.class);
                String[] raidInfo = new String[] {  nameArray[position],
                                                    startTimeArray[position],
                                                    Integer.toString(levels[position]),
                                                    gymArray[position],
                                                    Integer.toString(members[position]),
                                                    pokemon[position],
                                                    meeting[position]};
                intent.putExtra("RaidInfo", raidInfo);
                getContext().startActivity(intent);
            }
        });

        return rowView;

    }
}
