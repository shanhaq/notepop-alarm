package com.zyvo.notepop.ui;

import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zyvo.notepop.R;
import com.zyvo.notepop.data.Note;
import com.zyvo.notepop.databinding.ActivityMainBinding;
import com.zyvo.notepop.databinding.DialogAddNoteBinding;
import com.zyvo.notepop.receiver.ScreenOnReceiver;
import com.zyvo.notepop.ui.adapter.NotesAdapter;
import com.zyvo.notepop.viewmodel.NoteViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NoteViewModel viewModel;
    private NotesAdapter adapter;
    private ScreenOnReceiver screenOnReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        adapter = new NotesAdapter(new NotesAdapter.OnNoteActionListener() {
            @Override
            public void onSetAlarm(Note note) {
                AlarmPickerBottomSheet.show(MainActivity.this, note, viewModel);
            }

            @Override
            public void onClearAlarm(Note note) {
                viewModel.clearAlarm(note);
            }
        });
        binding.notesRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.notesRecycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        viewModel.getAllNotes().observe(this, notes -> {
            adapter.submitList(notes);
            binding.emptyView.setVisibility(notes == null || notes.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        binding.fabAdd.setOnClickListener(v -> showAddNoteDialog());

        // Register SCREEN_ON receiver
        screenOnReceiver = new ScreenOnReceiver();
        IntentFilter filter = new IntentFilter(android.content.Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOnReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(screenOnReceiver);
        } catch (Exception ignored) {
        }
    }

    private void showAddNoteDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        DialogAddNoteBinding db = DialogAddNoteBinding.inflate(getLayoutInflater());
        d.setContentView(db.getRoot());
        db.btnSave.setOnClickListener(v -> {
            String t = db.etNote.getText() != null ? db.etNote.getText().toString().trim() : "";
            if (!t.isEmpty()) {
                Note n = new Note();
                n.text = t;
                n.alarmTimeMillis = 0;
                viewModel.insert(n);
            }
            d.dismiss();
        });
        db.btnCancel.setOnClickListener(v -> d.dismiss());
        d.show();
    }
}