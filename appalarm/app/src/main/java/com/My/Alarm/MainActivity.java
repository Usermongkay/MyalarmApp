package com.My.Alarm;

import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.My.Alarm.adapter.PesananAdapter;
import com.My.Alarm.data.DbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private SQLiteDatabase mDatabase;
    private PesananAdapter mAdapter;
    private RecyclerView recyclerView;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
																 R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        DbHelper dbHelper = new DbHelper(this);
        mDatabase = dbHelper.getReadableDatabase();

        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(MainActivity.this, EditorActivity.class);
					startActivity(intent);
				}
			});
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAlarmPermission();
        loadPesanan();
    }

    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
					.setTitle("Izin Diperlukan")
					.setMessage("Aplikasi ini memerlukan izin untuk menyetel alarm agar dapat berfungsi dengan baik. Ketuk 'Buka Pengaturan' untuk mengaktifkannya.")
					.setPositiveButton("Buka Pengaturan", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
							startActivity(intent);
						}
					})
					.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Pengguna menolak
						}
					})
					.show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_pengaturan_alarm) {
            Intent intent = new Intent(this, AlarmSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_riwayat) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manajemen_data) {
            Toast.makeText(this, "Fitur ini belum dibuat", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_tes_suara) {
            Intent intent = new Intent(this, TesSuaraActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadPesanan() {
        Cursor cursor = mDatabase.query(
			DbHelper.TABLE_NAME,
			null,
			DbHelper.COLUMN_STATUS + " = ?",
			new String[]{"upcoming"},
			null,
			null,
			DbHelper.COLUMN_DATETIME + " ASC"
        );

        if (mAdapter == null) {
            mAdapter = new PesananAdapter(this, cursor);
            recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.swapCursor(cursor);
        }

        if(cursor.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    // Metode ini diperlukan untuk menghindari eror aneh di beberapa versi AIDE
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}

