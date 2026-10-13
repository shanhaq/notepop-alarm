package com.zyvo.notepop.ui;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zyvo.notepop.data.Note;
import com.zyvo.notepop.databinding.DialogSetAlarmBinding;
import com.zyvo.notepop.receiver.AlarmReceiver;
import com.zyvo.notepop.viewmodel.NoteViewModel;

import java.util.Calendar;

public class AlarmPickerBottomSheet {
    public static void show(AppCompatActivity activity, Note note, NoteViewModel vm) {
        BottomSheetDialog d = new BottomSheetDialog(activity);
        DialogSetAlarmBinding db = DialogSetAlarmBinding.inflate(activity.getLayoutInflater());
        d.setContentView(db.getRoot());

        final Calendar cal = Calendar.getInstance();
        final long[] selectedTime = new long[]{0};
        final Uri[] selectedUri = new Uri[]{note.alarmSoundUri != null ? Uri.parse(note.alarmSoundUri) : null};
        final String[] selectedTitle = new String[]{note.alarmSoundTitle};

        db.tvSound.setText(selectedTitle[0] != null ? selectedTitle[0] : "Default alarm sound");

        ActivityResultLauncher<Intent> picker = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                final int flags = result.getData().getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                activity.getContentResolver().takePersistableUriPermission(uri, flags);
                            } catch (Exception ignored) {
                            }
                            selectedUri[0] = uri;
                            String title = DocumentsContract.getDocumentId(uri);
                            // fallback
                            if (title == null) title = uri.getLastPathSegment();
                            selectedTitle[0] = title;
                            db.tvSound.setText(title);
                        }
                    }
                });

        db.btnPickTime.setOnClickListener(v -> {
            TimePickerDialog tp = new TimePickerDialog(activity, (view, hour, min) -> {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, min);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                selectedTime[0] = cal.getTimeInMillis();
                db.tvTime.setText(String.format("%02d:%02d", hour, min));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            tp.show();
        });

        db.btnPickSound.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("audio/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            picker.launch(i);
        });

        db.btnCancel.setOnClickListener(v -> d.dismiss());

        db.btnSave.setOnClickListener(v -> {
            if (selectedTime[0] == 0) {
                Toast.makeText(activity, "Pick a time first", Toast.LENGTH_SHORT).show();
                return;
            }
            // schedule alarm
            note.alarmTimeMillis = selectedTime[0];
            note.alarmSoundUri = selectedUri[0] != null ? selectedUri[0].toString() : null;
            note.alarmSoundTitle = selectedTitle[0];
            vm.update(note);
            // schedule
            AlarmReceiver.scheduleAlarm(activity, note.id, note.alarmTimeMillis, note.alarmSoundUri, note.alarmSoundTitle);
            d.dismiss();
        });

        d.show();
    }
}