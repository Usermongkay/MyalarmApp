package com.My.Alarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Metode ini dipanggil oleh AlarmManager saat waktunya tiba

        // Ambil data yang kita kirim dari EditorActivity
        String teksPesanan = intent.getStringExtra("TEKS_PESANAN");
        String audioPath = intent.getStringExtra("AUDIO_PATH");

        // Buat intent baru untuk memulai TtsService
        Intent serviceIntent = new Intent(context, TtsService.class);
        serviceIntent.putExtra("TEKS_PESANAN", teksPesanan);
        serviceIntent.putExtra("AUDIO_PATH", audioPath);

        // Mulai service di latar belakang
        context.startService(serviceIntent);
    }
}


