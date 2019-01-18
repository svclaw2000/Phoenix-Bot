package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

public class tmp {

}
/*
package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class add_ap_info extends AppCompatActivity implements View.OnClickListener {
    WifiManager wm;
    LinearLayout container;
    Button refresh;
    Button send;
    List<ScanResult> scanResults;
    int count, nowcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ap_info);

        wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        count = 0;
        nowcount = 0;

        if (!wm.isWifiEnabled() && wm.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
            //wifiAlert();
        }

        container = findViewById(R.id.container);
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(this);

        send = findViewById(R.id.send);
        send.setOnClickListener(this);
    }

    void wifiAlert(){ //와이파이 꺼져있을 때 열림
        AlertDialog.Builder askWifi = new AlertDialog.Builder(add_ap_info.this);
        askWifi.setTitle("와이파이를 켜시겠습니까?")
                .setMessage("이 작업은 와이파이가 필요합니다. 확인을 누르시면 와이파이가 켜집니다.")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wm.setWifiEnabled(true);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                scanwifi();
                break;
            case R.id.send:
                //updatelist();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x12345) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.i("@@@", "Permission not granted.");
                    return;
                }
            }
            Log.i("@@@", "Permission granted.");
            scanwifi();
        }
    }

    void scanwifi(){ //와이파이 스캔 명령
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            wm.startScan();
            //Log.i("@@@", String.format("Scanned. count is %d, nowcount is %d", count, nowcount));
        }
    }

    void updatelist() { //스캔 결과 화면으로 보여주기
        if (count != 0 && count != nowcount) {
            container.removeAllViews();
            if (scanResults.size() == 0) {
                no_ap no = new no_ap(getApplicationContext());
                container.addView(no);
            } else {
                for (int i = 0; i < scanResults.size(); i++) {
                    each_ap each = new each_ap(getApplicationContext());

                    container.addView(each);
                    TextView ssid = findViewById(R.id.ssid);
                    ssid.setText(scanResults.get(i).SSID);
                    int ssid_id = getResources().getIdentifier("ssid_" + i, "id", getApplicationContext().getPackageName());
                    ssid.setId(ssid_id);

                    TextView bssid = findViewById(R.id.bssid);
                    bssid.setText(scanResults.get(i).BSSID);
                    int bssid_id = getResources().getIdentifier("bssid_" + i, "id", getApplicationContext().getPackageName());
                    bssid.setId(bssid_id);

                    TextView level = findViewById(R.id.level);
                    level.setText(scanResults.get(i).level + "");
                    int level_id = getResources().getIdentifier("level_" + i, "id", getApplicationContext().getPackageName());
                    level.setId(level_id);

                    Log.i("@@@", String.format("SSID: %s, BSSID: %s, LEVEL: %d", scanResults.get(i).SSID, scanResults.get(i).BSSID, scanResults.get(i).level));
                }
            }
            nowcount = count;
            Log.i("@@@", String.format("Updated. List size is %d.", scanResults.size()));
        } else Log.i("@@@", "Not updated. count = nowcount");
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("@@@", "Receiver called");
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.i("@@@","Received SCAN_RESULTS");
                scanResults = wm.getScanResults();
                container.removeAllViews();
                if (scanResults.size() == 0) {
                    no_ap no = new no_ap(getApplicationContext());
                    container.addView(no);
                } else {
                    for (int i = 0; i < scanResults.size(); i++) {
                        each_ap each = new each_ap(getApplicationContext());

                        container.addView(each);
                        TextView ssid = findViewById(R.id.ssid);
                        ssid.setText(scanResults.get(i).SSID);
                        int ssid_id = getResources().getIdentifier("ssid_" + i, "id", getApplicationContext().getPackageName());
                        ssid.setId(ssid_id);

                        TextView bssid = findViewById(R.id.bssid);
                        bssid.setText(scanResults.get(i).BSSID);
                        int bssid_id = getResources().getIdentifier("bssid_" + i, "id", getApplicationContext().getPackageName());
                        bssid.setId(bssid_id);

                        TextView level = findViewById(R.id.level);
                        level.setText(scanResults.get(i).level + "");
                        int level_id = getResources().getIdentifier("level_" + i, "id", getApplicationContext().getPackageName());
                        level.setId(level_id);

                        Log.i("@@@", String.format("SSID: %s, BSSID: %s, LEVEL: %d", scanResults.get(i).SSID, scanResults.get(i).BSSID, scanResults.get(i).level));
                    }
                }
                Log.i("@@@", String.format("Updated. List size is %d.", scanResults.size()));
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentfilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentfilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(receiver, intentfilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }
}
 */