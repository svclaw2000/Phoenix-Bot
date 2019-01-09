package com.khnsoft.schperfectmap;

import android.Manifest;
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SENSOR_SERVICE;

public class UserFragment extends Fragment {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_interface, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preview = getView().findViewById(R.id.preview_camera);
        background = getView().findViewById(R.id.background);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(autoFocusCallback);
            }
        });

        sm = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mAccelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        azimut = getView().findViewById(R.id.azimut);
        pitch = getView().findViewById(R.id.pitch);
        roll = getView().findViewById(R.id.roll);
        mAzimut = 0;
        mPitch = 0;
        mRoll = 0;

        sp = getActivity().getSharedPreferences("settings", MODE_PRIVATE);
        if (sp.getBoolean("direction", true)) {
            getView().findViewById(R.id.sensorName).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.sensorValue).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.sensorName).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.sensorValue).setVisibility(View.INVISIBLE);
        }

        init();
    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            getView().findViewById(R.id.sensorName).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.sensorValue).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.sensorName).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.sensorValue).setVisibility(View.INVISIBLE);
        }
        try {
            camera.startPreview();
        } catch (Exception e) {
        }
        sm.registerListener(sensorlistener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(sensorlistener, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        handler.sendEmptyMessage(0);
    }

    @Override
    public void onPause() {
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
