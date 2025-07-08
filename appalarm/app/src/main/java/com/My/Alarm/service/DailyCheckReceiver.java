package com.My.Alarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.My.Alarm.AlarmScheduler;
import com.My.Alarm.data.DbHelper;

import java.util.Calendar;

public class DailyCheckReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DailyCheck", "Pengecek harian berjalan...");

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
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_ID));
                String teks = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_TEKS));
                long waktuPesanan = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_WAKTU));
                int hMinus = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_H_MINUS));
                String audioPath = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_AUDIO_PATH));

                // Hitung waktu alarm H-
                Calendar calAlarm = Calendar.getInstance();
                calAlarm.setTimeInMillis(waktuPesanan);
                calAlarm.add(Calendar.DAY_OF_YEAR, -hMinus);

                // Hitung waktu alarm Hari-H
                Calendar calHariH = Calendar.getInstance();
                calHariH.setTimeInMillis(waktuPesanan);

                // Cek apakah alarm H- atau Hari-H jatuh pada hari ini
                if (isToday(calAlarm) || isToday(calHariH)) {
                    // Jika ya, setel alarm yang sesungguhnya
                    if (isToday(calAlarm)) {
                        AlarmScheduler.setAlarm(context, id, calAlarm.getTimeInMillis(), "Pengingat: " + teks, audioPath);
                    }
                    if (isToday(calHariH)) {
                        AlarmScheduler.setAlarm(context, id, calHariH.getTimeInMillis(), teks, audioPath);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    private boolean isToday(Calendar targetCal) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR);
    }
}
