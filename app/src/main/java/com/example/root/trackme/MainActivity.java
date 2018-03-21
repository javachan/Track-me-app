package com.example.root.trackme;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    Button startBtn, stopBtn, settingsBtn, mapBtn, realtimeBtn, reportBtn;
    TextView statusTw, countTw, currentSpeedTw, currentCoordsTw;
    DBHelper db;
    GPSTracker gpt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(getApplicationContext());
        startBtn = (Button) findViewById(R.id.btnStart);
        stopBtn = (Button) findViewById(R.id.btnStop);
        settingsBtn = (Button) findViewById(R.id.btnSettings);
        mapBtn = (Button) findViewById(R.id.btnMap);
        reportBtn = (Button) findViewById(R.id.btnReport);
        realtimeBtn = (Button) findViewById(R.id.btnRealTime);
        statusTw = (TextView) findViewById(R.id.twStatus);
        countTw = (TextView) findViewById(R.id.twCount);
        currentSpeedTw = (TextView) findViewById(R.id.twCurrentSpeed);
        currentCoordsTw = (TextView) findViewById(R.id.twCurrentCoords);



        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyServiceRunning(TrackerService.class)){
                    Intent serviceIntent = new Intent(getApplicationContext(), TrackerService.class);
                    startService(serviceIntent);
                    setViews();
                }

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning(TrackerService.class)) {
                    Intent serviceIntent = new Intent(getApplicationContext(), TrackerService.class);
                    stopService(serviceIntent);
                    setViews();

                }
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ReportActivity.class);
                startActivity(i);
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                i.putExtra("mode", "normal");
                startActivity(i);
            }
        });

        realtimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                i.putExtra("mode", "realtime");
                startActivity(i);
            }
        });

        setViews();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(preferences.getBoolean("sw",false)) {


            final Handler helper;
            helper = new Handler(Looper.getMainLooper());

            Timer timing = new Timer();
            long scheduleTime = preferences.getInt("rate",5) * 1000;
            timing.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    callMethod();
                }

                private void callMethod() {
                    helper.post(new Runnable() {
                        public void run() {
                            if (!isMyServiceRunning(TrackerService.class)) {
                                startBtn.setVisibility(View.VISIBLE);
                                stopBtn.setVisibility(View.INVISIBLE);
                                statusTw.setText("Idle");
                            } else {
                                stopBtn.setVisibility(View.VISIBLE);
                                startBtn.setVisibility(View.INVISIBLE);
                                statusTw.setText("Running");
                            }
                            currentSpeedTw.setText(db.lastSpeed());
                            gpt = new GPSTracker(MainActivity.this);

                            if (gpt.canGetLocation() && gpt.getLocation() != null)
                                currentCoordsTw.setText("Lat: " + gpt.getLocation().getLatitude() + " Long: " + gpt.getLocation().getLongitude());
                            countTw.setText(db.getRowCount() + " ");

                        }
                    });
                }

            }, 0, scheduleTime);
        }
    }
    private void setViews(){
        if (!isMyServiceRunning(TrackerService.class)) {
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.INVISIBLE);
            statusTw.setText("Idle");
        } else {
            stopBtn.setVisibility(View.VISIBLE);
            startBtn.setVisibility(View.INVISIBLE);
            statusTw.setText("Running");
        }
        countTw.setText(db.getRowCount() + " ");

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}