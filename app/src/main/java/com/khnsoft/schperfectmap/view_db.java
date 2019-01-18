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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

public class view_db extends AppCompatActivity {
    TextView DBname;
    LinearLayout DBlist;
    SharedPreferences sp;
    SQLiteDatabase db;
    String SQL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_db);

        sp = getSharedPreferences("settings", MODE_PRIVATE);
        db = openOrCreateDatabase("MAP_DATA", MODE_PRIVATE, null);

        DBname = findViewById(R.id.DBname);
        DBlist = findViewById(R.id.DBlist);
        refresh();
    }

    void refresh(){
        String ver = sp.getString("version_map", "0.1");
        String name = "VERSION_" + ver.replaceAll("\\.", "_");
        DBname.setText(name);
        DBlist.removeAllViews();
        SQL = "SELECT * FROM " + name + " ORDER BY CAST(row_num AS INTEGER), CAST(col_num AS INTEGER)";
        Log.i("@@@", "rawQuery: " + SQL);
        Cursor outCursor = db.rawQuery(SQL, null);
        if (outCursor != null && outCursor.getCount() != 0) {
            int recordCount = outCursor.getCount();
            for (int i=0; i<recordCount; i++) {
                outCursor.moveToNext();
                int id = outCursor.getInt(0);
                String row = outCursor.getString(1);
                String col = outCursor.getString(2);
                String mac = outCursor.getString(3);
                String level = outCursor.getString(4);

                db_item inner = new db_item(this);
                DBlist.addView(inner);

                TextView Trow = findViewById(R.id.row);
                TextView Tcol = findViewById(R.id.col);
                TextView Tmac = findViewById(R.id.mac);
                TextView Tlevel = findViewById(R.id.signal);

                Trow.setText(row);
                Tcol.setText(col);
                Tmac.setText(mac);
                Tlevel.setText(level);

                TagInfo tag = new TagInfo(row, col);
                inner.setTag(tag);
                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final View v2 = v;
                        TagInfo tag = (TagInfo) v.getTag();
                        AlertDialog.Builder builder = new AlertDialog.Builder(view_db.this);
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
                                        refresh();
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

                int innerid = getResources().getIdentifier("inner_" + i, "id", getApplicationContext().getPackageName());
                int rowid = getResources().getIdentifier("row_" + i, "id", getApplicationContext().getPackageName());
                int colid = getResources().getIdentifier("col_" + i, "id", getApplicationContext().getPackageName());
                int macid = getResources().getIdentifier("mac_" + i, "id", getApplicationContext().getPackageName());
                int levelid = getResources().getIdentifier("signal_" + i, "id", getApplicationContext().getPackageName());

                inner.setId(innerid);
                Trow.setId(rowid);
                Tcol.setId(colid);
                Tmac.setId(macid);
                Tlevel.setId(levelid);
            }
        }
    }
}
