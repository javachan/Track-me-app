package com.example.root.trackme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class ReportActivity extends AppCompatActivity {
    Button btnFreqPlaces;
    TextView MinMaxLongTw,MinMaxLatTw,AvgMaxTw;
    DatePicker StartDateDp, EndDateDp;
    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = new DBHelper(getApplicationContext());

        double[] lats = db.getMinMaxLat();
        String minLat = lats[0]+"";
        String maxLat = lats[1]+"";

        double[] lngs = db.getMinMaxLong();
        String minLong = lngs[0]+"";
        String maxLong = lngs[1]+"";

        double[] spd = db.getAvgMaxSpeed();
        String avgSpeed = spd[0]+"";
        String maxSpeed = spd[1]+"";

        MinMaxLatTw= (TextView) findViewById(R.id.twMinMaxLat);
        MinMaxLongTw= (TextView) findViewById(R.id.twMinMaxLong);
        AvgMaxTw= (TextView) findViewById(R.id.twAvgMaxSpeed);

        StartDateDp= (DatePicker) findViewById(R.id.dpStartDate);
        EndDateDp= (DatePicker) findViewById(R.id.dpEndDate);

        btnFreqPlaces = (Button) findViewById(R.id.btnFreqPlaces);

        MinMaxLatTw.setText("Min: "+minLat+" Max: "+maxLat);
        MinMaxLongTw.setText("Min: "+minLong+" Max: "+maxLong);
        AvgMaxTw.setText("Avg: "+avgSpeed+" Max: "+maxSpeed);


        btnFreqPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long start = datePickerMillis(StartDateDp);
                long end = datePickerMillis(EndDateDp);
                long now = System.currentTimeMillis();
                if(start < end){
                    if(start < now){
                        Intent i = new Intent(ReportActivity.this, MapActivity.class);
                        i.putExtra("mode","freq");
                        i.putExtra("datemin",start);
                        i.putExtra("datemax",end);
                        startActivity(i);
                    } else  Toast.makeText(getApplicationContext(), "Starting date must be before now", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(getApplicationContext(), "Starting date must be before end date", Toast.LENGTH_SHORT).show();
            }
        });

        long minDate = db.getMinDate();
        StartDateDp.setMinDate(minDate);
        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(minDate);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        StartDateDp.updateDate(mYear, mMonth, mDay);

    }
    public long datePickerMillis(DatePicker dp){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
        cal.set(Calendar.MONTH, dp.getMonth());
        cal.set(Calendar.YEAR, dp.getYear());
        return cal.getTimeInMillis();
    }
}
