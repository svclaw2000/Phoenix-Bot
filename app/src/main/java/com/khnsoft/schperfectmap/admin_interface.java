package com.khnsoft.schperfectmap;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class admin_interface extends AppCompatActivity implements View.OnClickListener {

    Button record;
    String strJson;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    WifiManager wm;
    SQLiteDatabase db;
    String SQL;
    Button checkDB;
    HttpAsyncTask httpTask;
    Button sendToServer;
    boolean sending;
    ScrollView vScroll;
    HorizontalScrollView hScroll;
    TextView status;
    Button viewLog;

    boolean touching;
    float mx, my;
    float curX, curY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_interface);

        record = findViewById(R.id.record_AP);
        record.setOnClickListener(this);

        checkDB = findViewById(R.id.viewDB);
        checkDB.setOnClickListener(this);

        sendToServer = findViewById(R.id.sendToServer);
        sendToServer.setOnClickListener(this);

        viewLog = findViewById(R.id.viewLOG);
        viewLog.setOnClickListener(this);

        status = findViewById(R.id.status);
        status.setBackgroundColor(Color.GRAY);
        status.setText("Waiting");

        sending = false;
        sp = getSharedPreferences("settings", MODE_PRIVATE);
        editor = sp.edit();
        wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        db = openOrCreateDatabase("MAP_DATA.db", MODE_PRIVATE, null);

        vScroll = (ScrollView) findViewById(R.id.vScroll);
        hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);

        if (!chkInfo()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("허가되지 않음")
                    .setMessage("AP Finger Print 저장 및 전송에 필요한 계정유형, 주소, 아이디, 비밀번호 정보가 부족합니다. 환경설정에서 설정 후 다시 시도해주세요.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            if (!sending) {
                httpTask = new HttpAsyncTask(admin_interface.this);
                String ip = "https://" + sp.getString("ip", "");
                Log.i("@@@", "Target IP: " + ip);
                httpTask.execute(ip, "onCreate");
            }
        }

        touching = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mx = event.getX();
                my = event.getY();
                touching = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!touching) {
                    mx = event.getX();
                    my = event.getY();
                } else {
                    curX = event.getX();
                    curY = event.getY();
                    vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                    hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                    mx = curX;
                    my = curY;
                }
                touching = true;
                break;
            case MotionEvent.ACTION_UP:
                mx = event.getX();
                my = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                touching = false;
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_AP:
                if (DBready()) {
                    Intent intent = new Intent(this, add_ap_info.class);
                    startActivity(intent);
                }break;
            case R.id.sendToServer:
                if (!sending) {
                    sendAlert();
                } else {
                    Toast.makeText(admin_interface.this, "전송중인 메시지가 있습니다. 잠시 후 시도해주세요.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.viewDB:
                if (DBready()) {
                    Intent intent = new Intent(this, view_db.class);
                    startActivity(intent);
                }
                break;
            case R.id.viewLOG:
                AlertDialog.Builder builder = new AlertDialog.Builder(admin_interface.this);
                builder.setTitle("로그를 내보내거나 새로운 로그를 만드시겠습니까?")
                        .setMessage("이메일로 로그를 내보내시겠습니까? 제거를 누르시면 이전 로그를 제거하고 새로운 로그를 생성합니다.")
                        .setCancelable(true)
                        .setPositiveButton("이메일", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendMail();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNeutralButton("제거", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String FILE_NAME = "Log.log";
                                File file = new File(getFilesDir(), FILE_NAME);
                                try {
                                    FileWriter fileWriter = new FileWriter(file);
                                    BufferedWriter bw = new BufferedWriter(fileWriter);
                                    bw.write("Log File\n\n");
                                    bw.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
        }
    }

    void sendMail() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        String FILE_NAME = "Log.log";
        File file = new File(getFilesDir(), FILE_NAME);
        Intent mail = new Intent(Intent.ACTION_SEND);
        mail.setType("plain/text");
        Uri uri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) uri = FileProvider.getUriForFile(admin_interface.this, getApplicationContext().getPackageName() + ".fileprovider", file);
        else uri = Uri.fromFile(file);
        String[] address = {"svclaw2000@gmail.com"};
        mail.putExtra(Intent.EXTRA_EMAIL, address);
        mail.putExtra(Intent.EXTRA_SUBJECT, sdf.format(date) + " Log File");
        mail.putExtra(Intent.EXTRA_STREAM, uri);
        mail.putExtra(Intent.EXTRA_TEXT, "The Log File.");
        startActivity(mail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chkInfo()) show();
    }

    void sendAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("전송")
                .setMessage("지금까지 기록한 AP 지도를 서버에 전송하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        httpTask = new HttpAsyncTask(admin_interface.this);
                        String ip = "https://" + sp.getString("ip", "");
                        Log.i("@@@", "Target IP: " + ip);
                        httpTask.execute(ip, "sendToServer");
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    class HttpAsyncTask extends AsyncTask<String, Void, String> {
        admin_interface ai;

        HttpAsyncTask(admin_interface adminInterface) {
            this.ai = adminInterface;
        }

        @Override
        protected String doInBackground(String... str) {
            sending = true;
            status.setBackgroundColor(Color.YELLOW);
            status.setText("Sending");
            return POST(str[0], str[1]);
        }

        @Override
        protected void onPostExecute(String rec) {
            super.onPostExecute(rec);
            strJson = rec;
            AddLog.add(admin_interface.this, "REC", rec);
            Log.i("@@@", "RECEIVED: " + strJson);
            ai.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (strJson.isEmpty()) {
                        status.setBackgroundColor(Color.RED);
                        status.setText("Error");
                        return;
                    }
                    status.setBackgroundColor(Color.GREEN);
                    status.setText("Received");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            status.setBackgroundColor(Color.GRAY);
                            status.setText("Waiting");
                        }
                    }, 1500);
                    try {
                        JsonParser parser = new JsonParser();
                        JsonObject json = (JsonObject) parser.parse(strJson);

                        // 맵이 정상 업로드 되면 mr 없음
                        if (json.has("mr")) {
                            if (json.getAsJsonObject("mr").get("error").toString().replaceAll("\"", "").contains("ERRORv-")) {
                                JsonObject versions = json.getAsJsonObject("mr").getAsJsonObject("versions");
                                int[] ver = checkVersions(versions);
                                Log.i("@@@", "version: " + ver[0] + ver[1] + ver[2]);
                                if (ver[1] == 1) {
                                    if (savemap(json.getAsJsonObject("mr").getAsJsonObject("map").toString())) {
                                        editor.putString("version_map", versions.get("version_map").toString().replaceAll("\"", ""));
                                        editor.putString("version_location_identifier", versions.get("version_location_identifier").toString().replaceAll("\"", ""));
                                        editor.apply();
                                        return;
                                    }
                                }
                                if (ver[2] == 1) {
                                    saveIdentifier(json.getAsJsonObject("mr"));
                                }
                            } // else if (json.getAsJsonObject("mr").get("error").toString().replaceAll("\"", "").contains("ERRORadmin1")) { }
                        }
                        try {
                            JsonObject map = (JsonObject) parser.parse(getmap());
                            chkDBver(map);
                        } catch (Exception e) {
                            e.printStackTrace();
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
            HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();
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
            if (mode.equals("sendToServer")) {
                JsonObject map = new JsonObject();
                String ver = sp.getString("version_map", "0.1");
                String name = "VERSION_" + ver.replaceAll("\\.", "_");
                SQL = "SELECT _id, row_num, col_num, mac, CAST(level AS INTEGER), count FROM " + name + " ORDER BY CAST(row_num AS INTEGER), CAST(col_num AS INTEGER), count";
                Log.i("@@@", "rawQuery: " + SQL);
                Cursor outCursor = db.rawQuery(SQL, null);
                String row = "";
                String col = "";
                int count=-1;
                if (outCursor != null && outCursor.getCount() != 0) {
                    int recordCount = outCursor.getCount();
                    JsonArray tilelist = new JsonArray();
                    JsonObject tile = new JsonObject();
                    for (int i=0; i<recordCount; i++) {
                        outCursor.moveToNext();
                        if (i==0) {
                            row = outCursor.getString(1);
                            col = outCursor.getString(2);
                            count = outCursor.getInt(5);
                        }
                        if (!row.equals(outCursor.getString(1)) || !col.equals(outCursor.getString(2))) { // 다음 타일
                            tilelist.add(tile);
                            tile = new JsonObject();
                            map.add(row+"/"+col, tilelist);
                            tilelist = new JsonArray();
                            row = outCursor.getString(1);
                            col = outCursor.getString(2);
                            count = outCursor.getInt(5);
                        }
                        if (count!=outCursor.getInt(5)) { // 다음 카운트
                            tilelist.add(tile);
                            tile = new JsonObject();
                            count = outCursor.getInt(5);
                        }
                        if (outCursor.getString(3) == null) continue;
                        tile.addProperty(outCursor.getString(3), outCursor.getInt(4));
                    }
                    tilelist.add(tile);
                    tile = new JsonObject();
                    map.add(row+"/"+col, tilelist);
                }
                Log.i("@@@", map.toString());
                JsonParser parser = new JsonParser();
                JsonObject sendMap = (JsonObject) parser.parse(getmap());
                JsonObject maps = sendMap.getAsJsonObject("maps");
                JsonObject ML_3floor_bigdata = maps.getAsJsonObject("ML_3floor_bigdata");
                ML_3floor_bigdata.remove("tile");
                ML_3floor_bigdata.add("tile", map);
                maps.remove("ML_3floor_bigdata");
                maps.add("ML_3floor_bigdata", ML_3floor_bigdata);
                sendMap.remove("maps");
                sendMap.add("maps", maps);
                jsonObject.add("map", sendMap);
            }

            json = jsonObject.toString();
            Log.i("@@@", "SEND: " + json);
            AddLog.add(admin_interface.this, "SEND", json);

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
            status.setBackgroundColor(Color.RED);
            status.setText("Error");
        }

        sending = false;
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

    String getmap(){
        String FILE_NAME = "map.json";
        File file = new File(getFilesDir(), FILE_NAME);
        StringBuffer output = new StringBuffer();

        try {
            FileReader fileReader = new FileReader(file.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fileReader);
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
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

    //DB의 지도 버전 확인 및 업데이트
    void chkDBver(JsonObject map) {
        String ver = sp.getString("version_map", "0.1");
        String name = "VERSION_" + ver.replaceAll("\\.", "_");
        try {
            SQL = "SELECT * FROM " + name + " ORDER BY CAST(row_num AS INTEGER), CAST(col_num AS INTEGER)";
            Log.i("@@@", "rawQuery: " + SQL);
            Cursor out = db.rawQuery(SQL, null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                SQL = "SELECT name FROM sqlite_master WHERE type='table' and name LIKE 'VERSION%'";
                Log.i("@@@", "rawQuery: " + SQL);
                Cursor c = db.rawQuery(SQL, null);
                int recordCount;
                if (c != null && (recordCount = c.getCount()) != 0) {
                    for (int i=0; i<recordCount; i++) {
                        c.moveToNext();
                        SQL = "DROP TABLE " + c.getString(0);
                        Log.i("@@@", "execSQL: " + SQL);
                        db.execSQL(SQL);
                        AddLog.add(this, "DB", "Drop DB " + c.getString(0));
                    }
                }
                AddLog.add(this, "DB", "Clear all DB");

                SQL = "CREATE TABLE " + name + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "row_num TEXT," +
                        "col_num TEXT," +
                        "mac TEXT," +
                        "level TEXT," +
                        "count INTEGER)";
                Log.i("@@@", "execSQL: " + SQL);
                db.execSQL(SQL);
                AddLog.add(this, "DB", "Create DB " + name);

                JsonObject tiles = map.getAsJsonObject("maps")
                        .getAsJsonObject("ML_3floor_bigdata")
                        .getAsJsonObject("tile");
                Object[] keys = tiles.keySet().toArray();

                for (Object s : keys) {
                    String[] spl = s.toString().split("/");
                    SQL = String.format("INSERT INTO %s (row_num, col_num, count) VALUES (\"%s\", \"%s\", %d)", name, spl[0], spl[1], 0);
                    Log.i("@@@", "execSQL: " + SQL);
                    db.execSQL(SQL);
                }
                Log.i("@@@", "No such table. Created table: " + name);
                show();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    boolean DBready() {
        String ver = sp.getString("version_map", "0.1");
        String name = "VERSION_" + ver.replaceAll("\\.", "_");
        try {
            SQL = "SELECT * FROM " + name;
            Log.i("@@@", "rawQuery: " + SQL);
            db.rawQuery(SQL, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean chkInfo(){
        if (sp.getString("requestType", "").isEmpty() || sp.getString("ip", "").isEmpty() ||
                sp.getString("userID", "").isEmpty() || sp.getString("passwd", "").isEmpty() ||
                sp.getString("requestType", "").equals("mr_user")) {
            return false;
        }
        return true;
    }

    void show() {
        try {
            String ver = sp.getString("version_map", "0.1");
            String name = "VERSION_" + ver.replaceAll("\\.", "_");
            SQL = "SELECT * FROM " + name + " ORDER BY CAST(row_num AS INTEGER), CAST(col_num AS INTEGER)";
            Log.i("@@@", "rawQuery: " + SQL);
            Cursor out = db.rawQuery(SQL, null);
            if (out != null && out.getCount() != 0) {
                int recordCount = out.getCount();
                String row = "-1";
                String col = "-1";
                LinearLayout show = findViewById(R.id.show);
                show.removeAllViews();
                map_row mrow = null;
                LinearLayout lrow = null;
                map_col mcol = null;
                TextView map_item = null;
                int tmp=0;
                for (int i = 0; i < recordCount; i++) {
                    out.moveToNext();
                    if (row.equals(out.getString(1)) && col.equals(out.getString(2))) continue;
                    if (!row.equals(out.getString(1))) {
                        mrow = new map_row(this);
                        show.addView(mrow);
                        lrow = findViewById(R.id.map_row);
                        int rowid = getResources().getIdentifier("lrow_" + i, "id", getApplicationContext().getPackageName());
                        lrow.setId(rowid);
                        tmp = 0;
                    }

                    while (Integer.parseInt(out.getString(2))!=tmp) {
                        mcol = new map_col(this);
                        lrow.addView(mcol);
                        map_item = findViewById(R.id.cross);
                        int colid = getResources().getIdentifier("lcol_" + i, "id", getApplicationContext().getPackageName());
                        map_item.setId(colid);
                        row = out.getString(1);
                        col = out.getString(2);
                        tmp ++;
                    }
                    mcol = new map_col(this);
                    lrow.addView(mcol);
                    map_item = findViewById(R.id.cross);
                    int colid = getResources().getIdentifier("lcol_" + i, "id", getApplicationContext().getPackageName());
                    map_item.setId(colid);
                    row = out.getString(1);
                    col = out.getString(2);
                    map_item.setText(out.getString(1) + "/" + out.getString(2));
                    if (out.getString(3) == null) {
                        map_item.setBackgroundColor(Color.RED);
                    } else {
                        map_item.setBackgroundColor(Color.GREEN);
                    }
                    TagInfo tag = new TagInfo(row, col);
                    map_item.setTag(tag);
                    map_item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TagInfo tag = (TagInfo) v.getTag();
                            Intent intent = new Intent(admin_interface.this, add_ap_info.class);
                            intent.putExtra("row", tag.row);
                            intent.putExtra("col", tag.col);
                            startActivity(intent);
                        }
                    });
                    map_item.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final View v2 = v;
                            TagInfo tag = (TagInfo) v.getTag();
                            AlertDialog.Builder builder = new AlertDialog.Builder(admin_interface.this);
                            builder.setTitle("삭제")
                                    .setMessage("(" + tag.row + "," + tag.col + ")에 등록된 AP 정보를 지우시겠습니까?")
                                    .setCancelable(true)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            TagInfo tag = (TagInfo) v2.getTag();
                                            String ver = sp.getString("version_map", "0.1");
                                            String name = "VERSION_" + ver.replaceAll("\\.", "_");
                                            SQL = String.format("DELETE FROM %s WHERE row_num=%s and col_num=%s", name, tag.row, tag.col);
                                            Log.i("@@@", "execSQL: " + SQL);
                                            db.execSQL(SQL);
                                            SQL = String.format("INSERT INTO %s (row_num, col_num) VALUES (%s, %s)", name, tag.row, tag.col);
                                            Log.i("@@@", "execSQL: " + SQL);
                                            db.execSQL(SQL);
                                            AddLog.add(admin_interface.this, "DB", String.format("(%s,%s) AP info remove from %s", tag.row, tag.col, name));
                                            show();
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();

                            return false;
                        }
                    });
                    tmp ++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "DB에 AP 정보가 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    void saveIdentifier(JsonObject identifier){
        String FILE_NAME = "identifier.md";
        File file = new File(this.getFilesDir(), FILE_NAME);

        try{
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(identifier.get("location_identifier").toString());
            bw.close();
            Log.i("@@@", "Saved at " + file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("@@@", "Cannot save at " + file.getPath());
        }
    }
}
