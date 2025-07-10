package com.My.Alarm;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import com.My.Alarm.data.DbHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class EditorActivity extends AppCompatActivity {

    private EditText mEditTextPesanan;
    private Button mBtnPilihTanggal, mBtnPilihJam, mBtnSimpan;

    private Calendar mCalendar;
    private SQLiteDatabase mDatabase;
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        DbHelper dbHelper = new DbHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        mEditTextPesanan = findViewById(R.id.edit_text_pesanan);
        mBtnPilihTanggal = findViewById(R.id.btn_pilih_tanggal);
        mBtnPilihJam = findViewById(R.id.btn_pilih_jam);
        mBtnSimpan = findViewById(R.id.btn_simpan);

        mCalendar = Calendar.getInstance();
        
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(new Locale("id", "ID"));
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Bahasa Indonesia tidak didukung.");
                    }
                } else {
                    Log.e("TTS", "Inisialisasi TTS Gagal!");
                }
            }
        });

        mBtnPilihTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
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
                savePesananKeDatabase();
            }
        });

        updateButtonsText();
    }

    private void savePesananKeDatabase() {
        String teksPesanan = mEditTextPesanan.getText().toString().trim();
        if (teksPesanan.isEmpty()) {
            Toast.makeText(this, "Teks pesanan tidak boleh kosong.", Toast.LENGTH_SHORT).show();
        return;
    }

    // Ambil pengaturan H- dari SharedPreferences
    SharedPreferences prefs = getSharedPreferences(AlarmSettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
    int hMinus = prefs.getInt(AlarmSettingsActivity.H_MINUS_KEY, 1); // Default H-1

    // ... (kode untuk membuat file audio tetap sama) ...
    SimpleDateFormat formatterTanggal = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
    String tanggalFormatted = formatterTanggal.format(mCalendar.getTime());
    String teksLengkapUntukTTS = teksPesanan + ". Pada tanggal " + tanggalFormatted;

    String audioFileName = "pesanan_" + UUID.randomUUID().toString() + ".mp3";
    File audioFile = new File(getExternalFilesDir(null), audioFileName);
    String audioPath = audioFile.getAbsolutePath();

    int result = mTTS.synthesizeToFile(teksLengkapUntukTTS, null, audioFile, "pesananAudio");

    if (result != TextToSpeech.SUCCESS) {
        Toast.makeText(this, "Gagal membuat file audio.", Toast.LENGTH_SHORT).show();
        return;
    }
    // ... (akhir dari kode audio) ...

    ContentValues cv = new ContentValues();
    cv.put(DbHelper.COLUMN_TEKS, teksPesanan);
    cv.put(DbHelper.COLUMN_WAKTU, mCalendar.getTimeInMillis()); // Tetap simpan waktu asli (Hari-H)
    cv.put(DbHelper.COLUMN_STATUS, "aktif");
    cv.put(DbHelper.COLUMN_H_MINUS, hMinus);
    cv.put(DbHelper.COLUMN_AUDIO_PATH, audioPath);

    long newRowId = mDatabase.insert(DbHelper.TABLE_PESANAN, null, cv);

    if (newRowId != -1) {
        // ---- INI LOGIKA BARU YANG BENAR ----
        // Buat kalender baru untuk waktu alarm H-
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(mCalendar.getTimeInMillis()); // Mulai dari waktu Hari-H
        alarmTime.add(Calendar.DAY_OF_YEAR, -hMinus); // Kurangi harinya sesuai pengaturan H-

        // Setel alarm menggunakan waktu H- yang sudah dihitung
        // Kita juga tambahkan "Pengingat: " agar pesannya berbeda
        AlarmScheduler.setAlarm(this, newRowId, alarmTime.getTimeInMillis(), "Pengingat: " + teksPesanan, audioPath);
        // ------------------------------------

        Toast.makeText(this, "Pesanan berhasil disimpan!", Toast.LENGTH_LONG).show();
        finish();
    } else {
        Toast.makeText(this, "Gagal menyimpan pesanan ke database.", Toast.LENGTH_SHORT).show();
    }
}

    // --- Metode showDatePickerDialog, showTimePickerDialog, dan updateButtonsText tetap sama ---
    // --- Tidak perlu ada lagi metode setAlarm() di sini ---

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateButtonsText();
            }
        };
        new DatePickerDialog(this, dateSetListener, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                mCalendar.set(Calendar.SECOND, 0);
                updateButtonsText();
            }
        };
        new TimePickerDialog(this, timeSetListener, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true).show();
    }
    
    private void updateButtonsText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        mBtnPilihTanggal.setText(dateFormat.format(mCalendar.getTime()));
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("id", "ID"));
        mBtnPilihJam.setText(timeFormat.format(mCalendar.getTime()));
    }

    @Override
    protected void onDestroy() {
        if (mTTS!= null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
}

