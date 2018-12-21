package com.khnsoft.schperfectmap;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class user_interface extends AppCompatActivity {
    SurfaceView preview_camera;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_user_interface);

        preview_camera = findViewById(R.id.preview_camera);
        preview_camera.setVisibility(View.GONE);
    }

    void startCamera() {

    }
}
