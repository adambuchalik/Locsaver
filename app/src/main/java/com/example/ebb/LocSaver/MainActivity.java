/* Author:  Adam Buchalik 40270304
   Subject: Mobile Application Development Coursework
   Title:   Loc Saver */

package com.example.ebb.LocSaver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.ebb.LocSaver.ObjectSerializer.*;

public class MainActivity extends AppCompatActivity {

    //static lists to access from other activities:
    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<LatLng> locations = new ArrayList<>();

    //to update listview itself ArrayAdapter must be updated from MapsActivity too
    static ArrayAdapter arrayAdapter;

    public void helpButton (View view){

        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Help")
                .setMessage("This app will save your locations on GoogleMaps.\n\n \u2022 To add location go to the map and hold your finger on the location" +
                        "\n\n\u2022 To edit or delete location hold your finger on the name of your location" +
                        "\n\n\u2022 To go to your saved location select it from the list" +
                        "\n\n\u2022 To navigate to your location tap on the marker on the map  and select navigation icon in the bottom right corner" )
                .setPositiveButton("Cancel", null)
                .show();
    }





    //shared preferences for serializing
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView locSaver = (ImageView)findViewById(R.id.locSaver);
        locSaver.animate().setStartDelay(2500).alpha(0f).scaleX(2f).scaleY(2f).setDuration(1000);

        TextView ad = (TextView) findViewById(R.id.ad);
        ad.animate().alpha(1).setDuration(1500);

        ad.setClickable(true);
        ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://adambuchalik.com"));
                startActivity(myWebLink);
            }
        });



        ListView listView = (ListView) findViewById(R.id.listView);


        //Deleting item on long click
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                String item = Long.toString(arrayAdapter.getItemId(position));
                Integer x = Integer.parseInt(item);


                // if item index is higher than 0 delete it
                if (x > 0) {

                    final int itemPosition = position;
                    final int itemIndex = x;

                    AlertDialog show = new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Edit item")
                            .setMessage("")
                            .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    showInputBox(places.get(itemPosition), itemPosition);
                                }
                            })
                            .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {


                                    arrayAdapter.remove(arrayAdapter.getItem(itemPosition));

                                    arrayAdapter.notifyDataSetChanged();

                                    //position is physical position in array starting from 1 while array index starts from 0
                                    locations.remove((itemIndex));
                                    //places.remove(1);

                                    arrayAdapter.notifyDataSetChanged();


                                    // S E R I A L I Z A T I O N when deleting item
                                    try {

                                        // to save locations, array must be created for Longitudes and latitudes separatelly
                                        ArrayList<String> latitudes = new ArrayList<>();
                                        ArrayList<String> longitudes = new ArrayList<>();

                                        //add latLng to both arraylists
                                        for (LatLng coordinates : MainActivity.locations) {

                                            latitudes.add(Double.toString(coordinates.latitude));
                                            longitudes.add(Double.toString(coordinates.longitude));
                                        }

                                        // save places and coordinates
                                        sharedPreferences.edit().putString("places", serialize(MainActivity.places)).apply();
                                        sharedPreferences.edit().putString("latitudes", serialize(latitudes)).apply();
                                        sharedPreferences.edit().putString("longitudes", serialize(longitudes)).apply();


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(MainActivity.this, "Location has been removed!", Toast.LENGTH_SHORT).show();


                                }
                            })
                            .setNeutralButton("Cancel", null)
                            .show();

                }


                return true;

            }

        });


        // D E - S E R I A L I Z A T I O N
        sharedPreferences = this.getSharedPreferences("com.example.ebb.LocSaver", Context.MODE_PRIVATE);

        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();

        //clearing up all variables that will be deserialized
        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {

            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>())));


        } catch (IOException e) {
            e.printStackTrace();
        }

        if (places.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0) {

            if (places.size() == latitudes.size() && latitudes.size() == longitudes.size()) {

                for (int i = 0; i < latitudes.size(); i++) {

                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));

                }


            }


        } else {

            places.add("Go to the map...");
            locations.add(new LatLng(0, 0));

        }

        // callback method to change arrayAdapter's cell properties
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, places) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.BLACK);
                tv.getBackground().setAlpha(80);
                tv.setShadowLayer(10, 5, 5, Color.BLACK);

                if (position == 0) {
                    tv.setTextColor(Color.BLACK);
                    tv.setBackgroundColor(Color.WHITE);
                    tv.getBackground().setAlpha(170);
                    tv.setShadowLayer(0, 2, 2, Color.BLACK);

                }



                // Generate ListView Item using TextView
                return view;
            }
        };

        listView.setAdapter(arrayAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("placeNumber", i);

                startActivity(intent);
            }

        });


    }
    //custom Dialog box for editing listview items
    public void showInputBox(String oldItem, final int index) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Input Box");
        dialog.setContentView(R.layout.input_box);
        TextView txtMessage = (TextView) dialog.findViewById(R.id.txtmessage);
        txtMessage.setText("Update item");
        txtMessage.setTextColor(Color.parseColor("#ffffff"));
        final EditText editText = (EditText) dialog.findViewById(R.id.txtinput);
        editText.setText(oldItem);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                places.set(index, editText.getText().toString());
                arrayAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}