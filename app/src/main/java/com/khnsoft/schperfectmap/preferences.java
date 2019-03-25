package com.khnsoft.schperfectmap;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class preferences extends AppCompatActivity {
	EditText ip_edit;
	EditText id_edit;
	EditText pw_edit;
	CheckBox direction;
	CheckBox check_user;
	CheckBox check_admin;
	Button saveSettings;
	SharedPreferences sp;
	SharedPreferences.Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		
		check_user = findViewById(R.id.check_user);
		check_admin = findViewById(R.id.check_admin);
		ip_edit = findViewById(R.id.ip_edit);
		id_edit = findViewById(R.id.id_edit);
		pw_edit = findViewById(R.id.pw_edit);
		direction = findViewById(R.id.direction);
		saveSettings = findViewById(R.id.saveSettings);
		
		sp = getSharedPreferences("settings", MODE_PRIVATE);
		editor = sp.edit();
		
		if (sp.getString("requestType", "mr_admin").equals("mr_admin")) check_admin.setChecked(true);
		else if (sp.getString("requestType", "").equals("mr_user")) check_user.setChecked(true);
		
		ip_edit.setText(sp.getString("ip", "114.71.220.20:8001/bytecellmr"));
		id_edit.setText(sp.getString("userID", "admin"));
		pw_edit.setText(sp.getString("passwd", "admin1234"));
		direction.setChecked(sp.getBoolean("direction", true));
		
		check_admin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					check_user.setChecked(false);
				} else {
					if (!check_user.isChecked()) buttonView.setChecked(true);
				}
			}
		});
		
		check_user.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					check_admin.setChecked(false);
				} else {
					if (!check_admin.isChecked()) buttonView.setChecked(true);
				}
			}
		});
		
		saveSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (check_admin.isChecked()) editor.putString("requestType", "mr_admin");
				else if (check_user.isChecked()) editor.putString("requestType", "mr_user");
				else {
					Toast.makeText(preferences.this, "계정 유형을 선택해주세요.", Toast.LENGTH_LONG).show();
					return;
				}
				editor.putString("ip", ip_edit.getText().toString());
				editor.putString("userID", id_edit.getText().toString());
				editor.putString("passwd", pw_edit.getText().toString());
				editor.putBoolean("direction", direction.isChecked());
				editor.apply();
				Toast.makeText(preferences.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}
}
