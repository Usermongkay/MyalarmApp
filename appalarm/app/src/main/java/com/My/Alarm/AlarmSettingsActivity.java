package com.My.Alarm;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

// Pastikan nama kelas ini sama persis dengan nama file
public class AlarmSettingsActivity extends AppCompatActivity {

    private TextView mTextViewHMinus;
    private Button mBtnPilihJam, mBtnSimpan, mBtnKurang, mBtnTambah;

    private int hMinusValue = 1;
    private int alarmHour = 7;
    private int alarmMinute = 0;

    public static final String PREFS_NAME = "AlarmSettings";
    public static final String H_MINUS_KEY = "HMinusValue";
    public static final String HOUR_KEY = "AlarmHour";
    public static final String MINUTE_KEY = "AlarmMinute";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_settings);

        // Tambahkan judul pada Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pengaturan Alarm");
        }

        mTextViewHMinus = findViewById(R.id.text_view_h_minus);
        mBtnKurang = findViewById(R.id.btn_h_minus_kurang);
        mBtnTambah = findViewById(R.id.btn_h_minus_tambah);
        mBtnPilihJam = findViewById(R.id.btn_pilih_jam_alarm);
        mBtnSimpan = findViewById(R.id.btn_simpan_pengaturan);

        loadSettings(); // Muat pengaturan yang tersimpan

        mBtnKurang.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (hMinusValue > 0) {
						hMinusValue--;
						updateHMinusText();
					}
				}
			});

        mBtnTambah.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (hMinusValue < 30) { // Batasan maksimal 30 hari
						hMinusValue++;
						updateHMinusText();
					} else {
						Toast.makeText(AlarmSettingsActivity.this, "Maksimal 30 hari", Toast.LENGTH_SHORT).show();
					}
				}
			});

        mBtnPilihJam.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showTimePickerDialog();
				}
			});

        mBtnSimpan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveSettings();
				}
			});
    }

    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                alarmHour = hourOfDay;
                alarmMinute = minute;
                updateJamButtonText();
            }
        };

        new TimePickerDialog(this, timeSetListener, alarmHour, alarmMinute, true).show();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        hMinusValue = prefs.getInt(H_MINUS_KEY, 1); // Default 1
        alarmHour = prefs.getInt(HOUR_KEY, 7); // Default 07
        alarmMinute = prefs.getInt(MINUTE_KEY, 0); // Default :00

        updateHMinusText();
        updateJamButtonText();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(H_MINUS_KEY, hMinusValue);
        editor.putInt(HOUR_KEY, alarmHour);
        editor.putInt(MINUTE_KEY, alarmMinute);
        editor.apply();

        Toast.makeText(this, "Pengaturan disimpan!", Toast.LENGTH_SHORT).show();
        finish(); // Tutup halaman setelah menyimpan
    }

    private void updateHMinusText() {
        mTextViewHMinus.setText(String.valueOf(hMinusValue));
    }



    private void updateJamButtonText() {
        mBtnPilihJam.setText(String.format(Locale.getDefault(), "%02d:%02d", alarmHour, alarmMinute));
    }
}

