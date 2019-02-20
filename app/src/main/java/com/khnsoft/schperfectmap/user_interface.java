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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.khnsoft.schperfectmap.DecisionTree.*;

import java.io.IOException;
import java.util.List;

public class user_interface extends AppCompatActivity{
	
	Button bt;
	TextView tv;
	SensorManager sm;
	Sensor gyroSensor;
	double dt = 0;
	double ts = 0;
	double mYaw, mPitch, mRoll;
	float NS2S = 1.0f/1000000000.0f;
	double RAD2DGR = 180 / Math.PI;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_interface);
		
		bt = findViewById(R.id.bt);
		tv = findViewById(R.id.tv);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		gyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sm.registerListener(sensorListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sm.unregisterListener(sensorListener);
	}
	
	SensorEventListener sensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] values;
			
			if (ts == 0) {
				ts = event.timestamp;
				return;
			}
			dt = (event.timestamp - ts) * NS2S;
			ts = event.timestamp;
			
			switch (event.sensor.getType()) {
				case Sensor.TYPE_GYROSCOPE:
					values = event.values.clone();
					mRoll += values[0] * dt * RAD2DGR;
					mPitch += values[1] * dt * RAD2DGR;
					mYaw += values[2] * dt * RAD2DGR;
					refresh();
					break;
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
		}
	};
	
	void refresh() {
		tv.setText(String.format("Yaw: %f\nPitch: %f\nRoll: %f", mYaw, mPitch, mRoll));
	}
}