package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class user_interface extends AppCompatActivity implements SurfaceHolder.Callback {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        preview = findViewById(R.id.preview_camera);
        background = findViewById(R.id.background);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(autoFocusCallback);
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

        init();
        //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
            cameraHolder.addCallback(this);
        }
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            camera.startPreview();
        } catch (Exception e) {
        }
        sm.registerListener(sensorlistener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(sensorlistener, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onPause() {
        sm.unregisterListener(sensorlistener);
        try {
            camera.stopPreview();
        } catch (Exception e) {
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
}
