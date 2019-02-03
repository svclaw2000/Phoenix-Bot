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

import com.khnsoft.schperfectmap.DecisionTree.*;

import java.io.IOException;
import java.util.List;

public class user_interface extends AppCompatActivity {
    private int mAzimuth = 0; // degree

    private SensorManager mSensorManager = null;

    private Sensor mSensor;

    boolean haveGravity = false;
    boolean haveAccelerometer = false;
    boolean haveMagnetometer = false;

    TextView tv;

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        float[] orientation = new float[3];
        float[] rMat = new float[9];

        public void onAccuracyChanged( Sensor sensor, int accuracy ) {}

        @Override
        public void onSensorChanged( SensorEvent event ) {
            if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ){
                // calculate th rotation matrix
                SensorManager.getRotationMatrixFromVector( rMat, event.values );
                // get the azimuth value (orientation[0]) in degree
                mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
                tv.setText(""+mAzimuth);
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        this.mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        this.mSensor = this.mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        tv = findViewById(R.id.textView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.haveGravity = this.mSensorManager.registerListener(mSensorEventListener, this.mSensor, SensorManager.SENSOR_DELAY_GAME);
    }
}
