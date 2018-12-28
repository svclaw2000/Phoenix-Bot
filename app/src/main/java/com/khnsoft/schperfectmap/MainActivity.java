package com.khnsoft.schperfectmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Button mode_user;
    Button mode_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mode_user = findViewById(R.id.mode_user);
        mode_user.setOnClickListener(this);

        mode_admin = findViewById(R.id.mode_admin);
        mode_admin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mode_user:
                Intent UI = new Intent(MainActivity.this, user_interface.class);
                startActivity(UI);
                break;
            case R.id.mode_admin:
                Intent AI = new Intent(MainActivity.this, add_ap_info.class);
                startActivity(AI);
                break;
        }
    }
}
