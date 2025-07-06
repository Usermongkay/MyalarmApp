package com.My.Alarm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.My.Alarm.R; // R besar

import java.io.IOException;

public class TtsService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "AlarmPesananChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Ambil data dari AlarmReceiver
        String teksPesanan = intent.getStringExtra("TEKS_PESANAN");
        String audioPath = intent.getStringExtra("AUDIO_PATH");

        // Buat notifikasi yang akan ditampilkan
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle("Pengingat Pesanan")
			.setContentText(teksPesanan)
			.setSmallIcon(R.drawable.ic_launcher_background) // Ganti dengan ikon notifikasi Anda
			.build();

        // Jalankan service ini di foreground agar tidak mudah dimatikan sistem
        startForeground(NOTIFICATION_ID, notification);

        // Putar audio
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						// Hentikan service setelah audio selesai
						stopSelf();
					}
				});
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf(); // Hentikan service jika ada error
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Buat channel notifikasi (wajib untuk Android 8.0 ke atas)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
				CHANNEL_ID,
				"Alarm Pesanan Service Channel",
				NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}


