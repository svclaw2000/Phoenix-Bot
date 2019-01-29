package com.khnsoft.schperfectmap;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class view_log extends AppCompatActivity {
    TextView Tlog;
    ClipboardManager cm;
    Button clip;
    Button newLog;
    Button email;
    ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_log);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        Tlog = findViewById(R.id.Tlog);
        Tlog.setText(getLog());

        clip = findViewById(R.id.clip);
        clip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clipData = ClipData.newPlainText("Log", getLog());
                cm.setPrimaryClip(clipData);
            }
        });

        newLog = findViewById(R.id.newLog);
        newLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view_log.this);
                builder.setTitle("새 로그를 만드시겠습니까?")
                        .setMessage("지금까지의 로그 기록이 삭제됩니다. 계속 하시겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("제거", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createLog();
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

        email = findViewById(R.id.email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });

        scroll = findViewById(R.id.scroll);
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    String getLog(){
        String FILE_NAME = "Log.log";
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

    void createLog() {
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

        Tlog.setText(getLog());
    }

    void sendMail() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        String FILE_NAME = "Log.log";
        File file = new File(getFilesDir(), FILE_NAME);
        Intent mail = new Intent(Intent.ACTION_SEND);
        mail.setType("plain/text");
        Uri uri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) uri = FileProvider.getUriForFile(view_log.this, getApplicationContext().getPackageName() + ".fileprovider", file);
        else uri = Uri.fromFile(file);
        String[] address = {"svclaw2000@gmail.com"};
        mail.putExtra(Intent.EXTRA_EMAIL, address);
        mail.putExtra(Intent.EXTRA_SUBJECT, sdf.format(date) + " Log File");
        mail.putExtra(Intent.EXTRA_STREAM, uri);
        mail.putExtra(Intent.EXTRA_TEXT, "The Log File.");
        startActivity(mail);
    }
}
