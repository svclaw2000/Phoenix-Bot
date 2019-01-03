package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class add_ap_info extends AppCompatActivity {
    WifiManager wm;
    List<ScanResult> scanResult;
    Button refresh;
    String[] permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int MULTIPLE_PERMISSIONS = 10;
    LinearLayout container;
    int count;
    int check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ap_info);

        wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.startScan();
                count++;
                Log.i("@@@", "Button clicked.");
            }
        });

        count = 0;
        check = 0;
        container = findViewById(R.id.container);
    }

    @Override
    protected void onResume() {
        if (wm != null) {
            if (!wm.isWifiEnabled() && wm.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                wm.setWifiEnabled(true);
            }
            final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(receiver, filter);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermissions()) finish();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    if (check != count)
                    getWIFIScanResult();
                    check = count;
                } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    context.sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
                }
            }
        }
    };

    public void getWIFIScanResult() {
        scanResult = wm.getScanResults();
        container.removeAllViews();
        if (scanResult.size()==0) {
            no_ap no_one = new no_ap(add_ap_info.this);
            container.addView(no_one);
        } else {
            for (int i = 0; i < scanResult.size(); i++) {
                ScanResult result = scanResult.get(i);
                Log.i("@@@", result.SSID + ": " + result.level + ", " + result.BSSID);

                each_ap each = new each_ap(add_ap_info.this);
                container.addView(each);
                TextView ssid = findViewById(R.id.ssid);
                TextView bssid = findViewById(R.id.bssid);
                TextView level = findViewById(R.id.level);

                if (result.SSID.length() > 15) ssid.setText(result.SSID.substring(0, 12)+"...");
                else ssid.setText(result.SSID);
                bssid.setText(result.BSSID);
                level.setText("" + result.level);

                int id_ssid = getResources().getIdentifier("ssid_"+i, "id", getApplicationContext().getPackageName());
                int id_bssid = getResources().getIdentifier("bssid_"+i, "id", getApplicationContext().getPackageName());
                int id_level = getResources().getIdentifier("level_"+i, "id", getApplicationContext().getPackageName());

                ssid.setId(id_ssid);
                bssid.setId(id_bssid);
                level.setId(id_level);
            }
        }
    }

    boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(add_ap_info.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(add_ap_info.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}