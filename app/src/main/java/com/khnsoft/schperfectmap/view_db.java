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
import android.widget.LinearLayout;
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
    LinearLayout dblist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_db);

        sp = getSharedPreferences("settings", MODE_PRIVATE);
        db = openOrCreateDatabase("MAP_DATA.db", MODE_PRIVATE, null);

        String ver = sp.getString("version_map", "0.1");
        String name = "VERSION_" + ver.replaceAll("\\.", "_");

        dbname = findViewById(R.id.dbname);
        dbname.setText(name);

        row = findViewById(R.id.row);
        col = findViewById(R.id.col);
        entered = findViewById(R.id.entered);
        empty = findViewById(R.id.empty);
        total = findViewById(R.id.total);
        dblist = findViewById(R.id.dblist);

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
                    "level TEXT," +
                    "count INTEGER)";
            Log.i("@@@", "execSQL: " + SQL);
            db.execSQL(SQL);

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
                    "level TEXT," +
                    "count INTEGER)";
            Log.i("@@@", "execSQL: " + SQL);
            db.execSQL(SQL);

            Object[] tiles = map.getAsJsonObject("maps")
                    .getAsJsonObject("ML_3floor_bigdata")
                    .getAsJsonObject("tile")
                    .keySet()
                    .toArray();
            for (Object s : tiles) {
                String[] spl = s.toString().split("/");
                SQL = String.format("INSERT INTO %s (row_num, col_num, count) VALUES (\"%s\", \"%s\", %d)", name, spl[0], spl[1], 0);
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
        SQL = "SELECT _id, row_num, col_num, mac, count FROM " + name + " ORDER BY CAST(row_num AS INTEGER), CAST(col_num AS INTEGER), count";
        Log.i("@@@", "rawQuery:" + SQL);
        Cursor outCursor = db.rawQuery(SQL, null);
        int recordCount = -1;
        int rowC = outCursor.getCount();
        int colC = outCursor.getColumnCount();
        int enteredC = 0;
        int emptyC = 0;
        int totalC = 0;
        String x = "-1";
        String y = "-1";
        db_item item = null;
        TextView numTile = null;
        TextView numCount = null;
        TextView numRow = null;
        LinearLayout container = null;
        int tileId;
        int countId;
        int rowId;
        int containerId;
        int count = -1;
        String r = "";
        String c = "";
        int countRow=0;
        dblist.removeAllViews();
        if (outCursor != null && (recordCount = outCursor.getCount()) != 0) {
            for (int i=0; i<recordCount; i++) {
                outCursor.moveToNext();
                if (!x.equals(outCursor.getString(1)) || !y.equals(outCursor.getString(2))) {
                    x = outCursor.getString(1);
                    y = outCursor.getString(2);
                    if (outCursor.getString(3) == null) emptyC++;
                    else enteredC++;
                    totalC++;
                }

                if (!r.equals(outCursor.getString(1)) || !c.equals(outCursor.getString(2))) {
                    if (i!=0) {
                        numRow.setText(""+countRow);
                        numCount.setText(""+count);
                    }

                    item = new db_item(this);
                    dblist.addView(item);
                    container = findViewById(R.id.container);
                    numTile = findViewById(R.id.numTile);
                    numCount = findViewById(R.id.numCount);
                    numRow = findViewById(R.id.numRow);
                    containerId = getResources().getIdentifier("container_" + i, "id", getApplicationContext().getPackageName());
                    tileId = getResources().getIdentifier("numTile_" + i, "id", getApplicationContext().getPackageName());
                    countId = getResources().getIdentifier("numCount" + i, "id", getApplicationContext().getPackageName());
                    rowId = getResources().getIdentifier("numRow" + i, "id", getApplicationContext().getPackageName());
                    container.setId(containerId);
                    numTile.setId(tileId);
                    numCount.setId(countId);
                    numRow.setId(rowId);

                    r = outCursor.getString(1);
                    c = outCursor.getString(2);
                    numTile.setText(r+" / "+c);
                    count = outCursor.getInt(4);
                    countRow = 0;
                    TagInfo tag = new TagInfo(r, c);
                    container.setTag(tag);

                    container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final TagInfo tag = (TagInfo) v.getTag();
                            AlertDialog.Builder builder = new AlertDialog.Builder(view_db.this);
                            builder.setTitle("타일 초기화")
                                    .setMessage(String.format("%s/%s 타일의 AP 정보를 초기화 하시겠습니까?", tag.row, tag.col))
                                    .setCancelable(true)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String ver = sp.getString("version_map", "0.1");
                                            String name = "VERSION_" + ver.replaceAll("\\.", "_");
                                            SQL = String.format("DELETE FROM %s WHERE row_num=%s AND col_num=%s", name, tag.row, tag.col);
                                            Log.i("@@@", "execSQL: " + SQL);
                                            db.execSQL(SQL);
                                            SQL = String.format("INSERT INTO %s (row_num, col_num, count) VALUES (\"%s\", \"%s\", %d)", name, tag.row, tag.col, 0);
                                            Log.i("@@@", "execSQL: " + SQL);
                                            db.execSQL(SQL);
                                            AddLog.add(view_db.this, "DB", String.format("Reset %s/%s", tag.row, tag.col));
                                            refresh();
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { }
                                    })
                                    .show();
                        }
                    });
                }
                if (count<outCursor.getInt(4)) count = outCursor.getInt(4);
                countRow++;
            }
            numRow.setText(""+countRow);
            numCount.setText(""+count);
        }
        row.setText(""+rowC);
        col.setText(""+colC);
        entered.setText(""+enteredC);
        empty.setText(""+emptyC);
        total.setText(""+totalC);
    }
}
