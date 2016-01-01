package com.deviantart.bimbombash.jeglongan;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import com.parse.Parse;
import com.parse.ParseObject;

public class MapsActivity extends FragmentActivity
        implements
        OnMapReadyCallback,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,

        LocationListener,

        SensorEventListener,

        com.google.android.gms.location.LocationListener
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

    MarkerOptions userMarker;
    MarkerOptions potholeMarker;
    Marker old;
    Marker pothole;

    private float yAccelration_NOW;
    private float yAccelration_MAX;
    private float yAccelration_MIN=10f;
    private float zAccelration_NOW;
    private float zAccelration_MAX;
    private float zAccelration_MIN=10f;
    private float yCallibratedMaximumAcceleration;
    private float zCallibratedMaximumAcceleration;

    private int red = Color.argb(25, 255, 0, 0);
    //private int blue = Color.rgb(75,90,255);
    //private int white = Color.rgb(245,245,245);

    private boolean calibrate;

    public TextView lat;
    public TextView lang;

    ParseObject parseMarker = new ParseObject("MarkerPosition");


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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        
        potholeMarker   = new MarkerOptions()
                .position(defaultLocation).title("Kubangan");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

        ygravityMaxView.setText(Float.toString(yCallibratedMaximumAcceleration));
        zgravityMaxView.setText(Float.toString(zCallibratedMaximumAcceleration));
        createLocationRequest();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "pZMFXpqkOSTeiI79BaqFTLqldv6oMMoa65xm3Kpi", "KUikuTVJpppmpQQmwQECUVy9QJLan2LAsv0W6nsg");
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
        yAccelration_MIN = yAccelration_MAX - 0.5f;

        zAccelration_MAX = zCallibratedMaximumAcceleration;
        zAccelration_MIN = zAccelration_MAX - 0.5f;

        //calibrate = sharedpreferences.getBoolean("calibrate",true);
    }

    private void setupView(){
        ygravityMaxView     = (TextView) findViewById(R.id.textView5);
        ygravityCurrentView = (TextView) findViewById(R.id.textView7);
        zgravityMaxView     = (TextView) findViewById(R.id.textView9);
        zgravityCurrentView = (TextView) findViewById(R.id.textView11);
        lang                = (TextView) findViewById(R.id.textView12);
        lat                 = (TextView) findViewById(R.id.textView13);
        
        final Button tombolLapor = (Button) findViewById(R.id.button);
        tombolLapor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarkerToMap(myLocation);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
                addMarkerToMap(myLocation);
                addLatLongToParse(myLocation);
            }
            if (zAccelration_NOW>=zAccelration_MIN){
                addMarkerToMap(myLocation);
                addLatLongToParse(myLocation);
            }
        }
    }

    private void addMarkerToMap(LatLng latLng) {
        //TODO add marker
        MarkerOptions marker   = new MarkerOptions()
                .position(latLng).title("Kubangan");

        mMap.addMarker(marker);

        Toast.makeText(MapsActivity.this, "Kubangan Terdeteksi", Toast.LENGTH_SHORT).show();
    }

    private void addLatLongToParse(LatLng latLng){

        double[] coord = {latLng.latitude, latLng.longitude};

        parseMarker.put("LongLat", coord);
        parseMarker.saveInBackground();
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location){
        Log.d(TAG, location.toString());

        double currentlatitude = location.getLatitude();
        double currentlongitude = location.getLongitude();

        lat .setText(Double.toString(currentlatitude));
        lang.setText(Double.toString(currentlongitude));

        myLocation = new LatLng(currentlatitude, currentlongitude);

        userMarker.position(myLocation);

        if (old!= null) old.remove();

        old = mMap.addMarker(userMarker);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

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
