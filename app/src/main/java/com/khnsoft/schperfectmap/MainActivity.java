package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
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
	
	double mYaw, mPitch, mRoll;
	double mAccPitch, mAccRoll;
	double timestamp;
	double temp;
	float a = 0.2f;
	double dt;
	double RAD2DGR = 180.0 / Math.PI;
	double exts = 0;
	double dt2;
	static final float NS2S = 1.0f/1000000000.0f;
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

	SharedPreferences sp;
	SharedPreferences.Editor editor;
	ImageView fab;
	ImageView fab1;
	ImageView fab2;
	Animation fab_open;
	Animation fab_close;
	Boolean isOpen;

	WifiManager wm;
	List<ScanResult> scanResult;
	String[] permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA};

	String strJson;
	HttpAsyncTask httpTask;

	// realDecisionTree dTree = null;
	// Vector<String> rawAttributes;
	// Vector<Double> rawValues;
	JsonObject wifiFingerprint;
	TextView Tres;
	boolean checking;

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
		yaw = findViewById(R.id.yaw);
		pitch = findViewById(R.id.pitch);
		roll = findViewById(R.id.roll);
		mGyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mAcceSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

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
		fab.setImageResource(R.drawable.setting);
		fab1.setImageResource(R.drawable.setting);
		fab2.setImageResource(R.drawable.record);
		fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
		fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close);
		fab.setOnClickListener(this);
		fab1.setOnClickListener(this);
		fab2.setOnClickListener(this);
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
		// Log.i("@@@", "Start Scan");

		if (!checkPermissions()) {
			finish();
		}

		if (chkInfo()) {
			initHttps();
			httpTask = new HttpAsyncTask(MainActivity.this);
			String ip = "https://" + sp.getString("ip", "");
			Log.i("@@@", "Target IP: " + ip);
			httpTask.execute(ip, "onCreate");
		}

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
		} */
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
					camera = Camera.open();
					camera.setDisplayOrientation(90);
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
		if (!chkInfo()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("허가되지 않음")
					.setMessage("AP Finger Print 저장 및 전송에 필요한 계정유형, 주소, 아이디, 비밀번호 정보가 부족합니다. 환경설정에서 설정 후 다시 시도해주세요.")
					.setCancelable(false)
					.setPositiveButton("확인", new DialogInterface.OnClickListener() {
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

		sm.registerListener(sensorlistener, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(sensorlistener, mAcceSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(sensorlistener, mMagnSensor, SensorManager.SENSOR_DELAY_NORMAL);
		handler.sendEmptyMessage(0);

		wm.startScan();

		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		try{
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
		float[] gData = new float[3];
		float[] mData = new float[3];
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
				//		if (mRoll>=0 && mRoll<90) {
				//			mYaw = mYaw - (mGyroValues[2]*(1-mRoll/90) + mGyroValues[1]*(mRoll/90)) * dt2 * RAD2DGR;
				//			Log.i("@@@", ""+((mGyroValues[2]*(1-mRoll/90) + mGyroValues[1]*(mRoll/90)) * dt2 * RAD2DGR));
				//		} else if (mRoll >= 90 && mRoll < 180) {
				//			mYaw = mYaw - (mGyroValues[2]*(1-mRoll/90) + mGyroValues[1]*(2-mRoll/90)) * dt2 * RAD2DGR;
				//			Log.i("@@@", ""+((mGyroValues[2]*(1-mRoll/90) + mGyroValues[1]*(2-mRoll/90)) * dt2 * RAD2DGR));
				//		}
					}
					break;

				case Sensor.TYPE_ACCELEROMETER:
					mAcceValues = event.values.clone();
					if (!acceRunning) {
						acceRunning = true;
					}
					gData = event.values.clone();
					break;

				case Sensor.TYPE_MAGNETIC_FIELD:
					mData = event.values.clone();

				default:
					return;
			}

			if (SensorManager.getRotationMatrix(rMat, iMat, gData, mData)){
				accYaw = (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
				if (fixMode) {
					Log.i("@@@", ""+fixCount);
					if (fixCount == 0) fixYaw = new double[5];
					if (fixCount < 5) {
						fixYaw[fixCount] = accYaw;
						fixCount++;
					} else {
						double sum = 0;
						for (int i=0; i<5; i++) {
							sum += fixYaw[i];
						}
						mYaw = sum / 5.0;
						lastSyncTime = System.currentTimeMillis();
						fixCount = 0;
						fixMode = false;
					}
				} else if (System.currentTimeMillis()-lastSyncTime>0 && mRoll<20 && mRoll>-20 && mPitch<20 && mPitch>-20) {
					fixMode = true;
				};
			}

			if (gyroRunning && acceRunning){
				complementaty(event.timestamp);
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

		temp = (1/a) * (mAccPitch - mPitch) + mGyroValues[1];
		mPitch = mPitch + (temp*dt);

		temp = (1/a) * (mAccRoll - mRoll) + mGyroValues[0];
		mRoll = mRoll + (temp*dt);
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
					wm.startScan();
					// Log.i("@@@", "Start Scan");
				} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
					context.sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
				}
			}
		}
	};

	public void getWIFIScanResult() {
		scanResult = wm.getScanResults();
		wifiFingerprint = new JsonObject();
		if (scanResult.size()!=0){
			for (int i=0; i<scanResult.size(); i++){
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
		// Log.i("@@@", "ScanResult : " + scanResult.size());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fab:
				anim();
				break;
			case R.id.fab1:
				anim();
			//	Intent intent = new Intent(this, preferences.class);
				Intent intent = new Intent(this, user_interface.class);
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

	class HttpAsyncTask extends AsyncTask<String, Void, String> {
		MainActivity ui;
		HttpAsyncTask(MainActivity main) {
			this.ui = main;
		}

		@Override
		protected String doInBackground(String... str) {
			return POST(str[0], str[1]);
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
						if (strJson.isEmpty() || strJson.contains("ERROR") || strJson.contains("error")) {
							Tres.setText("Error");
							return;
						}
						
						JsonParser parser = new JsonParser();
						JsonObject json = (JsonObject) parser.parse(strJson);

						if (json.has("mr")) {
							if (json.getAsJsonObject("mr").has("error") && json.getAsJsonObject("mr").get("error").toString().replaceAll("\"", "").contains("ERRORv-")) {
								JsonObject versions = json.getAsJsonObject("mr").getAsJsonObject("versions");
								int[] ver = checkVersions(versions);
								Log.i("@@@", "version: " + ver[0] + ver[1] + ver[2]);
								if (ver[1] == 1) {
									if (savemap(json.getAsJsonObject("mr").getAsJsonObject("map").toString())) {
										editor.putString("version_map", versions.get("version_map").toString().replaceAll("\"", ""));
										editor.apply();
									}
								}
								/*if (ver[2] == 1) {
									if (saveIdentifier(json.getAsJsonObject("mr"))) {
										editor.putString("version_location_identifier", versions.get("version_location_identifier").toString().replaceAll("\"", ""));
										editor.apply();
									}
								}*/
							}
							if (json.getAsJsonObject("mr").has("users")) {
								JsonObject userLocation = (JsonObject)((JsonObject) json.getAsJsonObject("mr").getAsJsonArray("users").get(0)).getAsJsonArray("location").get(0);
								Tres.setText(userLocation.get("map").toString() + userLocation.get("tile").toString());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	String POST(String url, String mode){
		String result = "";
		InputStream is = null;

		try {
			URL urlCon = new URL(url);
			HttpsURLConnection httpsCon = (HttpsURLConnection) urlCon.openConnection();
			httpsCon.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			HttpURLConnection httpCon = httpsCon;
			String json = "";
			WifiInfo info = wm.getConnectionInfo();
			int ipAddress = info.getIpAddress();
			String myip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("requestType", sp.getString("requestType", ""));
			jsonObject.addProperty("userID", sp.getString("userID", ""));
			jsonObject.addProperty("passwd", sp.getString("passwd", ""));
			// jsonObject.accumulate("IP", myip);
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

			json = jsonObject.toString();
			Log.i("@@@", "SEND: " + json);
			AddLog.add(MainActivity.this, "SEND", json);

			// httpCon.setRequestProperty("Accept", "application/json");
			httpCon.setRequestProperty("Content-type", "application/json");
			httpCon.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
			httpCon.setRequestProperty("Accept","*/*");
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

	/*
	 * 리턴값은 어떻게 나올지 몰라서 일단 float[] 으로 했고, 필요하신 자료형으로 바꾸셔도 됩니다,
	 * int[] userPos : 사용자 위치 (지도의 [0]X, [1]Y 좌표)
	 * float[] userDirt : 사용자 각도 ([0]좌우각도, [1]상하각도, [2]화면회전각도)
	 *                    일단은 기기에서 구해지는 각도
	 * int[] toonPos : 캐릭터 위치 (지도의 [0]X, [1]Y 좌표)
	 * float[] toonDirt : 캐릭터 각도 ([0]좌우각도 ...) 이 부분은 정해서 알려주시면 수정하겠습니다.
	 */

	float[] toonPosition(int[] userPos, float[] userDirt, int[] toonPos, float[] toonDirt) {

		return null;
	}

	boolean chkInfo(){
		if (sp.getString("requestType", "").isEmpty() || sp.getString("ip", "").isEmpty() ||
				sp.getString("userID", "").isEmpty() || sp.getString("passwd", "").isEmpty()) {
			return false;
		}
		return true;
	}

	boolean savemap(String msg) {
		String FILE_NAME = "map.json";
		File file = new File(this.getFilesDir(), FILE_NAME);

		try{
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
		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
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
			txt.append("어플");
			ret[0] = 1;
			Log.i("@@@", "version_app: " + vers.get("version_app").toString().replaceAll("\"", "") + ", " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} else ret[0] = 0;
		if (!vers.get("version_map").toString().replaceAll("\"", "").equals(sp.getString("version_map", ""))) {
			if (txt.length()==0) txt.append("지도");
			else txt.append(", 지도");
			ret[1] = 1;
			Log.i("@@@", "version_map: " + vers.get("version_map").toString().replaceAll("\"", "") + ", " + sp.getString("version_map", "0.1"));
		} else ret[1] = 0;
		if (!vers.get("version_location_identifier").toString().replaceAll("\"", "").equals(sp.getString("version_location_identifier", "0.1"))) {
			if (txt.length()==0) txt.append("인식기");
			else txt.append(", 인식기");
			ret[2] = 1;
			Log.i("@@@", "version_location_identifier: " + vers.get("version_location_identifier").toString().replaceAll("\"", "") + ", " + sp.getString("version_location_identifier", "0.1"));
		} else ret[2] = 0;
		if (txt.length()!=0) {
			txt.append("의 버전이 낮습니다.");
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

	void calibrate_location(int[] userLoc, float[] gData, float aData) {

	}
}
