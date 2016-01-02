package com.deviantart.bimbombash.jeglongan;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;

public class GetInfo extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE=1;
    ImageView RiftarImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_info);
        Button TombolFoto=(Button) findViewById(R.id.TombolFoto);
        RiftarImageView =(ImageView) findViewById(R.id.RiftarImageView);

        String[] foods = {"Riftar", "Bima","Bob", "Nama", "Mama", "Papa"};
        ListAdapter buckysAdapter = new CustomCommentAdapter(this, foods);
        ListView buckysListView = (ListView) findViewById(R.id.commentList);
        buckysListView.setAdapter(buckysAdapter);

        //Disable Tombol
        if(!hasCamera())
            TombolFoto.setEnabled(false);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("info");
        tabSpec.setContent(R.id.tabInfo);
        tabSpec.setIndicator("Info");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("comment");
        tabSpec.setContent(R.id.tabComment);
        tabSpec.setIndicator("Comment");
        tabHost.addTab(tabSpec);
    }

    //Cek ada kamera gak (ngefek ke tombol)
    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    //Launching Kamera
    public void launchCamera(View view){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }


    //if you want to reutun the image taken
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
            //get the photo
            Bundle extras = data.getExtras();
            Bitmap photo =(Bitmap) extras.get("data");
            RiftarImageView.setImageBitmap(photo);
        }
    }
}
