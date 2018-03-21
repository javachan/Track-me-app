package com.example.root.trackme;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    Button save, hms, btnExport;
    ProgressBar pbExport;
    Integer count = 0;
    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = new DBHelper(getApplicationContext());

        final EditText rate = (EditText) findViewById(R.id.etRate);
        final EditText recordShow = (EditText) findViewById(R.id.etRecordShow);
        final EditText minBatteryLevelEt = (EditText) findViewById(R.id.etMinBatLvl);
        final Switch swReal = (Switch) findViewById(R.id.swHomeRt);
        btnExport = (Button) findViewById(R.id.btnExport);
        save = (Button) findViewById(R.id.btnSet);
        hms = (Button) findViewById(R.id.btnHomeSettings);
        pbExport = (ProgressBar) findViewById(R.id.pbExport);
        pbExport.setMax(db.getRowCount());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int myRate = preferences.getInt("rate", 1);
        int minBatteryLevel = preferences.getInt("minBattery",20);
        int rcShow = preferences.getInt("recordShow", 1);

        recordShow.setText(rcShow+"");
        rate.setText(myRate+"");
        minBatteryLevelEt.setText(minBatteryLevel+"");
        swReal.setChecked(preferences.getBoolean("sw", false));


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("rate", Integer.parseInt(String.valueOf(rate.getText())));
                editor.putInt("recordShow", Integer.parseInt(String.valueOf(recordShow.getText())));
                editor.putInt("minBattery", Integer.parseInt(String.valueOf(minBatteryLevelEt.getText())));
                editor.commit();

                if(isMyServiceRunning(TrackerService.class)){
                    Intent serviceIntent = new Intent(getApplicationContext(), TrackerService.class);
                    stopService(serviceIntent);
                    startService(serviceIntent);
                }

            }
        });

        swReal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("sw", swReal.isChecked());
                editor.commit();

            }
        });

        hms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbExport.setVisibility(View.VISIBLE);
                pbExport.setProgress(0);
                //   new MyTask().execute(db.getRowCount());
                new MyTask().execute(db.getRowCount());
            }
        });
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


    class MyTask extends AsyncTask<Integer, Integer, String> {
        FileWriter writer;
        ArrayList<HashMap<String,String>> coords;
        String datemillis;
        Calendar calendar;
        int m,y,d;
        @Override
        protected String doInBackground(Integer... params) {
            for (; count < params[0]; count++) {

                try {
                    datemillis = coords.get(count).get("date");
                    calendar.setTimeInMillis(Long.parseLong(datemillis));

                    y = calendar.get(Calendar.YEAR);
                    m = calendar.get(Calendar.MONTH);
                    d = calendar.get(Calendar.DAY_OF_MONTH);

                    writer.append("<tr><td>"+coords.get(count).get("lat")+"</td><td>"+coords.get(count).get("long")+"</td><td>"+coords.get(count).get("speed")+"</td><td>"+d+"/"+m+"/"+y+"</td></tr>");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                publishProgress(count);
            }
            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {
            pbExport.setVisibility(View.GONE);
            try {
                writer.append("</table></html>");

                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();

        }
        @Override
        protected void onPreExecute() {
            try {
                File root = new File(Environment.getExternalStorageDirectory(), "Notes");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File gpxfile = new File(root, "coordinates.html");
                writer = new FileWriter(gpxfile);

            } catch (IOException e) {
                e.printStackTrace();
            }
            coords = db.coords();
            try {
                writer.append("<html><table cellpadding=5 border=2 >");

                writer.append("<tr><td>Latitude</td><td>Longtitude</td><td>Speed</td><td>Date</td></tr>");
            } catch (IOException e) {
                e.printStackTrace();
            }
            calendar = Calendar.getInstance();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            pbExport.setProgress(values[0]);
        }
    }
}
