package com.example.root.trackme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    DBHelper db;
    String mode;
    int rcShow;
    private Marker userMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        db = new DBHelper(getApplicationContext());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        rcShow = preferences.getInt("recordShow", 1);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        GPSTracker gps = new GPSTracker(MapActivity.this);

        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            LatLng sydney = new LatLng(latitude, longitude);

            //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker near you"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(20.0f));
        }else{
            // gps.showSettingsAlert();
            onDestroy();
        }
        putSchoolAndHomeIcons();
        if(mode.compareTo("normal") == 0){
            generateNormalMap();
        } else if(mode.compareTo("freq") == 0){
            generateFreqMap();
        } else if(mode.compareTo("realtime") == 0){
            realTime();
        }



    }
    private void generateNormalMap(){
        ArrayList<HashMap<String,String>> coords;
        int size = db.getRowCount();

        double places[][] = db.getSorted((int)(size*0.005));

        coords = db.coordsWithLimit(rcShow+"");
        PolylineOptions rectOptions = new PolylineOptions();
        double maxSpeed = 100;
        for(int i = 0; i < coords.size(); i++){

            double speed = Double.parseDouble(coords.get(i).get("speed"));
            LatLng ll = new LatLng(Double.parseDouble(coords.get(i).get("lat")),Double.parseDouble(coords.get(i).get("long")));
            rectOptions.add(ll).color(Color.rgb((int)(speed/maxSpeed)*256, (int)(speed/maxSpeed)*256, (int)(speed/maxSpeed)*256));

            if((i+1)%100 == 0){
                mMap.addPolyline(rectOptions);

                rectOptions = new PolylineOptions() ; // Closes the polyline.
            }
           // mMap.addMarker(new MarkerOptions().position(ll).title(i+""));
        }
        for(int i = 0; i < places.length; i++){
            MarkerOptions marker = new MarkerOptions().position(new LatLng(places[i][0], places[i][1])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            marker.title(places[i][2]+"%");
            if (i%5 == 0)
                marker.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            if (i%5 == 1)
                marker.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            if (i%5 == 2)
                marker.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            if (i%5 == 3)
                marker.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            if (i%5 == 4)
                marker.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mMap.addMarker(marker);
        }
        mMap.addPolyline(rectOptions);
}
    private void generateFreqMap(){
        Intent intent = getIntent();

        long start = intent.getLongExtra("datemin",0);
        long end = intent.getLongExtra("datemax",System.currentTimeMillis());

        double coords[][] = db.getTopFive(start, end);
        int crdLen = coords.length;
        for(int i = 0; i < crdLen; i++){
            LatLng ll = new LatLng(coords[i][0],coords[i][1]);

            mMap.addMarker(new MarkerOptions().position(ll).title(i+""));

        }
    }
    private void realTime(){
        final Handler helper;
        helper= new Handler(Looper.getMainLooper());

        Timer timing = new Timer();
        long scheduleTime = 3 * 1000;
        userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)));

        timing.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                callMethod();
            }

            private void callMethod() {
                helper.post(new Runnable() {
                    public void run(){

                        userMarker.remove();
                        GPSTracker gps = new GPSTracker(MapActivity.this);

                        if(gps.canGetLocation()){
                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();
                            LatLng ll = new LatLng(latitude,longitude);
                            //mMap.clear();
                            userMarker = mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.foot)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));

                        }else{
                            Intent serviceIntent = new Intent(getApplicationContext(), TrackerService.class);
                            stopService(serviceIntent);
                            onDestroy();

                        }

                    }
                });
            }

        }, 0,  scheduleTime);
    }
    private void putSchoolAndHomeIcons(){
        double coords[][] = db.getTopFive(0,System.currentTimeMillis());
        LatLng ll = new LatLng(coords[0][0],coords[0][1]);
        mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.home)));
        ll = new LatLng(coords[1][0],coords[1][1]);
        mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.school)));

    }
}
