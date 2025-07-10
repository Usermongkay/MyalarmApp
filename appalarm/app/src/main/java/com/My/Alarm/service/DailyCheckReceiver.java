package com.My.Alarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.My.Alarm.AlarmScheduler;
import com.My.Alarm.AlarmSettingsActivity; // Pastikan di-import
import com.My.Alarm.data.DbHelper;
import java.util.Calendar;

public class DailyCheckReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DailyCheck", "Pengecek harian berjalan...");

        // AMBIL JAM PENGINGAT DARI PENGATURAN
        SharedPreferences prefs = context.getSharedPreferences(
            AlarmSettingsActivity.PREFS_NAME, // Gunakan konstanta yang sama
            Context.MODE_PRIVATE
        );
        int jamPengingat = prefs.getInt(AlarmSettingsActivity.HOUR_KEY, 7); // Default 07:00
        int menitPengingat = prefs.getInt(AlarmSettingsActivity.MINUTE_KEY, 0); // Default 00

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
            DbHelper.TABLE_PESANAN,
            null,
            DbHelper.COLUMN_STATUS + " =?",
            new String[]{"aktif"},
            null, null, null
        );

        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_ID));
                String teks = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_TEKS));
                long waktuPesanan = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_WAKTU));
                int hMinus = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_H_MINUS));
                String audioPath = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_AUDIO_PATH));

                // ========== PERUBAHAN PENTING DI BAWAH INI ========== //
                
                // 1. ALARM H- (jika hMinus > 0)
                if (hMinus > 0) {
                    Calendar calHMinus = Calendar.getInstance();
                    calHMinus.setTimeInMillis(waktuPesanan);
                    calHMinus.add(Calendar.DAY_OF_YEAR, -hMinus); // Mundur H-hari
                    
                    // SET JAM SESUAI PENGATURAN (bukan jam alarm utama)
                    calHMinus.set(Calendar.HOUR_OF_DAY, jamPengingat);
                    calHMinus.set(Calendar.MINUTE, menitPengingat);
                    calHMinus.set(Calendar.SECOND, 0);
                    
                    if (isToday(calHMinus)) {
                        String teksPengingat = "Pengingat H-" + hMinus + ": " + teks;
                        AlarmScheduler.setAlarm(context, id, calHMinus.getTimeInMillis(), teksPengingat, audioPath);
                        Log.d("AlarmH-", "Disetel: " + calHMinus.getTime());
                    }
                }

                // 2. ALARM UTAMA (Hari-H)
                Calendar calHariH = Calendar.getInstance();
                calHariH.setTimeInMillis(waktuPesanan);
                
                if (isToday(calHariH)) {
                    AlarmScheduler.setAlarm(context, id, calHariH.getTimeInMillis(), teks, audioPath);
                    Log.d("AlarmHariH", "Disetel: " + calHariH.getTime());
                }
            }
        } finally {
            cursor.close();
            db.close();
        }
    }

    private boolean isToday(Calendar targetCal) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR);
    }
}
