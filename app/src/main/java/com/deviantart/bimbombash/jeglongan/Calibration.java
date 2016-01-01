package com.deviantart.bimbombash.jeglongan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Calibration extends AppCompatActivity implements SensorEventListener {

    private SensorManager cSensorManager;
    private Sensor cSensor;

    private float cyaccel;
    private float cymax = 0f;
    private float czaccel;
    private float czmax = 0f;

    public TextView cymaxview;
    public TextView czmaxview;

    //boolean calibrate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        SharedPreferences sharedpreferences = getSharedPreferences("dataCalibration", Context.MODE_PRIVATE);

        //calibrate = sharedpreferences.getBoolean("calibrate",true);

        cSensorManager = (SensorManager) getSystemService (Context.SENSOR_SERVICE);

        cSensor = cSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        cSensorManager.registerListener(this, cSensor, SensorManager.SENSOR_DELAY_GAME);

        cymaxview = (TextView) findViewById(R.id.textView3);
        czmaxview = (TextView) findViewById(R.id.textView4);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calibration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public final static String EXTRA_MESSAGE="com.deviantart.bimbombash.TugasMap.MESSAGE";

    public void sendMessage (View view){
        putData();
        Intent intent = new Intent(this,MapsActivity.class);
        String message = "Sekarang anda di entah dimana...";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void putData(){
        SharedPreferences sharedpreferences = getSharedPreferences("dataCalibration", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("yForce",cymax);
        editor.putFloat("zForce",czmax);
        editor.putBoolean("calibrate",false);
        editor.apply();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == sensor.TYPE_ACCELEROMETER){
            cyaccel = event.values[1];
            czaccel = event.values[2];
            if (cyaccel > cymax){
                cymax = cyaccel;
                cymaxview.setText(Float.toString(cymax));
            }
            if (czaccel > czmax){
                czmax = czaccel;
                czmaxview.setText(Float.toString(czmax));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
