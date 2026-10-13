package com.zyvo.notepop.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zyvo.notepop.R;
import com.zyvo.notepop.data.Note;
import com.zyvo.notepop.data.NoteRepository;
import com.zyvo.notepop.databinding.ActivityAlarmPopupBinding;
import com.zyvo.notepop.receiver.AlarmReceiver;
import com.zyvo.notepop.receiver.AlarmSoundService;
import com.zyvo.notepop.viewmodel.NoteViewModel;

import java.util.concurrent.Executors;

public class AlarmPopupActivity extends AppCompatActivity {
    private ActivityAlarmPopupBinding binding;
    private long noteId;
    private NoteRepository repo;
    private Uri selectedSoundUri;
    private String selectedSoundTitle;
    private ActivityResultLauncher<Intent> soundPicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Wake up screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        binding = ActivityAlarmPopupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteId = getIntent().getLongExtra(AlarmReceiver.EXTRA_NOTE_ID, -1);
        repo = new NoteRepository(getApplication());

        // sound picker
        soundPicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    // persist permission if needed
                    final int takeFlags = result.getData().getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception ignored) {
                    }
                    selectedSoundUri = uri;
                    // get display name
                    String title = uri.getLastPathSegment();
                    selectedSoundTitle = title;
                }
            }
        });

        loadNote();
    }

    private void loadNote() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Note n = com.zyvo.notepop.data.NoteDatabase.getInstance(this).noteDao().getNoteById(noteId);
            if (n == null) {
                new Handler(Looper.getMainLooper()).post(this::finish);
                return;
            }
            new Handler(Looper.getMainLooper()).post(() -> bindNote(n));
        });
    }

    private void bindNote(Note n) {
        binding.tvMessage.setText(n.text);
        binding.btnStop.setOnClickListener(v -> {
            // stop sound service and close
            AlarmSoundService.stopService(this);
            // optional: clear alarm flag if one-time? we left as is (user can clear in list)
            finish();
        });
    }
}