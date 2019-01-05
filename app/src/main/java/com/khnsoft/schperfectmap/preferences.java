package com.khnsoft.schperfectmap;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class preferences extends AppCompatActivity {
    EditText ip_edit;
    EditText port_edit;
    CheckBox direction;
    Button saveSettings;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        ip_edit = findViewById(R.id.ip_edit);
        port_edit = findViewById(R.id.port_edit);
        direction = findViewById(R.id.direction);
        saveSettings = findViewById(R.id.saveSettings);

        sp = getSharedPreferences("settings", MODE_PRIVATE);
        editor = sp.edit();

        ip_edit.setText(sp.getString("ip", ""));
        port_edit.setText(sp.getString("port", ""));
        direction.setChecked(sp.getBoolean("direction", true));

        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("ip", ip_edit.getText().toString());
                editor.putString("port", port_edit.getText().toString());
                editor.putBoolean("direction", direction.isChecked());
                editor.apply();
                Toast.makeText(preferences.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
