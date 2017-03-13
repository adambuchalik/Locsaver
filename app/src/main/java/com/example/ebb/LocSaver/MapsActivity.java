package com.example.ebb.LocSaver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import static com.example.ebb.LocSaver.MainActivity.places;



// add GoogleMap.OnMapLongClickListener and its methods for marking location with lonClick
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;



    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your location");

            }
        }
    }

    //method to process location
    public void centerMapOnLocation(Location location, String title){

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        //to clear marker on the map
        mMap.clear();

        // if location wasn't select show marker
        if(title != "Your location") {
            //add new marker based on userLocation and name (from a list)
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        }
        //last parameter to st map zoom
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        Toast.makeText(this, "To add location to your list\nhold your finger on the map", Toast.LENGTH_LONG).show();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();

        //gets data from MainActivity and if a list is empty...
        if(intent.getIntExtra("placeNumber", 0)==0){
            //zoom in on user's location

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    centerMapOnLocation(location, "Your location");

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };


            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your location");
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }

        } else{

            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).longitude);

            centerMapOnLocation(placeLocation, places.get(intent.getIntExtra("placeNumber", 0)));

        }

    }

    //method auto-created when added onMapLongClick to MapsActivity class
    @Override
    public void onMapLongClick(LatLng latLng) {



        //Adding geocoder to get position
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "";

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if(listAddresses != null && listAddresses.size() > 0){

                if(listAddresses.get(0).getThoroughfare() != null){

                    if (listAddresses.get(0).getSubThoroughfare() != null){

                        address += listAddresses.get(0).getSubThoroughfare() + " ";
                    }

                    address += listAddresses.get(0).getThoroughfare();


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If there is no address put time and date on label
        if (address == ""){

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
            address = sdf.format(new Date());


        }


        //add marker on the map with address
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        //adding map address to list of places
        places.add(address);
        //adding location to list of locations
        MainActivity.locations.add(latLng);
        //updating listview in MainActivity
        MainActivity.arrayAdapter.notifyDataSetChanged();


        // S E R I A L I Z A T I O N //////////////////////
        /* With custom class SerializeObject created
         * Must be deserialize in MainActivity  */

        // to start serializing SharedPreferences must be created
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.ebb.LocSaver", Context.MODE_PRIVATE);


        try {

            // to save locations, array must be created for Longitudes and latitudes separatelly
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();

            //add latLng to both arraylists
            for (LatLng coordinates: MainActivity.locations){

                latitudes.add(Double.toString(coordinates.latitude));
                longitudes.add(Double.toString(coordinates.longitude));
            }


            // save places and coordinates
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();
            Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show();




        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
