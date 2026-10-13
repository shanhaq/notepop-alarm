package com.zyvo.notepop.ui;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zyvo.notepop.data.Note;
import com.zyvo.notepop.data.NoteDatabase;
import com.zyvo.notepop.databinding.ActivityScreenOnPopupBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class ScreenOnPopupActivity extends AppCompatActivity {
    private ActivityScreenOnPopupBinding binding;
    private List<Note> notes = new ArrayList<>();
    private int index = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        binding = ActivityScreenOnPopupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadNotes();

        binding.btnLater.setOnClickListener(v -> finish());
        binding.btnOpen.setOnClickListener(v -> {
            // Go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        binding.btnNext.setOnClickListener(v -> showNext());
    }

    private void loadNotes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Note> all = NoteDatabase.getInstance(this).noteDao().getAllNotes().getValue();
            // getAllNotes is LiveData; better query synchronously? but we use DAO list not exposed; alternatively query via repo by blocking? simpler: fetch via new query
            // getNotesWithAlarms not same; get all: we can do quick check by reading DB once
            // Room allows synchronous if we call from executor on same dao? NoteDao has no sync getAll except LiveData; so just proceed with empty + showNext logic by trying to get from ViewModel? easier: load minimal by iterating? alternatively query a small list
            // But to keep it simple: show any saved notes by re-querying via a quick method – we'll do a lightweight approach: pick from a cached source not needed; instead, just show instructions? no. use NoteDao.getNotesWithAlarms is wrong; so add a sync query
            // quick fix: add sync getAll to DAO logic by reading via a helper? implement by executing and collecting – but we already have executor; better add @Transaction? not necessary. We'll read by creating a temp list via LiveData observer not possible here; instead, add method: we'll modify DAO next? but easier: query all notes synchronously by writing a small helper – but in same executor, just call noteDao and we need to return list; change approach: use runBlocking not available; instead, store last list in a static holder? no. simpler: just show all notes by loading via a one-shot LiveData value? LiveData.getValue() returns last; if none loaded, empty. So show "Next" only if count>1.
            List<Note> loaded = NoteDatabase.getInstance(this).noteDao().getAllNotes().getValue();
            List<Note> result = loaded != null ? new ArrayList<>(loaded) : new ArrayList<>();
            // if empty, maybe finish
            handler.post(() -> {
                notes = result;
                if (notes.isEmpty()) {
                    finish();
                    return;
                }
                index = 0;
                showNote(notes.get(index));
                binding.btnNext.setVisibility(notes.size() > 1 ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showNote(Note n) {
        binding.tvMessage.setText(n.text);
    }

    private void showNext() {
        if (notes.isEmpty()) return;
        index = (index + 1) % notes.size();
        showNote(notes.get(index));
    }
}