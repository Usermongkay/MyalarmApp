package com.My.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import com.My.Alarm.service.AlarmReceiver;

public class AlarmScheduler {

    public static void setAlarm(Context context, long uniqueId, long triggerTime, String teksPesanan, String audioPath) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("AlarmScheduler", "AlarmManager tidak ditemukan.");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        // Tambahkan ID alarm ke intent agar bisa digunakan oleh receiver
        intent.putExtra("ALARM_ID", uniqueId);
        intent.putExtra("TEKS_PESANAN", teksPesanan);
        intent.putExtra("AUDIO_PATH", audioPath);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) uniqueId, // ID harus unik, kita cast dari long
            intent,
            Pending_Intent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Periksa izin untuk Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Beri tahu pengguna jika izin tidak ada
                Toast.makeText(context, "Izin untuk menyetel alarm presisi tidak diberikan.", Toast.LENGTH_LONG).show();
                // Arahkan pengguna ke pengaturan untuk memberikan izin
                Intent settingsIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(settingsIntent);
                return; // Hentikan proses karena izin tidak ada
            }
        }
        
        // Setel alarm
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        Log.d("AlarmScheduler", "Alarm disetel untuk ID " + uniqueId);
    }

    public static void cancelAlarm(Context context, long uniqueId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) uniqueId,
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d("AlarmScheduler", "Alarm dengan ID " + uniqueId + " dibatalkan.");
        }
    }
}
