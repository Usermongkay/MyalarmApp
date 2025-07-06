package com.My.Alarm;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class TesSuaraActivity extends AppCompatActivity {

    private TextToSpeech mTTS;
    private EditText mEditText;
    private Button mButtonTes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tes_suara);

        mEditText = findViewById(R.id.edit_text_tes);
        mButtonTes = findViewById(R.id.btn_tes_suara);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int status) {
					if (status == TextToSpeech.SUCCESS) {
						int result = mTTS.setLanguage(new Locale("id", "ID"));
						if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
							Log.e("TTS", "Bahasa Indonesia tidak didukung.");
						}
					} else {
						Log.e("TTS", "Inisialisasi TTS Gagal!");
					}
				}
			});

        mButtonTes.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					speak();
				}
			});
    }

    private void speak() {
        String text = mEditText.getText().toString();
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
	
	@Override
	public void onPointerCaptureChanged(boolean hasCapture) {
		super.onPointerCaptureChanged(hasCapture);
		
	}
}

