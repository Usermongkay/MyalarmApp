package com.My.Alarm.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pengingatku.db";
    private static final int DATABASE_VERSION = 2; // Naikkan versi karena ada perubahan struktur

    // Nama tabel utama kita
    public static final String TABLE_PESANAN = "pesanan";

    // Kolom-kolom sesuai rancangan baru Anda
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEKS = "teks_pesanan";
    public static final String COLUMN_WAKTU = "waktu_pesanan"; // Disimpan sebagai long (timestamp)
    public static final String COLUMN_STATUS = "status"; // "aktif" atau "riwayat"
    public static final String COLUMN_H_MINUS = "reminder_h_minus"; // Angka H-
    public static final String COLUMN_AUDIO_PATH = "path_audio";

    // Perintah SQL untuk membuat tabel baru
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_PESANAN + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TEKS + " TEXT NOT NULL, " +
                    COLUMN_WAKTU + " INTEGER NOT NULL, " +
                    COLUMN_STATUS + " TEXT NOT NULL, " +
                    COLUMN_H_MINUS + " INTEGER NOT NULL, " +
                    COLUMN_AUDIO_PATH + " TEXT);";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Cara paling simpel untuk upgrade: hapus tabel lama, buat yang baru
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PESANAN);
        onCreate(db);
    }
}

