package com.zoma.map;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by noura_000 on 8/19/2017.
 */

public class NearByPlacesData extends AsyncTask<Object,String,String> {
    String googlePlacesData;
    GoogleMap mMap;
    String url;


    @Override
    protected String doInBackground(Object... objects) {
        Log.d("GetNearbyPlacesData", "doInBackground entered");//
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        DownloadURL durl = new DownloadURL();
        try {
            googlePlacesData = durl.readURL(url);
            Log.d("GooglePlacesReadTask", "doInBackground Exit");//
        } catch (MalformedURLException e) {
            Log.d("GooglePlacesReadTask", e.toString());//
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");//
        List<HashMap<String,String>> nearByPlacesList = null;
        DataParser  parser = new DataParser();
        nearByPlacesList = parser.parse(s);
        showNearByPlaces(nearByPlacesList);
        Log.d("GooglePlacesReadTask", "onPostExecute Exit");//
    }

    private void showNearByPlaces(List<HashMap<String, String>> nearByPlacesList) {
    for(int i = 0 ; i < nearByPlacesList.size() ; i++)
    {
        Log.d("onPostExecute","Entered into showing locations");//
        MarkerOptions markerOptions = new MarkerOptions();
        HashMap<String,String> googlePlace = nearByPlacesList.get(i);

        String placeName = googlePlace.get("place_name");
        String vicinity = googlePlace.get("vicinity");
        double lng = Double.parseDouble(googlePlace.get("lng"));
        double lat = Double.parseDouble(googlePlace.get("lat"));

        LatLng latlng = new LatLng(lat,lng);
        markerOptions.position(latlng);
        markerOptions.title(placeName + " " + vicinity);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
    }
}
