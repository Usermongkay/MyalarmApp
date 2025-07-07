package com.My.Alarm.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.My.Alarm.R; // R besar adalah referensi ke resource
import com.My.Alarm.data.DbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PesananAdapter extends RecyclerView.Adapter<PesananAdapter.PesananViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public PesananAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    // ViewHolder untuk menyimpan referensi ke view di setiap item
    public class PesananViewHolder extends RecyclerView.ViewHolder {
        public TextView pesananText;
        public TextView tanggalText;

        public PesananViewHolder(@NonNull View itemView) {
            super(itemView);
            pesananText = itemView.findViewById(R.id.text_view_pesanan);
            tanggalText = itemView.findViewById(R.id.text_view_tanggal);
        }
    }

    @NonNull
    @Override
    public PesananViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Membuat view baru dari layout list_item.xml
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new PesananViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PesananViewHolder holder, int position) {
        // Metode ini dipanggil untuk setiap item dalam daftar
        if (!mCursor.moveToPosition(position)) {
            return; // Jika cursor tidak bisa pindah ke posisi, hentikan
        }

        // Ambil data dari cursor dengan validasi
        int textColumnIndex = mCursor.getColumnIndex(DbHelper.COLUMN_TEXT);
        int datetimeColumnIndex = mCursor.getColumnIndex(DbHelper.COLUMN_DATETIME);

        String teksPesanan = textColumnIndex != -1 ? mCursor.getString(textColumnIndex) : "";
        long waktuMillis = datetimeColumnIndex != -1 ? mCursor.getLong(datetimeColumnIndex) : 0;
        }
        // Format timestamp (long) menjadi tanggal dan jam yang mudah dibaca
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));
        String tanggalFormatted = formatter.format(new Date(waktuMillis));

        // Set data ke TextViews
        holder.pesananText.setText(teksPesanan);
        holder.tanggalText.setText(tanggalFormatted);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    // Metode untuk menukar cursor lama dengan yang baru (saat data diupdate)
    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
}


