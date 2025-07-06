package com.My.Alarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Logika untuk menyetel ulang semua alarm akan kita tambahkan di sini nanti.
            // Untuk sekarang, kita hanya perlu membuktikan pada sistem bahwa receiver ini ada dan berfungsi.
            Log.d("BootReceiver", "HP selesai booting, aplikasi alarm siap.");
        }
    }
}

