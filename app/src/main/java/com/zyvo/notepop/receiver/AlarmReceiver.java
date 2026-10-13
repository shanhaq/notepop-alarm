package com.zyvo.notepop.receiver;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.zyvo.notepop.R;
import com.zyvo.notepop.ui.AlarmPopupActivity;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1);
        if (noteId < 0) return;

        // Show alarm popup (full-screen)
        Intent popup = new Intent(context, AlarmPopupActivity.class);
        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        popup.putExtra(EXTRA_NOTE_ID, noteId);
        context.startActivity(popup);

        // Start alarm sound service
        Intent svc = new Intent(context, AlarmSoundService.class);
        svc.putExtra(EXTRA_NOTE_ID, noteId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(svc);
        } else {
            context.startService(svc);
        }

        // Also post notification as fallback (optional but safe)
        createChannel(context);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Alarm triggered")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) (noteId % 100000), nb.build());
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.notification_channel_alarm),
                    NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("NotePop alarms");
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    public static void scheduleAlarm(Context context, long noteId, long triggerAtMillis, String soundUri, String soundTitle) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra(EXTRA_NOTE_ID, noteId);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int) (noteId % Integer.MAX_VALUE),
                i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            }
        }
        // Sound info stored in Note; service reads from DB (we'll pass minimal via prefs or read DB)
        // store sound uri/title for this note id in prefs for service
        context.getSharedPreferences("notepop_alarm", Context.MODE_PRIVATE)
                .edit()
                .putString("sound_uri_" + noteId, soundUri)
                .putString("sound_title_" + noteId, soundTitle)
                .apply();
    }

    public static void cancelAlarm(Context context, long noteId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int) (noteId % Integer.MAX_VALUE),
                i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null && pi != null) am.cancel(pi);
        context.getSharedPreferences("notepop_alarm", Context.MODE_PRIVATE)
                .edit()
                .remove("sound_uri_" + noteId)
                .remove("sound_title_" + noteId)
                .apply();
    }
}