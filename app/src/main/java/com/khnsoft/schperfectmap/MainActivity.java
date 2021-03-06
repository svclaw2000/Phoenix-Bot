package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.khnsoft.schperfectmap.add_ap_info.MULTIPLE_PERMISSIONS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	
	SurfaceView preview;
	Camera camera;
	SurfaceHolder cameraHolder;
	LinearLayout background;
	
	// sensor
	SensorManager sm;
	Sensor mGyroSensor;
	Sensor mAcceSensor;
	Sensor mMagnSensor;
	Sensor mGravSensor;
	
	double mYaw, mPitch, mRoll;
	double mAccPitch, mAccRoll;
	double timestamp;
	double temp;
	float a = 0.2f;
	double dt;
	double RAD2DGR = 180.0 / Math.PI;
	double exts = 0;
	double dt2;
	static final float NS2S = 1.0f / 1000000000.0f;
	TextView yaw;
	TextView pitch;
	TextView roll;
	boolean gyroRunning = false;
	boolean acceRunning = false;
	float[] mGyroValues = new float[3];
	float[] mAcceValues = new float[3];
	long lastSyncTime = -1;
	double[] fixYaw;
	int fixCount = 0;
	boolean fixMode = false;
	final int MAX_FIX_COUNT = 3;
	final int MIN_ROLL = 30;
	final int MAX_ROLL = 50;
	final int MIN_PITCH = -20;
	final int MAX_PITCH = 20;
	double tmpYaw, tmpPitch, tmpRoll;
	ArrayList<Double> yawFilter;
	ArrayList<Double> pitchFilter;
	ArrayList<Double> rollFilter;
	final int FILTER_SIZE = 7;
	
	float[] gData = new float[3];
	float[] mData = new float[3];
	boolean gReady = false;
	boolean mReady = false;
	
	SharedPreferences sp;
	SharedPreferences.Editor editor;
	ImageView fab;
	ImageView fab1;
	ImageView fab2;
	ImageView fab3;
	Animation fab_open;
	Animation fab_close;
	Boolean isOpen;
	
	WifiManager wm;
	List<ScanResult> scanResult;
	String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
	boolean wifiScanning = false;
	
	String strJson;
	HttpAsyncTask httpTask;
	
	/*
	realDecisionTree dTree = null;
	Vector<String> rawAttributes;
	Vector<Double> rawValues;
	*/
	
	JsonObject wifiFingerprint;
	TextView Tres;
	
	ImageView phoenix;
	int[] mUserPos = null;
	int[] mPhoenixPos = null;
	double fixed_default_yaw = -1.0;
	double ex_fixed_yaw = -1.0;
	// final int PHOENIX_DEFAULT_SIZE_X = 320;
	// final int PHOENIX_DEFAULT_SIZE_Y = 400;
	final int PHOENIX_DEFAULT_SIZE_X = 640;
	final int PHOENIX_DEFAULT_SIZE_Y = 800;
	final int DEFAULT_POS_X = 540;
	final int DEFAULT_POS_Y = 1100;
	final Double[] DEFAULT_YAW = {237.0, 0.0, 0.0, 0.0, 190.57,        // 0-4
			195.45, 216.96, 277.63, 251.24, 244.26,                    // 5-9
			263.04, 275.30, 233.52, 213.76, 210.58};                   // 10-14
	final Double DEFAULT_ROLL = 90.0;
	
	TextView room;
	final String[][] rooms = {
			{"", ""}, {"", ""}, {"", ""}, {"", ""},
			{"ML313", "Server Room"},            // 4
			{"", ""},                            // 5
			{"ML313", ""},                       // 6
			{"Prof. Y.S.Jeong", "ML304"},        // 7
			{"", ""},                            // 8
			{"Prof. J.H.Kim", ""},               // 9
			{"Prof. J.Y.Woo", ""},               // 10
			{"Prof. Y.M.Kim", "ML304"},          // 11
			{"Prof. C.K.Cho", "R.A."},            // 12
			{"", ""},                            // 13
			{"", "Conference Room"}              // 14
	};
	final int ROOM_DEFAULT_SIZE_X = 500;
	final int ROOM_DEFAULT_SIZE_Y = 200;
	
	EditText dialogInput;
	Button dialogSend;
	Button Bstt;
	boolean sendable = false;
	float[] displayPos = null;
	TextView dialogMsg;
	final int DIALOG_DEFAULT_SIZE_X = 700;
	final int DIALOG_DEFAULT_SIZE_Y = 400;
	String resp = "";
	boolean dialogVisible = false;
	long dialogTime = 0;
	final long DIALOG_SHOW_TIME = 3000;
	String sendMsg;
	String action;
	
	final long WALKING_CHK_DURING_TIME = 500;
	final float WALKING_WIDTH_MIN = 3;
	final float WALKING_WIDTH_MAX = 5;
	long lastChkWalking = 0;
	ArrayList<Float> chkWalkSet;
	boolean isWalking = true;
	
	final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
	ImageViewTarget ivTarget = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		preview = findViewById(R.id.preview_camera);
		background = findViewById(R.id.background);
		background.setOnClickListener(this);
		/*
		background.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				GradientDrawable drawable = (GradientDrawable) Tres.getBackground();
				switch (motionEvent.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_MOVE:
						isWalking = true;
						if (!wifiScanning) {
							wm.startScan();
							wifiScanning = true;
						}
						drawable.setColor(Color.YELLOW);
						break;
						
					case MotionEvent.ACTION_UP:
						isWalking = false;
						drawable.setColor(Color.WHITE);
						break;
				}
				return false;
			}
		});
		//*/
		
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		yaw = findViewById(R.id.yaw);
		pitch = findViewById(R.id.pitch);
		roll = findViewById(R.id.roll);
		mGyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mAcceSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mGravSensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
		
		Tres = findViewById(R.id.result);
		
		sp = getSharedPreferences("settings", MODE_PRIVATE);
		editor = sp.edit();
		if (sp.getBoolean("direction", true)) {
			Tres.setVisibility(View.VISIBLE);
			findViewById(R.id.sensorValue).setVisibility(View.VISIBLE);
		} else {
			Tres.setVisibility(View.INVISIBLE);
			findViewById(R.id.sensorValue).setVisibility(View.INVISIBLE);
		}
		
		isOpen = false;
		fab = findViewById(R.id.fab);
		fab1 = findViewById(R.id.fab1);
		fab2 = findViewById(R.id.fab2);
		fab3 = findViewById(R.id.fab3);
		fab.setImageResource(R.drawable.setting);
		fab1.setImageResource(R.drawable.setting);
		fab2.setImageResource(R.drawable.record);
		fab3.setImageResource(R.drawable.compass);
		fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
		fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close);
		fab.setOnClickListener(this);
		fab1.setOnClickListener(this);
		fab2.setOnClickListener(this);
		fab3.setOnClickListener(this);
		init();
		
		String FILE_NAME = "Log.log";
		File file = new File(this.getFilesDir(), FILE_NAME);
		if (!file.exists()) {
			try {
				FileWriter fileWriter = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fileWriter);
				bw.write("Log File\n\n");
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		wm.startScan();
		wifiScanning = true;
		// Log.i("@@@", "Start Scan");
		
		if (!checkPermissions()) {
			finish();
		}
		
		if (chkInfo()) {
			httpTask = new HttpAsyncTask(MainActivity.this);
			String ip = "https://" + sp.getString("ip", "");
			Log.i("@@@", "Target IP: " + ip);
			httpTask.execute(ip, "onCreate");
		}
		
		// Initialize Phoenix character
		phoenix = new ImageView(this);
		phoenix.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!sendable) return;
				resp = "OUCH!";
				dialogTime = System.currentTimeMillis();
				dialogHandler.sendEmptyMessage(0);
				// Glide.with(MainActivity.this).load(R.raw.stickman2).into(ivTarget);
			}
		});
		// phoenix.setImageResource(R.drawable.phoenix);
		ivTarget = new GlideDrawableImageViewTarget(phoenix, 1);
		phoenix.setImageResource(R.drawable.character);
		
		phoenix.setLayoutParams(new ViewGroup.LayoutParams(PHOENIX_DEFAULT_SIZE_X, PHOENIX_DEFAULT_SIZE_Y));
		phoenix.setVisibility(View.INVISIBLE);
		((FrameLayout) findViewById(R.id.mainView)).addView(phoenix);
		
		room = new TextView(this);
		room.setLayoutParams(new ViewGroup.LayoutParams(ROOM_DEFAULT_SIZE_X, ROOM_DEFAULT_SIZE_Y));
		room.setVisibility(View.INVISIBLE);
		room.setTextSize(20);
		room.setGravity(Gravity.CENTER);
		room.setBackgroundResource(R.drawable.ap_layout_box_white);
		((FrameLayout) findViewById(R.id.mainView)).addView(room);
		
		dialogInput = findViewById(R.id.dialogInput);
		dialogSend = findViewById(R.id.dialogSend);
		dialogSend.setOnClickListener(this);
		dialogMsg = new TextView(this);
		dialogMsg.setLayoutParams(new ViewGroup.LayoutParams(DIALOG_DEFAULT_SIZE_X, DIALOG_DEFAULT_SIZE_Y));
		dialogMsg.setVisibility(View.INVISIBLE);
		dialogMsg.setTextSize(20);
		dialogMsg.setGravity(Gravity.CENTER);
		dialogMsg.setBackgroundResource(R.drawable.ap_layout_box_white);
		((FrameLayout) findViewById(R.id.mainView)).addView(dialogMsg);
		
		Bstt = findViewById(R.id.voiceStream);
		Bstt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				inputVoice(dialogInput);
			}
		});
		
		/* Decision Tree
		FILE_NAME = "identifier.md";
		file = new File(this.getFilesDir(), FILE_NAME);
		if (file.exists()) {
			StringBuffer output = new StringBuffer();

			try {
				FileReader fileReader = new FileReader(file.getAbsoluteFile());
				BufferedReader br = new BufferedReader(fileReader);
				String line = "";
				while ((line = br.readLine()) != null) {
					Log.i("@@@", line);
					output.append(line + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!output.toString().isEmpty()) {
				try {
					String tmp = output.toString();
					dTree = new realDecisionTree("m2", tmp);
					dTree.setItemSet();
					dTree.setGoalAttribute();
					dTree.resetHash();
					Log.i("@@@", "Create decision tree");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		*/
	}
	
	Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
		}
	};
	
	Handler starthandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			try {
				camera = Camera.open();
				camera.setDisplayOrientation(90);
				cameraHolder = preview.getHolder();
				cameraHolder.addCallback(callback);
				starthandler.removeMessages(0);
			} catch (Exception e) {
				e.printStackTrace();
				starthandler.sendEmptyMessageDelayed(0, 1000);
			}
			return false;
		}
	});
	
	void init() {
		starthandler.sendEmptyMessage(0);
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
			if (cameraHolder.getSurface() == null) return;
			
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
		if (!chkInfo()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("???????????? ??????")
					.setMessage("AP Finger Print ?????? ??? ????????? ????????? ????????????, ??????, ?????????, ???????????? ????????? ???????????????. ?????????????????? ?????? ??? ?????? ??????????????????.")
					.setCancelable(false)
					.setPositiveButton("??????", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(MainActivity.this, preferences.class);
							startActivity(intent);
						}
					})
					.show();
		}
		if (sp.getBoolean("direction", true)) {
			Tres.setVisibility(View.VISIBLE);
			findViewById(R.id.sensorValue).setVisibility(View.VISIBLE);
		} else {
			Tres.setVisibility(View.INVISIBLE);
			findViewById(R.id.sensorValue).setVisibility(View.INVISIBLE);
		}
		
		sm.registerListener(sensorlistener, mGyroSensor, SensorManager.SENSOR_DELAY_UI);
		sm.registerListener(sensorlistener, mAcceSensor, SensorManager.SENSOR_DELAY_UI);
		sm.registerListener(sensorlistener, mMagnSensor, SensorManager.SENSOR_DELAY_UI);
		sm.registerListener(sensorlistener, mGravSensor, SensorManager.SENSOR_DELAY_UI);
		handler.sendEmptyMessage(0);
		
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		try {
			camera = Camera.open();
			camera.setDisplayOrientation(90);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (wm != null) {
			if (!wm.isWifiEnabled() && wm.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
				wm.setWifiEnabled(true);
			}
			final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			registerReceiver(receiver, filter);
		}
	}
	
	@Override
	public void onPause() {
		sm.unregisterListener(sensorlistener);
		try {
			camera.stopPreview();
			if (camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
			}
			Log.i("@@@", "Preview paused.");
		} catch (Exception e) {
			Log.i("@@@", "Preview not paused.");
		}
		handler.removeMessages(0);
		
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	boolean checkPermissions() {
		int result;
		List<String> listPermissionsNeeded = new ArrayList<>();
		for (String p : permissions) {
			result = ContextCompat.checkSelfPermission(this, p);
			if (result != PackageManager.PERMISSION_GRANTED) {
				listPermissionsNeeded.add(p);
			}
		}
		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
			return false;
		}
		return true;
	}
	
	SensorEventListener sensorlistener = new SensorEventListener() {
		float[] rMat = new float[9];
		float[] iMat = new float[9];
		float[] orientation = new float[3];
		double accYaw;
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			switch (event.sensor.getType()) {
				case Sensor.TYPE_GYROSCOPE:
					mGyroValues = event.values.clone();
					if (!gyroRunning) {
						gyroRunning = true;
					}
					if (exts == 0) {
						exts = event.timestamp;
					} else {
						dt2 = (event.timestamp - exts) * NS2S;
						exts = event.timestamp;
						if (yawFilter == null) yawFilter = new ArrayList<Double>();
						if (mRoll >= 0 && mRoll < 90) {
							tmpYaw = tmpYaw - (mGyroValues[2] * (1 - mRoll / 90.0) + mGyroValues[1] * (mRoll / 90.0)) * dt2 * RAD2DGR;
						} else if (mRoll >= 90 && mRoll < 180) {
							tmpYaw = tmpYaw - (mGyroValues[2] * (1 - mRoll / 90.0) + mGyroValues[1] * (2 - mRoll / 90.0)) * dt2 * RAD2DGR;
						}
						while (tmpYaw < 0) {
							tmpYaw += 360;
						}
						tmpYaw = tmpYaw % 360;
						
						if (!fixMode) {
							if (yawFilter.size() < FILTER_SIZE - 1) yawFilter.add(tmpYaw);
							else if (yawFilter.size() == FILTER_SIZE - 1) {
								yawFilter.add(tmpYaw);
								double sinsum = 0;
								double cossum = 0;
								for (double i : yawFilter) {
									sinsum += Math.sin(Math.toRadians(i));
									cossum += Math.cos(Math.toRadians(i));
								}
								mYaw = Math.toDegrees(Math.atan2(sinsum, cossum)) % 360;
							} else {
								yawFilter.add(tmpYaw);
								if (Math.abs(yawFilter.get(0) - mYaw) > 180 || Math.abs(tmpYaw - mYaw) > 180) {
									double tYaw, lYaw, nYaw;
									tYaw = (mYaw < 180) ? mYaw + 360 : mYaw;
									lYaw = (yawFilter.get(0) < 180) ? yawFilter.get(0) + 360 : yawFilter.get(0);
									nYaw = (tmpYaw < 180) ? tmpYaw + 360 : tmpYaw;
									mYaw = (tYaw + (nYaw - lYaw) / FILTER_SIZE) % 360;
								} else
									mYaw = (mYaw + (tmpYaw - yawFilter.get(0)) / FILTER_SIZE) % 360;
								yawFilter.remove(0);
							}
						}
					}
					break;
				
				case Sensor.TYPE_ACCELEROMETER:
					mAcceValues = event.values.clone();
					if (!acceRunning) {
						acceRunning = true;
					}
					
					if (chkWalkSet == null) {
						chkWalkSet = new ArrayList<Float>();
					}
					if (System.currentTimeMillis() - lastChkWalking > WALKING_CHK_DURING_TIME) {
						if (chkWalkSet.isEmpty()) {
							lastChkWalking = System.currentTimeMillis();
							break;
						}
						float diff = Collections.max(chkWalkSet) - Collections.min(chkWalkSet);
						isWalking = (WALKING_WIDTH_MIN < diff);
						isWalking = true;
						GradientDrawable drawable = (GradientDrawable) Tres.getBackground();
						
						/*
						if (isWalking) {
							drawable.setColor(Color.YELLOW);
							if (!wifiScanning) {
								wm.startScan();
								wifiScanning = true;
							}
						}
						else
							drawable.setColor(Color.WHITE);
						*/
						
						chkWalkSet = new ArrayList<Float>();
						lastChkWalking = System.currentTimeMillis();
					} else chkWalkSet.add(mAcceValues[1]);
					break;
				
				case Sensor.TYPE_MAGNETIC_FIELD:
					mData = event.values.clone();
					mReady = true;
					break;
				
				case Sensor.TYPE_GRAVITY:
					gData = event.values.clone();
					gReady = true;
					break;
				
				default:
					return;
			}
			
			if (mReady && gReady && SensorManager.getRotationMatrix(rMat, iMat, gData, mData)) {
				accYaw = (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
				// accYaw = 0;
				//*
				if (fixMode) { // && mRoll < MAX_ROLL && mRoll > MIN_ROLL && mPitch < MAX_PITCH && mPitch > MIN_PITCH) {
					if (fixCount == 0) fixYaw = new double[MAX_FIX_COUNT];
					if (fixCount < MAX_FIX_COUNT) {
						// fixYaw[fixCount] = accYaw;
						fixYaw[fixCount] = 130;
						fixCount++;
					} else {
						double sinsum = 0;
						double cossum = 0;
						for (double i : fixYaw) {
							sinsum += Math.sin(Math.toRadians(i));
							cossum += Math.cos(Math.toRadians(i));
						}
						tmpYaw = Math.toDegrees(Math.atan2(sinsum, cossum)) % 360;
						// fixed_default_yaw = DEFAULT_YAW[mUserPos[0]];
						fixed_default_yaw = 150;
						lastSyncTime = System.currentTimeMillis();
						fixCount = 0;
						yawFilter = new ArrayList<Double>();
						Toast.makeText(MainActivity.this, "?????? ?????????", Toast.LENGTH_SHORT).show();
						fixMode = false;
					}
				}
				//	*/
				
				/*
				if (fixMode) {
					tmpYaw = 180;
					fixed_default_yaw = 0;
					yawFilter = new ArrayList<Double>();
					fixMode = false;
					Toast.makeText(MainActivity.this, "?????? ?????????", Toast.LENGTH_SHORT).show();
				}
				//	*/
				
				mReady = false;
				gReady = false;
			}
			
			if (gyroRunning && acceRunning) {
				complementaty(event.timestamp);
				toonPosition(mUserPos, new Double[]{mYaw, mPitch, mRoll}, mPhoenixPos, null);
				textPosition(mUserPos, new Double[]{mYaw, mPitch, mRoll});
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
	void complementaty(double new_ts) {
		gyroRunning = false;
		acceRunning = false;
		
		if (timestamp == 0) {
			timestamp = new_ts;
			return;
		}
		dt = (new_ts - timestamp) * NS2S;
		timestamp = new_ts;
		
		mAccPitch = -Math.atan2(mAcceValues[0], mAcceValues[2]) * RAD2DGR;
		mAccRoll = Math.atan2(mAcceValues[1], mAcceValues[2]) * RAD2DGR;
		
		temp = (1 / a) * (mAccPitch - tmpPitch) + mGyroValues[1];
		tmpPitch = tmpPitch + (temp * dt);
		
		temp = (1 / a) * (mAccRoll - tmpRoll) + mGyroValues[0];
		tmpRoll = tmpRoll + (temp * dt);
		
		if (pitchFilter == null) pitchFilter = new ArrayList<Double>();
		if (rollFilter == null) rollFilter = new ArrayList<Double>();
		
		if (pitchFilter.size() < FILTER_SIZE - 1) pitchFilter.add(tmpPitch);
		else if (pitchFilter.size() == FILTER_SIZE - 1) {
			pitchFilter.add(tmpPitch);
			double sum = 0;
			for (double i : pitchFilter) sum += i;
			mPitch = sum / pitchFilter.size();
		} else {
			pitchFilter.add(tmpPitch);
			mPitch = mPitch + (pitchFilter.get(pitchFilter.size() - 1) - pitchFilter.get(0)) / FILTER_SIZE;
			pitchFilter.remove(0);
		}
		
		if (rollFilter.size() < FILTER_SIZE - 1) rollFilter.add(tmpRoll);
		else if (rollFilter.size() == FILTER_SIZE - 1) {
			rollFilter.add(tmpRoll);
			double sum = 0;
			for (double i : rollFilter) sum += i;
			mRoll = sum / rollFilter.size();
		} else {
			rollFilter.add(tmpRoll);
			mRoll = mRoll + (rollFilter.get(rollFilter.size() - 1) - rollFilter.get(0)) / FILTER_SIZE;
			rollFilter.remove(0);
		}
	}
	
	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			yaw.setText(String.format("%.2f", mYaw));
			roll.setText(String.format("%.2f", mRoll));
			pitch.setText(String.format("%.2f", mPitch));
			handler.sendEmptyMessageDelayed(0, 100);
			return false;
		}
	});
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action != null) {
				if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
					getWIFIScanResult();
					// if (isWalking) wm.startScan();
					// else wifiScanning = false;
				} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
					context.sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
				}
			}
		}
	};
	
	public void getWIFIScanResult() {
		scanResult = wm.getScanResults();
		wifiFingerprint = new JsonObject();
		if (scanResult.size() != 0) {
			for (int i = 0; i < scanResult.size(); i++) {
				ScanResult result = scanResult.get(i);
				wifiFingerprint.addProperty(result.BSSID, result.level);
			}
			
			httpTask = new HttpAsyncTask(MainActivity.this);
			String ip = "https://" + sp.getString("ip", "");
			Log.i("@@@", "Target IP: " + ip);
			httpTask.execute(ip, "locate");
		}
		
		/* Decision Tree
		rawAttributes = new Vector<String>();
		rawValues = new Vector<Double>();
		if (scanResult.size()!=0) {
			for (int i=0; i<scanResult.size(); i++) {
				ScanResult result = scanResult.get(i);
				rawAttributes.add(result.BSSID);
				rawValues.add((double) result.level);
			}
			Log.i("@@@", "rawAttributes: " + rawAttributes.toString());
			Log.i("@@@", "rawValues: " + rawValues.toString());
			try {
				String result = dTree.TestWithRealTimeData(rawAttributes, rawValues);
				Tres.setText(result);
			} catch (Exception e) {
				e.printStackTrace();
				Tres.setText("Error");
			}
		}
		*/
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.background:
				try {
					camera.autoFocus(autoFocusCallback);
				} catch (Exception e) {
				}
				break;
			
			case R.id.fab:
				anim();
				break;
			
			case R.id.fab1:
				anim();
				Intent intent = new Intent(this, preferences.class);
				//	Intent intent = new Intent(this, user_interface.class);
				startActivity(intent);
				break;
			
			case R.id.fab2:
				anim();
				Intent intent2 = new Intent(this, admin_interface.class);
				startActivity(intent2);
				break;
			
			case R.id.fab3:
				fixMode = true;
				anim();
				break;
			
			case R.id.dialogSend:
				if (sendable) {
					sendMsg = dialogInput.getText().toString();
					dialogInput.setText("");
					httpTask = new HttpAsyncTask(MainActivity.this);
					String ip = "https://114.71.220.20:8001/bytecelldialog";
					Log.i("@@@", "Target IP: " + ip);
					httpTask.execute(ip, "dialog");
				} else
					Toast.makeText(MainActivity.this, "???????????? ????????? ????????? ???????????? ???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
				break;
		}
	}
	
	void anim() {
		if (isOpen) {
			fab.setImageResource(R.drawable.setting);
			fab1.startAnimation(fab_close);
			fab2.startAnimation(fab_close);
			fab3.startAnimation(fab_close);
			fab1.setClickable(false);
			fab2.setClickable(false);
			fab3.setClickable(false);
			isOpen = false;
		} else {
			fab.setImageResource(R.drawable.cancel);
			fab1.startAnimation(fab_open);
			fab2.startAnimation(fab_open);
			fab3.startAnimation(fab_open);
			fab1.setClickable(true);
			fab2.setClickable(true);
			fab3.setClickable(true);
			isOpen = true;
		}
	}
	
	class HttpAsyncTask extends AsyncTask<String, Void, String> {
		MainActivity ui;
		String mode;
		
		HttpAsyncTask(MainActivity main) {
			this.ui = main;
		}
		
		@Override
		protected String doInBackground(String... str) {
			mode = str[1];
			return POST(str[0], mode);
		}
		
		@Override
		protected void onPostExecute(String rec) {
			super.onPostExecute(rec);
			strJson = rec;
			AddLog.add(MainActivity.this, "REC", rec);
			Log.i("@@@", "RECEIVED: " + strJson);
			ui.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						JsonParser parser = new JsonParser();
						JsonObject json = (JsonObject) parser.parse(strJson);
						
						Log.i("@@@", mode);
						
						if (mode.equals("onCreate")) {
							if (json.has("mr")) {
								if (json.getAsJsonObject("mr").get("error").toString().replaceAll("\"", "").contains("ERRORv-")) {
									JsonObject versions = json.getAsJsonObject("mr").getAsJsonObject("versions");
									int[] ver = checkVersions(versions);
									Log.i("@@@", "version: " + ver[0] + ver[1] + ver[2]);
									if (ver[1] == 1) {
										if (savemap(json.getAsJsonObject("mr").getAsJsonObject("map").toString())) {
											editor.putString("version_map", versions.get("version_map").toString().replaceAll("\"", ""));
											editor.apply();
										}
									}
								}
								/*if (ver[2] == 1) {
									if (saveIdentifier(json.getAsJsonObject("mr"))) {
										editor.putString("version_location_identifier", versions.get("version_location_identifier").toString().replaceAll("\"", ""));
										editor.apply();
									}
								}*/
							}
						} else if (mode.equals("locate")) {
							if (strJson.isEmpty() || strJson.contains("ERROR") || strJson.contains("error")) {
								Tres.setText("Error");
								return;
							}
							if (json.has("mr")) {
								if (json.getAsJsonObject("mr").has("users")) {
									JsonObject userLocation = (JsonObject) ((JsonObject) json.getAsJsonObject("mr").getAsJsonArray("users").get(0))
											.getAsJsonArray("location").get(0);
									Tres.setText(userLocation.get("map").toString() + "\"3/0\""); // userLocation.get("tile").toString());
									String[] tile = userLocation.get("tile").toString().replace("\"", "").split("/");
									// mUserPos = new int[]{Integer.parseInt(tile[0]), Integer.parseInt(tile[1])};
									mUserPos = new int[] {3,0};
									String[] phoenixLocation = ((JsonObject) json.getAsJsonObject("mr").getAsJsonArray("systems").get(1))
											.getAsJsonObject("location").get("tile").toString().replace("\"", "").split("/");
									mPhoenixPos = new int[]{Integer.parseInt(phoenixLocation[0]), Integer.parseInt(phoenixLocation[1])};
								}
							}
						} else if (mode.equals("dialog")) {
							if (json.has("resp")) {
								resp = ((JsonObject) (json.getAsJsonObject("resp").getAsJsonObject("client_actions")
										.getAsJsonArray("vision").get(0)))
										.getAsJsonArray("object").get(0).toString();
								Log.i("@@@", "Dialog: " + resp);
								dialogTime = System.currentTimeMillis();
								dialogHandler.sendEmptyMessage(0);
								if (json.getAsJsonObject("resp").getAsJsonObject("client_actions")
										.getAsJsonArray("body").size() != 0) {
									action = ((JsonObject) (json.getAsJsonObject("resp").getAsJsonObject("client_actions")
											.getAsJsonArray("body").get(0)))
											.getAsJsonArray("object").get(0).toString();
									if (action.equals("arm1") || action.equals("arm2"))
										Log.i("@@@", "ARM");
										Glide.with(MainActivity.this).load(R.drawable.character).into(ivTarget);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	String POST(String url, String mode) {
		String result = "";
		InputStream is = null;
		
		try {
			initHttps();
			URL urlCon = new URL(url);
			HttpsURLConnection httpsCon = (HttpsURLConnection) urlCon.openConnection();
			httpsCon.setHostnameVerifier(DO_NOT_VERIFY);
			HttpURLConnection httpCon = httpsCon;
			//		HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();
			String json = "";
			WifiInfo info = wm.getConnectionInfo();
			int ipAddress = info.getIpAddress();
			String myip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
			JsonObject jsonObject = new JsonObject();
			if (mode.equals("dialog")) {
				jsonObject.addProperty("requestType", "dialog");
				jsonObject.addProperty("userID", "admin");
				jsonObject.addProperty("IP", myip);
				jsonObject.addProperty("rawText", sendMsg);
			} else {
				jsonObject.addProperty("requestType", sp.getString("requestType", ""));
				jsonObject.addProperty("userID", sp.getString("userID", ""));
				jsonObject.addProperty("passwd", sp.getString("passwd", ""));
				jsonObject.addProperty("IP", myip);
				jsonObject.addProperty("version_app", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
				jsonObject.addProperty("version_map", sp.getString("version_map", ""));
				jsonObject.addProperty("version_location_identifier", sp.getString("version_location_identifier", "0.1"));
				
				if (sp.getString("requestType", "").equals("mr_user")) {
					if (mode.equals("locate")) jsonObject.add("user_location", wifiFingerprint);
					JsonObject dir = new JsonObject();
					dir.addProperty("yaw", (int) mYaw);
					dir.addProperty("pitch", (int) mPitch);
					dir.addProperty("roll", (int) mRoll);
					jsonObject.add("user_direction", dir);
					jsonObject.addProperty("user_action", "");
				}
			}
			json = jsonObject.toString();
			Log.i("@@@", "SEND: " + json);
			AddLog.add(MainActivity.this, "SEND", json);
			
			// httpCon.setRequestProperty("Accept", "application/json");
			httpCon.setRequestProperty("Content-type", "application/json");
			httpCon.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
			httpCon.setRequestProperty("Accept", "*/*");
			httpCon.setDoOutput(true);
			httpCon.setDoInput(true);
			
			OutputStream os = httpCon.getOutputStream();
			os.write(json.getBytes("utf-8"));
			os.flush();
			
			int status = httpCon.getResponseCode();
			try {
				if (status != HttpURLConnection.HTTP_OK) is = httpCon.getErrorStream();
				else is = httpCon.getInputStream();
				if (is != null) result = convertInputStreamToString(is);
				else result = "Did not work!";
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				httpCon.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null) {
			result += line;
		}
		inputStream.close();
		return result;
	}
	
	void toonPosition(int[] userPos, Double[] userDirt, int[] toonPos, Double[] toonDirt) {
		if (userPos == null || toonPos == null) return;
		
		ViewGroup.LayoutParams params = phoenix.getLayoutParams();
		
		float sizeX = PHOENIX_DEFAULT_SIZE_X;
		float sizeY = PHOENIX_DEFAULT_SIZE_Y;
		
		//	if (mSizeX!=-1 && mSizeY!=-1) {
		//		params.width = mSizeX;
		//		params.height = mSizeY;
		//	} else
		double firstDist = Math.sqrt((userPos[0] - toonPos[0]) * (userPos[0] - toonPos[0]) + (userPos[1] - toonPos[1]) * (userPos[1] - toonPos[1])) - 3;
		double dist = firstDist;
		// Log.i("@@@", String.format("dist: %s", dist));
		// Log.i("@@@", String.format("toon: [%d/%d], user: [%d/%d]", toonPos[0], toonPos[1], userPos[0], userPos[1]));
		if (dist > 0) {
			while (dist > 0.1) {
				sizeX *= 0.98;
				sizeY *= 0.98;
				dist -= 0.1;
			}
		} else if (dist < 0) {
			while (dist < -0.1) {
				sizeX *= 1.02;
				sizeY *= 1.02;
				dist += 0.1;
			}
		}
		params.width = Math.round(sizeX);
		params.height = Math.round(sizeY);
		
		// ????????? ?????? ????????? ?????? ??????
		double fixedYaw = fixed_default_yaw + Math.toDegrees(Math.atan2(toonPos[1] - userPos[1], userPos[0] - toonPos[0]));
		if (toonPos[0] == userPos[0] && toonPos[1] == userPos[1]) fixedYaw = ex_fixed_yaw;
		ex_fixed_yaw = fixedYaw;
		while (fixedYaw <= 0 || fixedYaw > 360) {
			fixedYaw %= 360;
			fixedYaw += 360;
		}
		Log.i("@@@", ""+fixedYaw);
		// Log.i("@@@", String.format("Default yaw: %s, Fixed yaw: %s", fixed_default_yaw, fixedYaw));
		
		// ????????? ?????? ????????? ?????? ??????
		// float centerPosX = (float) (DEFAULT_POS_X + (fixedYaw - userDirt[0]) * 25);
		// float centerPosY = (float) (DEFAULT_POS_Y + (userDirt[2] - DEFAULT_ROLL) * 32);
		float centerPosX = (float) ((fixedYaw - userDirt[0]) * 25);
		float centerPosY = (float) (DEFAULT_POS_Y + (userDirt[2] - DEFAULT_ROLL) * 31);
		
		Log.i("@@@", String.format("X: %s, Y: %s, UserDirt: %s, %s", centerPosX, centerPosY, userDirt[0], userDirt[1]));
		
		phoenix.setLayoutParams(params);
		sendable = false;
		// Log.i("@@@", centerPosX + "," + centerPosY);
		if (centerPosX < 0 || centerPosX > 1080 || centerPosY < 0 || centerPosY > 1920) {
			phoenix.setVisibility(View.INVISIBLE);
			displayPos = null;
		} else {
			if (firstDist <= 3) sendable = true;
			phoenix.setVisibility(View.VISIBLE);
			displayPos = new float[]{centerPosX, centerPosY - params.height / 2};
			showDialog(resp, dialogVisible);
			phoenix.setX(centerPosX - params.width / 2);
			phoenix.setY(centerPosY - params.height / 2);
		}
	}
	
	void textPosition(int[] userPos, Double[] userDirt) {
		if (userPos == null || userDirt == null) return;
		
		int leftOrRight;
		if (fixed_default_yaw - 180 < 0) {
			if (fixed_default_yaw < userDirt[0] && userDirt[0] < fixed_default_yaw + 180)
				leftOrRight = 1;
			else leftOrRight = 0;
		} else {
			if (fixed_default_yaw - 180 < userDirt[0] && userDirt[0] < fixed_default_yaw)
				leftOrRight = 0;
			else leftOrRight = 1;
		}
		if (rooms[userPos[0]][leftOrRight].isEmpty()) {
			room.setVisibility(View.INVISIBLE);
		} else {
			room.setText(rooms[userPos[0]][leftOrRight]);
			room.setVisibility(View.VISIBLE);
		}
		
		double fixedYaw;
		if (leftOrRight == 0) {
			fixedYaw = fixed_default_yaw - 90;
			if (fixedYaw < 0) fixedYaw += 360;
		} else {
			fixedYaw = fixed_default_yaw + 90;
			if (fixedYaw > 360) fixedYaw -= 360;
		}
		
		float centerPosX = (float) (DEFAULT_POS_X + (fixedYaw - userDirt[0]) * 25);
		float centerPosY = (float) (DEFAULT_POS_Y + (userDirt[2] - DEFAULT_ROLL) * 32);
		
		if (centerPosX < 0 || centerPosX > 1080 || centerPosY < 0 || centerPosY > 1920) {
			room.setVisibility(View.INVISIBLE);
		} else {
			room.setX(centerPosX - ROOM_DEFAULT_SIZE_X / 2);
			room.setY(centerPosY - ROOM_DEFAULT_SIZE_Y / 2);
		}
	}
	
	void showDialog(String msg, boolean visible) {
		if (displayPos == null) return;
		
		float centerPosX = displayPos[0];
		float centerPosY = displayPos[1];
		
		dialogMsg.setText(msg);
		
		if (visible) dialogMsg.setVisibility(View.VISIBLE);
		else dialogMsg.setVisibility(View.INVISIBLE);
		dialogMsg.setX(centerPosX - DIALOG_DEFAULT_SIZE_X / 2);
		dialogMsg.setY(centerPosY - DIALOG_DEFAULT_SIZE_Y);
	}
	
	Handler dialogHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			if (dialogTime == 0) return false;
			if (System.currentTimeMillis() - dialogTime < DIALOG_SHOW_TIME) {
				dialogVisible = true;
				dialogHandler.sendEmptyMessage(0);
			} else {
				dialogVisible = false;
				dialogMsg.setVisibility(View.INVISIBLE);
				dialogHandler.removeMessages(0);
			}
			return false;
		}
	});
	
	boolean chkInfo() {
		if (sp.getString("requestType", "").isEmpty() || sp.getString("ip", "").isEmpty() ||
				sp.getString("userID", "").isEmpty() || sp.getString("passwd", "").isEmpty()) {
			return false;
		}
		return true;
	}
	
	boolean savemap(String msg) {
		String FILE_NAME = "map.json";
		File file = new File(this.getFilesDir(), FILE_NAME);
		
		try {
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.write(msg);
			bw.close();
			Log.i("@@@", "Saved at " + file.getPath());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("@@@", "Cannot save at " + file.getPath());
		}
		return false;
	}
	
	void initHttps() {
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			
			}
			
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[]{};
			}
		}};
		
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	int[] checkVersions(JsonObject vers) throws PackageManager.NameNotFoundException {
		StringBuffer txt = new StringBuffer();
		int[] ret = new int[3];
		if (!vers.get("version_app").toString().replaceAll("\"", "").equals(getPackageManager().getPackageInfo(getPackageName(), 0).versionName)) {
			txt.append("??????");
			ret[0] = 1;
			Log.i("@@@", "version_app: " + vers.get("version_app").toString().replaceAll("\"", "") + ", "
					+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} else ret[0] = 0;
		if (!vers.get("version_map").toString().replaceAll("\"", "").equals(sp.getString("version_map", ""))) {
			if (txt.length() == 0) txt.append("??????");
			else txt.append(", ??????");
			ret[1] = 1;
			Log.i("@@@", "version_map: " + vers.get("version_map").toString().replaceAll("\"", "") + ", "
					+ sp.getString("version_map", "0.1"));
		} else ret[1] = 0;
		if (!vers.get("version_location_identifier").toString().replaceAll("\"", "").equals(sp.getString("version_location_identifier", "0.1"))) {
			if (txt.length() == 0) txt.append("?????????");
			else txt.append(", ?????????");
			ret[2] = 1;
			Log.i("@@@", "version_location_identifier: " + vers.get("version_location_identifier").toString().replaceAll("\"", "") + ", "
					+ sp.getString("version_location_identifier", "0.1"));
		} else ret[2] = 0;
		if (txt.length() != 0) {
			txt.append("??? ????????? ????????????.");
			Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
		}
		return ret;
	}

	/* Decision Tree
	boolean saveIdentifier(JsonObject identifier){
		String FILE_NAME = "identifier.md";
		File file = new File(this.getFilesDir(), FILE_NAME);

		try{
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fileWriter);
			String tmp = identifier.get("location_identifier").toString();
			bw.write(tmp.substring(1, tmp.length()-1).replaceAll("\\\\n","\n").replaceAll("\\\\t", "\t").replaceAll("\\\\\"", "\""));
			bw.close();
			Log.i("@@@", "Saved at " + file.getPath());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("@@@", "Cannot save at " + file.getPath());
		}
		return false;
	}
	*/
	
	void inputVoice(final EditText dialog) {
		try {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
			final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
			stt.setRecognitionListener(new RecognitionListener() {
				@Override
				public void onReadyForSpeech(Bundle bundle) {
					Toast.makeText(MainActivity.this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onBeginningOfSpeech() {
				
				}
				
				@Override
				public void onRmsChanged(float v) {
				
				}
				
				@Override
				public void onBufferReceived(byte[] bytes) {
				
				}
				
				@Override
				public void onEndOfSpeech() {
				
				}
				
				@Override
				public void onError(int i) {
					Toast.makeText(MainActivity.this, "??????: " + i, Toast.LENGTH_SHORT).show();
					stt.destroy();
				}
				
				@Override
				public void onResults(Bundle bundle) {
					ArrayList<String> result = (ArrayList<String>) bundle.get(SpeechRecognizer.RESULTS_RECOGNITION);
					dialog.setText(result.get(0));
					stt.destroy();
				}
				
				@Override
				public void onPartialResults(Bundle bundle) {
				
				}
				
				@Override
				public void onEvent(int i, Bundle bundle) {
				
				}
			});
			stt.startListening(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
