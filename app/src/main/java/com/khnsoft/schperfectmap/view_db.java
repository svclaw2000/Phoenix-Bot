package com.khnsoft.schperfectmap;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class view_db extends AppCompatActivity {
    SharedPreferences sp;
    SQLiteDatabase db;
    String SQL;
    TextView dbname;
    TextView row;
    TextView col;
    TextView entered;
    TextView empty;
    TextView total;
    Button resetDB;
    Button emptyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_db);

        sp = getSharedPreferences("settings", MODE_PRIVATE);
        db = openOrCreateDatabase("MAP_DATA", MODE_PRIVATE, null);

        String ver = sp.getString("version_map", "0.1");
        String name = "VERSION_" + ver.replaceAll("\\.", "_");

        dbname = findViewById(R.id.dbname);
        dbname.setText(name);

        row = findViewById(R.id.row);
        col = findViewById(R.id.col);
        entered = findViewById(R.id.entered);
        empty = findViewById(R.id.empty);
        total = findViewById(R.id.total);

        refresh();

        resetDB = findViewById(R.id.resetDB);
        resetDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view_db.this);
                builder.setTitle("DB 리셋")
                        .setMessage("지금까지 DB의 수정사항이 모두 지워집니다. 그래도 지도파일 map.json의 데이터로 DB를 리셋하시겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("리셋", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        emptyDB = findViewById(R.id.emptyDB);
        emptyDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view_db.this);
                builder.setTitle("DB 비우기")
                        .setMessage("지금까지 DB의 수정사항을 지우고 빈 지도 DB를 생성합니다. 계속 하시겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("비우기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetEmpty();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
    }

    void reset(){
        try {
            String ver = sp.getString("version_map", "0.1");
            String name = "VERSION_" + ver.replaceAll("\\.", "_");

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

            JsonParser parser = new JsonParser();
            JsonObject map = (JsonObject) parser.parse(getmap());

            SQL = "CREATE TABLE " + name + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "row_num TEXT," +
                    "col_num TEXT," +
                    "mac TEXT," +
                    "level TEXT)";
            Log.i("@@@", "execSQL: " + SQL);
            db.execSQL(SQL);

            JsonObject tiles = map.getAsJsonObject("maps")
                    .getAsJsonObject("ML_3floor_bigdata")
                    .getAsJsonObject("tile");
            Object[] keys = tiles.keySet().toArray();

            for (Object s : keys) {
                String obj = s.toString();
                Log.i("@@@", obj + " " + tiles.getAsJsonObject(obj).size());
                if (tiles.getAsJsonObject(obj).size() == 0) {
                    String[] spl = obj.split("/");
                    SQL = "INSERT INTO " + name + " (row_num, col_num) VALUES (\"" + spl[0] + "\",\"" + spl[1] + "\")";
                    Log.i("@@@", "execSQL: " + SQL);
                    db.execSQL(SQL);
                } else {
                    JsonObject tile = tiles.getAsJsonObject(obj);
                    Object[] tilekey = tile.keySet().toArray();
                    for (Object o : tilekey) {
                        String mac = o.toString();
                        String[] spl = obj.split("/");
                        String level = tile.get(mac).getAsString();
                        SQL = String.format("INSERT INTO %s (row_num, col_num, mac, level) VALUES (\"%s\", \"%s\", \"%s\", \"%s\")",
                                name, spl[0], spl[1], mac, level);
                        Log.i("@@@", "execSQL: " + SQL);
                        db.execSQL(SQL);
                    }
                }
            }
            Log.i("@@@", "No such table. Created table: " + name);
            AddLog.add(this, "DB", "From map.json reset DB " + name);
            refresh();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    void resetEmpty(){
        try {
            String ver = sp.getString("version_map", "0.1");
            String name = "VERSION_" + ver.replaceAll("\\.", "_");

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

            JsonParser parser = new JsonParser();
            JsonObject map = (JsonObject) parser.parse(getmap());

            SQL = "CREATE TABLE " + name + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "row_num TEXT," +
                    "col_num TEXT," +
                    "mac TEXT," +
                    "level TEXT)";
            Log.i("@@@", "execSQL: " + SQL);
            db.execSQL(SQL);

            Object[] tiles = map.getAsJsonObject("maps")
                    .getAsJsonObject("ML_3floor_bigdata")
                    .getAsJsonObject("tile")
                    .keySet()
                    .toArray();
            for (Object s : tiles) {
                String[] spl = s.toString().split("/");
                SQL = "INSERT INTO " + name + " (row_num, col_num) VALUES (\"" + spl[0] + "\",\"" + spl[1] + "\")";
                Log.i("@@@", "execSQL: " + SQL);
                db.execSQL(SQL);
            }
            Log.i("@@@", "No such table. Created table: " + name);
            AddLog.add(this, "DB", "Create empty map DB " + name);
            refresh();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    String getmap(){
        String FILE_NAME = "map.json";
        File file = new File(this.getFilesDir(), FILE_NAME);
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

    void refresh(){
        String ver = sp.getString("version_map", "0.1");
        String name = "VERSION_" + ver.replaceAll("\\.", "_");
        SQL = "SELECT * FROM " + name + " ORDER BY CAST(row_num AS INTEGER), CAST(col_num AS INTEGER)";
        Cursor outCursor = db.rawQuery(SQL, null);
        int recordCount = -1;
        int rowC = outCursor.getCount();
        int colC = outCursor.getColumnCount();
        int enteredC = 0;
        int emptyC = 0;
        int totalC = 0;
        String x = "-1";
        String y = "-1";
        if (outCursor != null && (recordCount = outCursor.getCount()) != 0) {
            for (int i=0; i<recordCount; i++) {
                outCursor.moveToNext();
                if (x.equals(outCursor.getString(1)) && y.equals(outCursor.getString(2))) continue;
                x = outCursor.getString(1);
                y = outCursor.getString(2);
                if (outCursor.getString(3) == null) emptyC++;
                else enteredC++;
                totalC++;
            }
        }
        row.setText(""+rowC);
        col.setText(""+colC);
        entered.setText(""+enteredC);
        empty.setText(""+emptyC);
        total.setText(""+totalC);
    }
}
