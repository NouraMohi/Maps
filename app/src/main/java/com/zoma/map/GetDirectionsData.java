package com.zoma.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by noura_000 on 5/10/2018.
 */

public class GetDirectionsData extends AsyncTask<Object,String,String> {
    String googleDirectionsData;
    GoogleMap mMap;
    String url;
    String duration,distance;
    Polyline polyLine;
    List<LatLng> points;
    PolylineOptions options;
    List<Polyline> polyLines = new ArrayList<Polyline>();

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        DownloadURL durl = new DownloadURL();
        try {
            googleDirectionsData = durl.readURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {


        String[] directionsList;
        DataParser parser = new DataParser();
        directionsList = parser.parseDirections(s);
        this.setDistance(parser.getDistance());
        this.setDuration(parser.getDuration());
        displayDirection(directionsList);

        /*distance = directionsList.get("distance");
        duration = directionsList.get("duration");

        Log.d("GoogleDirectionReadTask", "onPostExecute Exit");//

        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.draggable(true);
        markerOptions.title("Duration="+duration);
        markerOptions.snippet(distance + " " + duration);

        mMap.addMarker(markerOptions);*/
    }

    public void removePolyLine()
    {
        this.cancel(true);
            for (int i = 0; i < polyLines.size(); i++) {
                polyLines.get(i).remove();
            }
            polyLines.clear();
            polyLines = new ArrayList<Polyline>();
    }

    public void displayDirection(String[] directionsList) {
        int count = directionsList.length;
        for(int i = 0 ; i < count ; i++)
        {
            options = new PolylineOptions();
            options.color(Color.CYAN);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));
            polyLine = mMap.addPolyline(options);
            points = polyLine.getPoints();
            polyLines.add(polyLine);
        }
    }

    public void setDistance(String d)
    {
        distance = d;
    }

    public void setDuration(String d)
    {
        duration = d;
    }

    public String getDistance()
    {
        return distance;
    }

    public String getDuration()
    {
        return duration;
    }

}
