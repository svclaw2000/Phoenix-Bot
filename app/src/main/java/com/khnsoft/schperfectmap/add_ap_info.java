package com.khnsoft.schperfectmap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class add_ap_info extends AppCompatActivity {
    WifiManager wm;
    LocationManager lm;
    List<ScanResult> scanResult;
    Button refresh;
    String[] permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int MULTIPLE_PERMISSIONS = 10;
    LinearLayout container;
    int count;
    int check;
    EditText row_edit;
    EditText col_edit;
    Button save_ap;
    String SQL;
    SQLiteDatabase db;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ap_info);

        db = openOrCreateDatabase("MAP_DATA.db", MODE_PRIVATE, null);
        sp = getSharedPreferences("settings", MODE_PRIVATE);

        String ver = sp.getString("version_map", "0.1");
        final String name = "VERSION_" + ver.replaceAll("\\.", "_");

        wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        refresh = findViewById(R.id.refresh);
        save_ap = findViewById(R.id.save_AP);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(add_ap_info.this, "AP정보를 검색하기 위해 위치 서비스를 켜주십시오.", Toast.LENGTH_LONG).show();
                    return;
                }
                wm.startScan();
                count++;
                Log.i("@@@", "Button clicked.");
            }
        });

        count = 0;
        check = 0;
        container = findViewById(R.id.container);

        row_edit = findViewById(R.id.row_num);
        col_edit = findViewById(R.id.col_num);
        save_ap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanResult == null || scanResult.size()==0) {
                    Toast.makeText(add_ap_info.this, "저장할 내용이 없습니다. 새로고침 버튼을 눌러 AP를 스캔해주십시오.", Toast.LENGTH_LONG).show();
                    return;
                }
                String row = row_edit.getText().toString();
                String col = col_edit.getText().toString();
                SQL = String.format("SELECT row_num, col_num FROM %s WHERE row_num=\"%s\" AND col_num=\"%s\"", name, row, col);
                Log.i("@@@", "rawQuery: " + SQL);
                Cursor count = db.rawQuery(SQL, null);
                if (count == null || count.getCount()==0) { // 해당 타일이 존재하는지 확인
                    Toast.makeText(add_ap_info.this, "잘못된 위치를 입력했습니다. 확인 후 다시 시도해주십시오.", Toast.LENGTH_LONG).show();;
                    return;
                }
                count.close();
                SQL = String.format("SELECT row_num, col_num, count FROM %s WHERE row_num=\"%s\" AND col_num=\"%s\"", name, row, col);
                Log.i("@@@", "rawQuery: " + SQL);
                count = db.rawQuery(SQL, null);
                int numcount = -1;
                if (count != null && count.getCount()!=0) {
                    for (int i=0; i<count.getCount(); i++) {
                        count.moveToNext();
                        if (numcount < count.getInt(2)) numcount = count.getInt(2);
                    }
                }
                if (numcount == 0) {
                    SQL = String.format("DELETE FROM %s WHERE row_num=\"%s\" AND col_num=\"%s\"", name, row, col);
                    Log.i("@@@", "execSQL: " + SQL);
                    db.execSQL(SQL);
                }
                for (int i = 0; i < scanResult.size(); i++) {
                    ScanResult result = scanResult.get(i);
                    SQL = String.format("INSERT INTO %s (row_num, col_num, mac, level, count) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", %d)", name, row, col, result.BSSID, result.level, numcount+1);
                    Log.i("@@@", "execSQL: " + SQL);
                    db.execSQL(SQL);
                }
                Toast.makeText(add_ap_info.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                AddLog.add(add_ap_info.this, "DB", String.format("(%s,%s) AP info save to %s", row, col, name));
                finish();
            }
        });

        Intent intent = getIntent();
        row_edit.setText(intent.getStringExtra("row"));
        col_edit.setText(intent.getStringExtra("col"));

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(add_ap_info.this, "AP정보를 검색하기 위해 위치 서비스를 켜주십시오.", Toast.LENGTH_LONG).show();
        } else {
            wm.startScan();
            count++;
        }
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