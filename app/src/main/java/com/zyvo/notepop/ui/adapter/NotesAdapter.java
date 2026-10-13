package com.zyvo.notepop.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.zyvo.notepop.R;
import com.zyvo.notepop.data.Note;
import com.zyvo.notepop.databinding.ItemNoteBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotesAdapter extends ListAdapter<Note, NotesAdapter.VH> {
    private final OnNoteActionListener listener;

    public NotesAdapter(OnNoteActionListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Note> DIFF = new DiffUtil.ItemCallback<Note>() {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.text != null && oldItem.text.equals(newItem.text)
                    && oldItem.alarmTimeMillis == newItem.alarmTimeMillis
                    && ((oldItem.alarmSoundUri == null && newItem.alarmSoundUri == null) ||
                    (oldItem.alarmSoundUri != null && oldItem.alarmSoundUri.equals(newItem.alarmSoundUri)));
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoteBinding b = ItemNoteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(getItem(position));
    }

    class VH extends RecyclerView.ViewHolder {
        private final ItemNoteBinding b;

        VH(ItemNoteBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Note n) {
            b.tvNoteText.setText(n.text);
            if (n.alarmTimeMillis > 0) {
                b.tvAlarmTime.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
                b.tvAlarmTime.setText("Alarm: " + sdf.format(new Date(n.alarmTimeMillis)));
                b.btnSetAlarm.setVisibility(View.GONE);
                b.btnClearAlarm.setVisibility(View.VISIBLE);
                b.btnClearAlarm.setOnClickListener(v -> listener.onClearAlarm(n));
            } else {
                b.tvAlarmTime.setVisibility(View.GONE);
                b.btnSetAlarm.setVisibility(View.VISIBLE);
                b.btnClearAlarm.setVisibility(View.GONE);
                b.btnSetAlarm.setOnClickListener(v -> listener.onSetAlarm(n));
            }
        }
    }

    public interface OnNoteActionListener {
        void onSetAlarm(Note note);
        void onClearAlarm(Note note);
    }
}