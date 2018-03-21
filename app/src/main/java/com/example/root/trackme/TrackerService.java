package com.example.root.trackme;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Service;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class TrackerService extends Service  {

        Timer timing;
        Handler helper;
        Intent batteryStatus;
        long scheduleTime = 1000;
        DBHelper db;
        double lastLat = -1;
        double lastLong = -1;
        double lastTime = -1;
        double speed = 0;
        final double speedLimit = 1000;
        int minBatteryLevel = 0;

        @Override
        public IBinder onBind(Intent intent)
        {
            return null;
        }

        @Override
        public void onCreate()
        {
            // TODO Auto-generated method stub
            super.onCreate();
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

            timing = new Timer();
            helper= new Handler(Looper.getMainLooper());
            db = new DBHelper(getApplicationContext());

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int myRate = preferences.getInt("rate", 1);
            minBatteryLevel = preferences.getInt("minBattery",20);
            scheduleTime = myRate * 1000;
            timing.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    callMethod();
                }

                private void callMethod() {
                    helper.post(new Runnable() {
                        public void run(){
                            GPSTracker gps = new GPSTracker(TrackerService.this);

                            if(gps.canGetLocation()){
                                double latitude = gps.getLatitude();
                                double longitude = gps.getLongitude();
                                doWithLocation(latitude, longitude);

                            }else{
                               // gps.showSettingsAlert();
                                addNotification("Location Disabled", "Stopped Tracking Your Position");

                                Intent serviceIntent = new Intent(getApplicationContext(), TrackerService.class);
                                stopService(serviceIntent);
                                onDestroy();

                            }
                        }
                    });
                }

            }, 0, scheduleTime);
        }

        @Override
        public void onDestroy()
        {
            timing.cancel();

            super.onDestroy();
        }
        private void doWithLocation(double latitude, double longitude){

            if(latitude != 0 && longitude != 0 ) {


                long time = System.currentTimeMillis();

                if (lastLat == -1 || lastLong == -1) {
                    speed = -1;
                } else {
                    float[] results = new float[1]; // meter
                    Location.distanceBetween(lastLat, lastLong, latitude, longitude, results);
                    speed = results[0] / ((time - lastTime) / 1000); // meter/seconds
                }

                if (speed < speedLimit) {
                    db.insertCoord(latitude + "", longitude + "", time + "", speed + "");
                    lastLat = latitude;
                    lastLong = longitude;
                    lastTime = time;
                }
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

                if (status < minBatteryLevel) {
                    addNotification("Low Battery", "Stopped Tracking Your Position");
                    Intent serviceIntent = new Intent(getApplicationContext(), TrackerService.class);
                    stopService(serviceIntent);
                    onDestroy();
                }
            }
        }
    private void addNotification(String title, String content) {
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.radar)
                        .setContentTitle(title)
                        .setContentText(content);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
