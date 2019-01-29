package com.khnsoft.schperfectmap;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddLog {
    static void add(Context context, String kind, String msg) {
        String FILE_NAME = "Log.log";
        File file = new File(context.getFilesDir(), FILE_NAME);

        try{
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("[yy/MM/dd HH:mm:ss]");
            bw.write(sdf.format(date) + " " + kind + ": " + msg + "\n\n");
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
