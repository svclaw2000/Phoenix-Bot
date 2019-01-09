package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    SurfaceView preview;
    Camera camera;
    SurfaceHolder cameraHolder;
    LinearLayout background;
    float[] mGravity;
    float[] mGeomagnetic;
    float mAzimut, mPitch, mRoll;
    SensorManager sm;
    Sensor mAccelerometer;
    Sensor mMagneticField;
    TextView azimut;
    TextView pitch;
    TextView roll;
    SharedPreferences sp;
    FloatingActionButton fab;
    FloatingActionButton fab1;
    FloatingActionButton fab2;
    Animation fab_open;
    Animation fab_close;
    Boolean isOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.preview_camera);
        background = findViewById(R.id.background);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.autoFocus(autoFocusCallback);
                } catch (Exception e) {
                }
            }
        });

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        azimut = findViewById(R.id.azimut);
        pitch = findViewById(R.id.pitch);
        roll = findViewById(R.id.roll);
        mAzimut = 0;
        mPitch = 0;
        mRoll = 0;

        sp = getSharedPreferences("settings", MODE_PRIVATE);
        if (sp.getBoolean("direction", true)) {
            findViewById(R.id.sensorName).setVisibility(View.VISIBLE);
            findViewById(R.id.sensorValue).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.sensorName).setVisibility(View.INVISIBLE);
            findViewById(R.id.sensorValue).setVisibility(View.INVISIBLE);
        }

        isOpen = false;
        fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fab.setImageResource(R.drawable.setting);
        fab1.setImageResource(R.drawable.setting);
        fab2.setImageResource(R.drawable.record);
        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        init();
    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 0x12345);
            init();
        } else {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            cameraHolder = preview.getHolder();
            cameraHolder.addCallback(callback);
        }
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (camera == null) {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                }
            } catch (IOException e) {
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (cameraHolder == null) return;

            try {
                camera.stopPreview();
            } catch (Exception e) {
            }

            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            camera.setParameters(parameters);
            try {
                camera.setPreviewDisplay(cameraHolder);
                camera.startPreview();
            } catch (Exception e) {
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (sp.getBoolean("direction", true)) {
            findViewById(R.id.sensorName).setVisibility(View.VISIBLE);
            findViewById(R.id.sensorValue).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.sensorName).setVisibility(View.INVISIBLE);
            findViewById(R.id.sensorValue).setVisibility(View.INVISIBLE);
        }
        init();
        sm.registerListener(sensorlistener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(sensorlistener, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        handler.sendEmptyMessage(0);
    }

    @Override
    public void onPause() {
        sm.unregisterListener(sensorlistener);
        try {
            camera.stopPreview();
            Log.i("@@@", "Preview paused.");
        } catch (Exception e) {
            Log.i("@@@", "Preview not paused.");
        }
        handler.removeMessages(0);
        super.onPause();
    }

    SensorEventListener sensorlistener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = event.values;
            }
            if (mGravity != null && mGeomagnetic != null) {
                float[] R = new float[9];
                float[] I = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

                if (success) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    mAzimut = (float) Math.toDegrees(orientation[0]);
                    mPitch = (float) Math.toDegrees(orientation[1]);
                    mRoll = (float) Math.toDegrees(orientation[2]);

                    Log.i("@@@", String.format("Azimut: %f, Pitch: %f, Roll: %f", mAzimut, mPitch, mRoll));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            azimut.setText("" + mAzimut);
            pitch.setText("" + mPitch);
            roll.setText("" + mRoll);
            handler.sendEmptyMessageDelayed(0, 1000);
            return false;
        }
    });

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                anim();
                break;
            case R.id.fab1:
                anim();
                Intent intent = new Intent(this, preferences.class);
                startActivity(intent);
                break;
            case R.id.fab2:
                anim();
                Intent intent2 = new Intent(this, admin_interface.class);
                startActivity(intent2);
                break;
        }
    }

    void anim(){
        if (isOpen) {
            fab.setImageResource(R.drawable.setting);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isOpen = false;
        } else {
            fab.setImageResource(R.drawable.cancel);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isOpen = true;
        }
    }
}
