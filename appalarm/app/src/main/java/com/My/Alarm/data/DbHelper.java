package com.My.Alarm.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    // Nama file database
    private static final String DATABASE_NAME = "pengingatku.db";

    // Versi database. Naikkan angkanya jika Anda mengubah struktur tabel.
    private static final int DATABASE_VERSION = 1;

    // Nama tabel
    public static final String TABLE_NAME = "pesanan";

    // Nama-nama kolom
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "teks";
    public static final String COLUMN_DATETIME = "waktu"; // Disimpan sebagai long (timestamp)
    public static final String COLUMN_STATUS = "status"; // 'upcoming' atau 'history'
    public static final String COLUMN_COMPLETED_AT = "waktu_selesai"; // Timestamp kapan ditandai selesai
    public static final String COLUMN_AUDIO_PATH = "path_audio";

    // Perintah SQL untuk membuat tabel
    private static final String SQL_CREATE_TABLE =
	"CREATE TABLE " + TABLE_NAME + " (" +
	COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	COLUMN_TEXT + " TEXT NOT NULL, " +
	COLUMN_DATETIME + " INTEGER NOT NULL, " +
	COLUMN_STATUS + " TEXT NOT NULL, " +
	COLUMN_COMPLETED_AT + " INTEGER, " +
	COLUMN_AUDIO_PATH + " TEXT);";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Metode ini hanya dipanggil sekali saat database pertama kali dibuat
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Logika untuk upgrade database (jika versi berubah)
        // Untuk simpelnya, kita hapus tabel lama dan buat yang baru
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}


