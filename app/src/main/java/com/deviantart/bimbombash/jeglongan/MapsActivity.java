package com.deviantart.bimbombash.jeglongan;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements
        OnMapReadyCallback,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,

        LocationListener,

        SensorEventListener,

        com.google.android.gms.location.LocationListener,
        com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
{


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng myLocation;
    private LatLng defaultLocation = new LatLng(0f,0f);

    private SensorManager mSensorManager;
    private Sensor mSensor;

    public TextView ygravityMaxView;
    public TextView ygravityCurrentView;
    public TextView zgravityMaxView;
    public TextView zgravityCurrentView;
    public TextView lattitudeView;
    public TextView longitudeView;

    MarkerOptions userMarker;

    private float yAccelration_NOW;
    private float yAccelration_MAX;
    private float yAccelration_MIN=10f;
    private float zAccelration_NOW;
    private float zAccelration_MAX;
    private float zAccelration_MIN=10f;
    private float yCallibratedMaximumAcceleration;
    private float zCallibratedMaximumAcceleration;


    // Inti dari kuliah satu semester ini
    ParseObject parseMarker;
    public Hashtable<String,Jeglongan> jeglonganHashtable = new Hashtable<String,Jeglongan>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intent = getIntent().getExtras();

        getCallibrationData();

        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        setupView();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);

        userMarker      = new MarkerOptions()
                .position(defaultLocation)
                .title("Anda")
                .snippet("apa ini saya ?")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mMap.setOnMarkerClickListener(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "pZMFXpqkOSTeiI79BaqFTLqldv6oMMoa65xm3Kpi", "KUikuTVJpppmpQQmwQECUVy9QJLan2LAsv0W6nsg");
        parseMarker = new ParseObject("MarkerPosition");

        DrawMarkerFromParse();

        ygravityMaxView.setText(Float.toString(yCallibratedMaximumAcceleration));
        zgravityMaxView.setText(Float.toString(zCallibratedMaximumAcceleration));
        createLocationRequest();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }

        mSensorManager.unregisterListener(this);
    }

    public void getCallibrationData(){
        SharedPreferences sharedpreferences = getSharedPreferences("dataCalibration", Context.MODE_PRIVATE);

        yCallibratedMaximumAcceleration = sharedpreferences.getFloat("yForce",0f);
        zCallibratedMaximumAcceleration = sharedpreferences.getFloat("zForce",0f);

        yAccelration_MAX = yCallibratedMaximumAcceleration;
        zAccelration_MAX = zCallibratedMaximumAcceleration;

        yAccelration_MIN = yAccelration_MAX - 0.5f;
        zAccelration_MIN = zAccelration_MAX - 0.5f;

    }

    private void setupView(){
        ygravityMaxView     = (TextView) findViewById(R.id.textView5);
        ygravityCurrentView = (TextView) findViewById(R.id.textView7);
        zgravityMaxView     = (TextView) findViewById(R.id.textView9);
        zgravityCurrentView = (TextView) findViewById(R.id.textView11);
        longitudeView       = (TextView) findViewById(R.id.textView12);
        lattitudeView       = (TextView) findViewById(R.id.textView13);

        final Button tombolLapor = (Button) findViewById(R.id.button);
        tombolLapor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLatLongToParse(myLocation);
                addNewMarkerToMap(myLocation);
            }
        });
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // Normal Type Map
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Layer Google Map
        mMap.setMyLocationEnabled(true);
    }

    protected void createLocationRequest(){

        LocationRequest mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates(){
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if ((sensor.getType()== Sensor.TYPE_ACCELEROMETER)){

            yAccelration_NOW = event.values[1];
            zAccelration_NOW = event.values[2];

            ygravityCurrentView.setText(Float.toString(yAccelration_NOW));
            zgravityCurrentView.setText(Float.toString(zAccelration_NOW));

            if (yAccelration_NOW>=yAccelration_MIN){
                addLatLongToParse(myLocation);
                addNewMarkerToMap(myLocation);
            }
            if (zAccelration_NOW>=zAccelration_MIN){
                addLatLongToParse(myLocation);
                addNewMarkerToMap(myLocation);
            }
        }
    }

    private void addNewMarkerToMap(LatLng latLng) {
        MarkerOptions marker   = new MarkerOptions()
                .position(latLng)
                .title("Kubangan baru")
                .snippet(latLng.toString() + " Korban: anda ")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mMap.addMarker(marker);

        Toast.makeText(MapsActivity.this, "Kubangan Terdeteksi", Toast.LENGTH_SHORT).show();
    }

    private void addLatLongToParse(LatLng latLng){
        parseMarker.put("Lat", latLng.latitude);
        parseMarker.put("Lng", latLng.longitude);
        parseMarker.put("Counter", 1);
        parseMarker.saveInBackground();

    }

    private void DrawMarkerFromParse(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("MarkerPosition");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> markers, ParseException e) {
                if (e == null) {
                    // Extract data dari parse
                    for (ParseObject marker : markers) {

                        // LatLng
                        Double lat = marker.getDouble("Lat");
                        Double lng = marker.getDouble("Lng");
                        LatLng latLng = new LatLng(lat, lng);

                        // Counter & Warna Marker
                        int counter = marker.getInt("Counter");
                        float iconColor;
                        if (counter < 10) {
                            iconColor = BitmapDescriptorFactory.HUE_GREEN;
                        } else if (counter < 20) {
                            iconColor = BitmapDescriptorFactory.HUE_YELLOW;
                        } else {
                            iconColor = BitmapDescriptorFactory.HUE_RED;
                        }

                        // ID
                        String ID = marker.getObjectId();

                        // Marker
                        Marker jeglonganMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Kubangan " + ID)
                                .snippet(latLng.toString() + " Korban: " + counter)
                                .icon(BitmapDescriptorFactory.defaultMarker(iconColor)));

                        // Object Jeglongan
                        Jeglongan jeglongan = new Jeglongan(ID, jeglonganMarker, counter);

                        jeglonganHashtable.put(ID, jeglongan);

                        Log.d(TAG, "ID jeglongan: " + marker.getObjectId() + " " + latLng.toString() + " Korban: " + counter);
                    }
                } else {
                    // handle Parse Exception here
                    Log.d(TAG, "parse draw marker error");
                }
            }
        });
    }



    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location){
        Log.d(TAG, location.toString());

        double currentlatitude = location.getLatitude();
        double currentlongitude = location.getLongitude();

        lattitudeView.setText(Double.toString(currentlatitude));
        longitudeView.setText(Double.toString(currentlongitude));

        myLocation = new LatLng(currentlatitude, currentlongitude);

        //TODO: User Marker Here

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14));

    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location==null){
            startLocationUpdates();
        }
        else{
            handleNewLocation(location);
        }
    }

    /**
     *
     *  listener marker
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        String ID = title.replaceAll("Kubangan ", "");

        Jeglongan jeglonganYangDiClick = jeglonganHashtable.get(ID);

        Log.d(TAG, "Jeglongan " + ID + " di click " + jeglonganYangDiClick.getCounter());

        //TODO passing object jeglongan ke detail

        return false;
    }

    // region As is

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended please reconnect.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()){
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }
            catch (IntentSender.SendIntentException e){
                e.printStackTrace();
            }
        } else {
            Log.i(TAG,"Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //endregion
}
