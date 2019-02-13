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

public class user_interface extends AppCompatActivity implements SensorEventListener{
	
	TextView nowOne, oldOne;
	Button button;
	
	private SensorManager sm;
	private Sensor accelSensor = null, compassSensor = null, orientSensor = null;
	private float[] accelValues = new float[3], compassValues = new float[3],orientValues = new float[3];
	private boolean ready1 = false; //检查传感器是否正常工作，即是否同时具有加速传感器和磁场传感器。
	private boolean ready2 = false;
	private float[] inR = new float[9];
	private float[] inclineMatrix = new float[9];
	private float[] prefValues = new float[3];
	private double mInclination;
	private int count = 1;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_interface);
		
		nowOne = findViewById(R.id.newOne);
		oldOne = findViewById(R.id.oldOne);
		button = findViewById(R.id.button);
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doUpdate(null);
			}
		});
		
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		compassSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		orientSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sm.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(this, orientSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sm.unregisterListener(this);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		switch(event.sensor.getType()){
			case Sensor.TYPE_ACCELEROMETER:
				accelValues = event.values.clone();
				if(compassValues[0] != 0)
					ready1 = true;
				break;
			
			case Sensor.TYPE_MAGNETIC_FIELD:
				compassValues = event.values.clone();
				if(accelValues[2] != 0)
					ready2 = true;
				break;
		}
		
		if(!ready1 || !ready2)
			return;
		
		if(SensorManager.getRotationMatrix(inR, inclineMatrix, accelValues, compassValues)){
			SensorManager.getOrientation(inR, prefValues);
			mInclination = SensorManager.getInclination(inclineMatrix);
			
			doUpdate(null);
			
		}else{
			Toast.makeText(this, "无法获得矩阵（SensorManager.getRotationMatrix）", Toast.LENGTH_LONG);
			finish();
		}
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	
	}
	
	public void doUpdate(View v){
		if(!ready1 || !ready2)
			return;
		float mAzimuth = (float)Math.toDegrees(prefValues[0]);
		
		String msg = String.format("推荐方式：\n方位角：%7.3f\npitch: %7.3f\nroll: %7.3f\n地磁仰角：%7.3f\n",
				mAzimuth,Math.toDegrees(prefValues[1]),Math.toDegrees(prefValues[2]),
				Math.toDegrees(mInclination));
		nowOne.setText(msg);
	}
}
