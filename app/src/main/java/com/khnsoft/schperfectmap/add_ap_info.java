package com.khnsoft.schperfectmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

public class add_ap_info extends AppCompatActivity {
    WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ap_info);

        if (!wm.isWifiEnabled() && wm.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
            wifiAlert();
        }


    }

    void wifiAlert(){
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

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> scanResults = wm.getScanResults();
                for (int i=0; i<scanResults.size(); i++) {
                    scanResults.get(i);
                }
            }
        }
    };
}