package com.zoma.map;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener,
        View.OnClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private double longitude;
    private double latitude;
    private int REQ_LOC_CODE = 99;
    private GoogleApiClient googleApiClient;
    private Marker currentLocationMarker;
    private Location location;
    private double endLatitude, endLongitude;
    private int proximityRadius = 10000;
    private String Address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //Initializing googleApiClient
        buildGoogleApiClient();
        CheckLocationPermission();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates state = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:


                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

        //getNeabyPlaces("restaurant");
        endLongitude = 31.27942934632301;
        endLatitude = 29.984261518516664;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            //moving the map to location
            moveMap(longitude,latitude);
            Toast.makeText(MapsActivity.this, longitude + " " + latitude, Toast.LENGTH_SHORT).show();
            //ReverseGecoding rg = new ReverseGecoding(this.getApplicationContext());
            Address = getCompleteAddressString(latitude, longitude);
        }
    }

    public String getNeabyPlaces(String type) {
        mMap.clear();
        String url = getNearByUrl(longitude, latitude, type);
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        NearByPlacesData neabyPlaces = new NearByPlacesData();
        neabyPlaces.execute(dataTransfer);
        return type;
    }

    public String getNearByUrl(double longitude, double latitude, String type) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + proximityRadius);
        googlePlaceUrl.append("&type=" + type);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=AIzaSyCKtLb0onHO0xTteMtXiMHmj9CCuBN_UFg");
        return googlePlaceUrl.toString();
    }

    public String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        // this.setContext(ma);
        String strAdd = "";
        StringBuilder strReturnedAddress = new StringBuilder("");
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Toast.makeText(this, "My Current location address : " + strAdd, Toast.LENGTH_SHORT).show();
                //  Log.w("My Current location address", strReturnedAddress.toString());
            } else {
                Toast.makeText(this, "My Current location address" + "No Address returned!", Toast.LENGTH_SHORT).show();
                // Log.w("My Current location address", "No Address returned!");
            }

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            //Toast.makeText(this, "Address : " + address, Toast.LENGTH_SHORT).show();
            String city = addresses.get(0).getLocality();
            //Toast.makeText(this, "City : " + city, Toast.LENGTH_SHORT).show();
            String state = addresses.get(0).getAdminArea();
            //Toast.makeText(this, "State : " + state, Toast.LENGTH_SHORT).show();
            String country = addresses.get(0).getCountryName();
            //Toast.makeText(this, "Country : " + country, Toast.LENGTH_SHORT).show();
            //String postalCode = addresses.get(0).getPostalCode();
            //Toast.makeText(this,"Postal Code : " + postalCode,Toast.LENGTH_SHORT).show();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            //Toast.makeText(this, "Known Name : " + knownName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "My Current location address" + "Cannot get Address!", Toast.LENGTH_SHORT).show();
            //Log.w("My Current location address", "Cannot get Address!");
        }

        getRoad(endLongitude,endLatitude);
        return strAdd;
    }

    public void getRoad(double newLongitude, double newLatitude) {
        //move to current position
        newLongitude = endLongitude;
        newLatitude = endLatitude;
        moveMap(newLongitude,newLatitude);
        getDistance(newLongitude,newLatitude);
    }

    public void getDistance(double newLongitude, double newLatitude) {
        final Button button = findViewById(R.id.search_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(endLatitude,endLongitude));
                markerOptions.title("Your Destination ;)");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                float[] results = new float[10];
                Location.distanceBetween(longitude,latitude,endLongitude,endLatitude,results);
                markerOptions.snippet("Distance = "+results[0]);
                Toast.makeText(MapsActivity.this, "Distance = "+results[0] + "*****" , Toast.LENGTH_SHORT).show();
                mMap.addMarker(markerOptions);
            }
        });
        getDirection(newLongitude,newLatitude);
        //getWalkingDirections();
    }

    public void getDirection(double newLongitude, double newLatitude) {
        Object[] dataTransfer = new Object[3];
        String url = getDirectionUrl();
        GetDirectionsData getDirectionsData = new GetDirectionsData();
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = new LatLng(endLatitude,endLongitude);
        getDirectionsData.execute(dataTransfer);
    }

    public String getDirectionUrl() {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+endLatitude+","+endLongitude);
        googleDirectionsUrl.append("&key=");//

        return googleDirectionsUrl.toString();
    }

    public void getWalkingDirections() {
        // Origin of route
        String str_origin = "origin=" + latitude + "," + longitude;

        // Destination of route
        String str_dest = "destination=" + endLatitude + "," + endLongitude;

        // Sensor enabled
        String sensor = "sensor=false";
        // Travelling mode enable
        // <strong>String mode = "mode = driving"</strong>
        String mode = "mode=walking";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&"+ mode;

        // Output format
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        try {
            String JSONdata = new DownloadURL().readURL(url);
            Toast.makeText(MapsActivity.this, JSONdata, Toast.LENGTH_SHORT).show();
//            DataParser dataParser =  new DataParser();
//            dataParser.parse(JSONdata);
//            HashMap<String, String> data = new HashMap<>();
//            Toast.makeText(MapsActivity.this, data.get("distance"), Toast.LENGTH_SHORT).show();
//            Toast.makeText(MapsActivity.this, data.get("duration"), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void moveMap(double longi,double lat) {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */
        LatLng myLoc = new LatLng(lat, longi);
        mMap.addMarker(new MarkerOptions()
                .position(myLoc)
                .draggable(true)
                .title(Address))
                .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //new ReverseGeocodingTask(getBaseContext()).execute(myLoc);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public boolean CheckLocationPermission() {
        // Here, thisActivity is the current activity
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , REQ_LOC_CODE);
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , REQ_LOC_CODE);
            }
            // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        Log.v(TAG, "view click event");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerDragStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        //Toast.makeText(MapsActivity.this, "onMarkerDrag", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // getting the Co-ordinates
        endLatitude = marker.getPosition().latitude;
        endLongitude = marker.getPosition().longitude;
        Toast.makeText(MapsActivity.this, "onMarkerDragEnd" + endLongitude + " " + endLatitude, Toast.LENGTH_SHORT).show();

        //move to current position
        moveMap(endLongitude,endLatitude);
        getDistance(endLongitude,endLatitude);
        /*Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(latitude,longitude), new LatLng(endLatitude, endLongitude))
                .width(5)
                .color(Color.RED));*/
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        marker.showInfoWindow();
        marker.setTitle(Address);
        Toast.makeText(MapsActivity.this, "onMarkerClick", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "LocationChanged", Toast.LENGTH_SHORT).show();
       /* Location lastLocation = location;
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        LatLng newLoc = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        mMap.addMarker(markerOptions.position(newLoc).title("Marker in Location Changed").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        currentLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLoc));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        //new ReverseGeocodingTask(getBaseContext()).execute(newLoc);
        mMap.clear();
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            //  Log.d("onLocationChanged", "Removing Location Updates");//
        }
        Toast.makeText(MapsActivity.this, "Here We Are!", Toast.LENGTH_LONG).show();

        getCompleteAddressString(latitude, longitude);
        */
        getRoad(endLongitude,endLatitude);
    }

}

