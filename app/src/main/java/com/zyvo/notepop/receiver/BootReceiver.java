package com.zyvo.notepop.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zyvo.notepop.data.Note;
import com.zyvo.notepop.data.NoteDatabase;
import com.zyvo.notepop.data.NoteRepository;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_QUICKBOOT_POWERON.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            // Reschedule alarms on boot
            Executors.newSingleThreadExecutor().execute(() -> {
                List<Note> notes = NoteDatabase.getInstance(context).noteDao().getNotesWithAlarms();
                long now = System.currentTimeMillis();
                for (Note n : notes) {
                    if (n.alarmTimeMillis > now) {
                        AlarmReceiver.scheduleAlarm(context, n.id, n.alarmTimeMillis, n.alarmSoundUri, n.alarmSoundTitle);
                    }
                    // if past, optionally ignore (or clear)
                }
            });
        }
    }
}