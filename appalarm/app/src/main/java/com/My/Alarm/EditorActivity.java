package com.My.Alarm;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import com.My.Alarm.service.AlarmReceiver; // Import kelas AlarmReceiver kita


import androidx.appcompat.app.AppCompatActivity;

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

        // Inisialisasi Database
        DbHelper dbHelper = new DbHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        // Inisialisasi Views
        mEditTextPesanan = findViewById(R.id.edit_text_pesanan);
        mBtnPilihTanggal = findViewById(R.id.btn_pilih_tanggal);
        mBtnPilihJam = findViewById(R.id.btn_pilih_jam);
        mBtnSimpan = findViewById(R.id.btn_simpan);

        // Inisialisasi Calendar
        mCalendar = Calendar.getInstance();

        // Inisialisasi Text-to-Speech Engine
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int status) {
					if (status == TextToSpeech.SUCCESS) {
						// Set bahasa ke Bahasa Indonesia
						int result = mTTS.setLanguage(new Locale("id", "ID"));
						if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
							Log.e("TTS", "Bahasa Indonesia tidak didukung.");
						}
					} else {
						Log.e("TTS", "Inisialisasi TTS Gagal!");
					}
				}
			});


        // Listener untuk tombol Pilih Tanggal
        mBtnPilihTanggal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDatePickerDialog();
				}
			});

        // Listener untuk tombol Pilih Jam
        mBtnPilihJam.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showTimePickerDialog();
				}
			});

        // Listener untuk tombol Simpan
        mBtnSimpan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					savePesanan();
				}
			});

        updateButtonsText();
    }

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

        new DatePickerDialog(this, dateSetListener,
							 mCalendar.get(Calendar.YEAR),
							 mCalendar.get(Calendar.MONTH),
							 mCalendar.get(Calendar.DAY_OF_MONTH)).show();
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

        new TimePickerDialog(this, timeSetListener,
							 mCalendar.get(Calendar.HOUR_OF_DAY),
							 mCalendar.get(Calendar.MINUTE), true).show();
    }

    private void updateButtonsText() {
        // Update teks tombol tanggal
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        mBtnPilihTanggal.setText(dateFormat.format(mCalendar.getTime()));

        // Update teks tombol jam
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("id", "ID"));
        mBtnPilihJam.setText(timeFormat.format(mCalendar.getTime()));
    }

    private void savePesanan() {
        final String teksPesanan = mEditTextPesanan.getText().toString().trim();

        if (teksPesanan.isEmpty()) {
            Toast.makeText(this, "Teks pesanan tidak boleh kosong.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate nama file audio unik
        final String audioFileName = "pesanan_" + UUID.randomUUID().toString() + ".mp3";
        final File audioFile = new File(getExternalFilesDir(null), audioFileName);
        final String audioPath = audioFile.getAbsolutePath();

        Toast.makeText(this, "Menyimpan dan membuat audio...", Toast.LENGTH_SHORT).show();

        // Simpan teks ke file audio
        int result = mTTS.synthesizeToFile(teksPesanan, null, audioFile, "pesananAudio");

        if (result == TextToSpeech.SUCCESS) {
            // Jika proses pembuatan audio berhasil dimulai
            // Kita akan menunggu sampai selesai, tapi untuk sekarang kita langsung simpan
            // (Untuk AIDE, pendekatan sederhana lebih baik)

            ContentValues cv = new ContentValues();
            cv.put(DbHelper.COLUMN_TEXT, teksPesanan);
            cv.put(DbHelper.COLUMN_DATETIME, mCalendar.getTimeInMillis());
            cv.put(DbHelper.COLUMN_STATUS, "upcoming");
            cv.put(DbHelper.COLUMN_AUDIO_PATH, audioPath);

            long insertResult = mDatabase.insert(DbHelper.TABLE_NAME, null, cv);

            if(insertResult != -1) {
				Toast.makeText(this, "Pesanan berhasil disimpan!", Toast.LENGTH_LONG).show();

				// Panggil metode untuk menyetel alarm
				setAlarm(mCalendar.getTimeInMillis(), teksPesanan, audioPath);

				finish(); // Kembali ke MainActivity
			} else {
				Toast.makeText(this, "Gagal menyimpan pesanan.", Toast.LENGTH_SHORT).show();
			}


        } else {
            Toast.makeText(this, "Gagal membuat file audio.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // Matikan TTS engine saat activity dihancurkan
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

	private void setAlarm(long triggerTime, String teksPesanan, String audioPath) {
		android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
   		Intent intent = new Intent(this, AlarmReceiver.class);
   		intent.putExtra("TEKS_PESANAN", teksPesanan);
    	intent.putExtra("AUDIO_PATH", audioPath);

		// Gunakan request code unik agar setiap alarm berbeda
    	int requestCode = (int) System.currentTimeMillis();
    	// Tambahkan FLAG_IMMUTABLE untuk keamanan di Android versi baru
		android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(this, requestCode, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

		// Set alarm yang presisi
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    	} else {
        	alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    	}

    	Toast.makeText(this, "Alarm berhasil disetel!", Toast.LENGTH_SHORT).show();

		// Set alarm untuk waktu yang dipilih
		setAlarm(mCalendar.getTimeInMillis(), teksPesanan, audioPath);

	}
}

