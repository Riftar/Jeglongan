package com.deviantart.bimbombash.jeglongan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class jeglongansplash extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 4000;
    boolean calibrated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeglongansplash);
        ImageView gif = (ImageView) findViewById(R.id.splashScreen);
        SharedPreferences sharedpreferences = getSharedPreferences("dataCalibration", Context.MODE_PRIVATE);
        gif.setBackgroundResource(R.drawable.jeglongan);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                decide();
                jeglongansplash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
        calibrated = sharedpreferences.getBoolean("Calibrated", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_jeglongansplash, menu);
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

    public void decide (){
        //putData();
        if (calibrated){
            Intent intent = new Intent(this,MapsActivity.class);
            startActivity(intent);
        }
        if (!calibrated){
            calibrated = true;
            SharedPreferences sharedpreferences = getSharedPreferences("dataCalibration", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean("Calibrated", calibrated);
            editor.apply();
            Intent intent = new Intent(this,Calibration.class);
            startActivity(intent);
        }
    }
}
