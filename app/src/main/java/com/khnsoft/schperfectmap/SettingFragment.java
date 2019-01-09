package com.khnsoft.schperfectmap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class SettingFragment extends Fragment {
    EditText ip_edit;
    EditText port_edit;
    CheckBox direction;
    Button saveSettings;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_preferences, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("@@@", "Setting Fragment Called");
        ip_edit = getView().findViewById(R.id.ip_edit);
        direction = getView().findViewById(R.id.direction);
        saveSettings = getView().findViewById(R.id.saveSettings);

        sp = getActivity().getSharedPreferences("settings", MODE_PRIVATE);
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
                Toast.makeText(getActivity(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
