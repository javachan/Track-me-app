package com.example.root.trackme;

/**
 * Created by root on 27.05.2017.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "map_db";//database adı

    private static final String TABLE_NAME = "coords";
    private static String COR_SPEED = "speed";
    private static String COR_LONG = "long";
    private static String COR_LAT = "lat";
    private static String COR_ID = "id";
    private static String COR_DATE = "date";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COR_LAT + " DOUBLE,"
                + COR_LONG + " DOUBLE,"
                + COR_SPEED + " DOUBLE,"
                + COR_DATE + " LONG"  + ")";
        db.execSQL(CREATE_TABLE);
    }

    public void deleteCoord(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COR_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public void insertCoord(String lat, String lon, String date, String speed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COR_LAT, lat);
        values.put(COR_LONG, lon);
        values.put(COR_SPEED, speed);
        values.put(COR_DATE, date);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public HashMap<String, String> getDetails(int id){
        HashMap<String,String> coord = new HashMap<String,String>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME+ " WHERE id="+id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            coord.put(COR_LAT, cursor.getString(1));
            coord.put(COR_LONG, cursor.getString(2));
            coord.put(COR_DATE, cursor.getString(3));
            coord.put(COR_SPEED, cursor.getString(4));
        }
        cursor.close();
        db.close();
        return coord;
    }

    public HashMap<String, String> getNearest(double lat){
        HashMap<String,String> coord = new HashMap<String,String>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME+ " ORDER BY ABS( "+COR_LAT+" - "+lat+" ) ASC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            coord.put(COR_LAT, cursor.getString(1));
            coord.put(COR_LONG, cursor.getString(2));
            coord.put(COR_DATE, cursor.getString(3));
            coord.put(COR_SPEED, cursor.getString(4));
        }
        cursor.close();
        db.close();
        return coord;
    }
    public long getMinDate(){
        String selectQuery = "SELECT min("+COR_DATE+") as mindate FROM " + TABLE_NAME + "";
        String min ="";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            min = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return Long.parseLong(min);
    }
    public double[] getMinMaxLat(){
        String selectQuery = "SELECT min("+COR_LAT+") as min_lat, max("+COR_LAT+") as max_lat FROM " + TABLE_NAME + "";
        String min ="",max = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            min = cursor.getString(0);
            max = cursor.getString(1);
        }
        cursor.close();
        db.close();
        return new double[]{ Double.parseDouble(min), Double.parseDouble(max)};
    }

    public double[] getMinMaxLong(){
        String selectQuery = "SELECT min("+COR_LONG+") as min_long, max("+COR_LONG+") as max_long FROM " + TABLE_NAME + "";
        String min ="",max = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            min = cursor.getString(0);
            max = cursor.getString(1);
        }
        cursor.close();
        db.close();
        return new double[]{ Double.parseDouble(min), Double.parseDouble(max)};
    }

    public double[] getAvgMaxSpeed(){
        String selectQuery = "SELECT max(speed),avg(speed) FROM " + TABLE_NAME + " WHERE speed <> -1 AND speed < 1000 AND speed <> 0";
        String max = "", avg="";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            max = cursor.getString(0);
            avg = cursor.getString(1);
        }
        cursor.close();
        db.close();
        return new double[]{ Double.parseDouble(avg), Double.parseDouble(max)};
    }



    public String lastSpeed(){
        HashMap<String,String> coord = new HashMap<String,String>();
        String selectQuery = "SELECT speed FROM " + TABLE_NAME + " ORDER BY "+COR_ID+" DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            coord.put(COR_SPEED, cursor.getString(0));
        }
        cursor.close();
        db.close();
        return coord.get(COR_SPEED);
    }

    public  ArrayList<HashMap<String, String>> coords(){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<HashMap<String, String>> coordList= new ArrayList<HashMap<String, String>>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for(int i=0; i<cursor.getColumnCount();i++)
                {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }

                coordList.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        return coordList;
    }


    public  ArrayList<HashMap<String, String>> coordsWithLimit(String limit){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME+" ORDER BY "+COR_ID+" DESC LIMIT "+limit;
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<HashMap<String, String>> coordList= new ArrayList<HashMap<String, String>>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for(int i=0; i<cursor.getColumnCount();i++)
                {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }

                coordList.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        return coordList;
    }
    public  ArrayList<HashMap<String, String>> coords(String where){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME+" WHERE "+where;
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<HashMap<String, String>> coordList= new ArrayList<HashMap<String, String>>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for(int i=0; i<cursor.getColumnCount();i++)
                {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }

                coordList.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        return coordList;
    }
    public double[][] getTopFive(long start, long end){
        String key;
        Map<String, Integer> aMap = new HashMap<String, Integer>();

        ArrayList<HashMap<String,String>> coords;
        coords = coords(COR_DATE+">"+start+" AND "+COR_DATE+"<"+end);
        for(int i = 0; i < coords.size(); i++) {
            key = convertToThree(coords.get(i).get(COR_LAT));
            key += "," +convertToThree(coords.get(i).get(COR_LONG));

            if(aMap.get(key) != null){
                aMap.put(key, aMap.get(key)+1);
            } else {
                aMap.put(key, Integer.valueOf(1));
            }

        }
        aMap = sortByValue(aMap);
        printMap(aMap);
        int i = 0;
        int dataSize = Math.min(5, aMap.size());
        double latlons[][] = new double[dataSize][2];
        for (Map.Entry<String, Integer> entry : aMap.entrySet()) {
            if(i < dataSize){
                String string = entry.getKey();
                String[] parts = string.split(",");
                String part1 = parts[0];
                String part2 = parts[1];
                double lat = Double.parseDouble(part1);
                double lon = Double.parseDouble(part2);
                Map<String,String> nearest = getNearest(lat);
                latlons[i][0] = Double.parseDouble(nearest.get(COR_LAT));
                latlons[i][1] = Double.parseDouble(nearest.get(COR_LONG));
            }
            i++;

        }
        return latlons;
    }





    public double[][] getSorted(int min){
        String key;
        Map<String, Integer> aMap = new HashMap<String, Integer>();

        ArrayList<HashMap<String,String>> coords;
        coords = coords();
        for(int i = 0; i < coords.size(); i++) {
            key = convertToThree(coords.get(i).get(COR_LAT));
            key += "," +convertToThree(coords.get(i).get(COR_LONG));

            if(aMap.get(key) != null){
                aMap.put(key, aMap.get(key)+1);
            } else {
                aMap.put(key, Integer.valueOf(1));
            }

        }
        aMap = sortByValue(aMap);
        printMap(aMap);
        int i = 0;
        int dataSize = aMap.size();
        double latlons[][] = new double[dataSize][3];
        for (Map.Entry<String, Integer> entry : aMap.entrySet()) {
            if(entry.getValue() > min){
                String string = entry.getKey();
                String[] parts = string.split(",");
                String part1 = parts[0];
                String part2 = parts[1];
                double lat = Double.parseDouble(part1);
                double lon = Double.parseDouble(part2);
                Map<String,String> nearest = getNearest(lat);
                latlons[i][0] = Double.parseDouble(nearest.get(COR_LAT));
                latlons[i][1] = Double.parseDouble(nearest.get(COR_LONG));
                latlons[i][2] = (entry.getValue()*100)/getRowCount();
            }
            i++;

        }
        return latlons;
    }

    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        return rowCount;
    }

    public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }


        return sortedMap;
    }
    public static <K, V> void printMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            Log.d("a","Key : " + entry.getKey()+ " Value : " + entry.getValue());
        }
    }
    public String convertToThree(String crd){
        double db = Double.parseDouble(crd);
        db = (int)(db*1000);
        db /= 1000;
        return db+"";
    }
}