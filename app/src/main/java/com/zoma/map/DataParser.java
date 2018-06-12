package com.zoma.map;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by noura_000 on 8/19/2017.
 */

public class DataParser {

    String distance = "",duration= "";

    private HashMap<String, String> getDirection(JSONArray googleDirectionJson) {
        HashMap<String,String> googleDirectionMap = new HashMap();
        try
        {
            distance = googleDirectionJson.getJSONObject(0).getJSONObject("distance").getString("text");
            duration = googleDirectionJson.getJSONObject(0).getJSONObject("duration").getString("text");
            googleDirectionMap.put("distance",distance);
            googleDirectionMap.put("duration",duration);
            this.setDistance(distance);
            this.setDuration(duration);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.d("Json Response", googleDirectionMap.toString());
        return googleDirectionMap;
    }

    private HashMap<String,String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String,String> googlePlaceMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String longitude = "";
        String latitude = "";
        String reference = "";

            try {
                if(!googlePlaceJson.isNull("name")) {
                    placeName = googlePlaceJson.getString("name");
                }
                if(!googlePlaceJson.isNull("vicinity"))
                {
                    vicinity = googlePlaceJson.getString("vicinity");
                }
                latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = googlePlaceJson.getString("reference");

                googlePlaceMap.put("place_name" , placeName);
                googlePlaceMap.put("vicinity" , vicinity);
                googlePlaceMap.put("lat" , latitude);
                googlePlaceMap.put("lng" , longitude);
                googlePlaceMap.put("reference" , reference);
                Log.d("getPlace", "Putting Places");//
            }
            catch (JSONException e) {
                Log.d("getPlace", "Error");//
                e.printStackTrace();

        }
        return googlePlaceMap;
    }

    private List<HashMap<String,String>> getPlaces(JSONArray jsonArr) {
        int count = jsonArr.length();
        List<HashMap<String,String>> placesList = new ArrayList<>();
        HashMap<String,String> placesMap = null;
        Log.d("Places", "getPlaces");//
        for(int i = 0 ; i < count ; i++)
        {
            try
            {
                placesMap = getPlace((JSONObject) jsonArr.get(i));
                placesList.add(placesMap);
                Log.d("Places", "Adding places");//
            }
            catch (JSONException e)
            {
                Log.d("Places", "Error in Adding places");//
                e.printStackTrace();
            }
        }
        return placesList;
    }

    public List<HashMap<String,String>> parse(String jsonData) {
        JSONArray jsonArr = null;
        JSONObject jsonObj ;
        try {
            Log.d("Places", "parse");//
            jsonObj = new JSONObject(jsonData);
            jsonArr = jsonObj.getJSONArray("results");
            getDirection(jsonArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArr);
    }

    public String[] parseDirections(String jsonData) {
        JSONArray jsonArr = null;
        JSONObject jsonObj ;
        try
        {
            Log.d("Directions", "parse");//
            jsonObj = new JSONObject((String)jsonData);
            jsonArr = jsonObj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //return getDirection(jsonArr);
        return getPaths(jsonArr);
    }

    public String[] getPaths(JSONArray googleStepsJson) {
        int count = googleStepsJson.length();
        String[] polylines = new String[count];
        for(int i = 0 ; i < count ; i++)
        {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        return polylines;
    }

    public String getPath(JSONObject googlePathJson) {
        String polyline = null;
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
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
