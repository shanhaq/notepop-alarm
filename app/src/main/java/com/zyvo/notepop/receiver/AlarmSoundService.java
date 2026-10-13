package com.zyvo.notepop.receiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zyvo.notepop.R;

import java.io.IOException;

public class AlarmSoundService extends Service {
    private MediaPlayer mediaPlayer;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "NotePop:AlarmWakeLock");
            wakeLock.acquire(10 * 60 * 1000L /* 10 min */);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long noteId = intent != null ? intent.getLongExtra(AlarmReceiver.EXTRA_NOTE_ID, -1) : -1;
        startForeground(1001, buildNotification());

        SharedPreferences sp = getSharedPreferences("notepop_alarm", Context.MODE_PRIVATE);
        String soundUriStr = sp.getString("sound_uri_" + noteId, null);
        Uri uri = null;
        if (soundUriStr != null) {
            try {
                uri = Uri.parse(soundUriStr);
            } catch (Exception ignored) {
            }
        }
        if (uri == null) {
            uri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;
        }

        stopPlayer();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            // fallback to default
            try {
                mediaPlayer.setDataSource(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException ignored) {
            }
        }

        return START_NOT_STICKY;
    }

    private void stopPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {
            }
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    private android.app.Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder nb = new NotificationCompat.Builder(this, AlarmReceiver.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_bell)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Alarm playing")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            return nb.build();
        } else {
            NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification_bell)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Alarm playing")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            return nb.build();
        }
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, AlarmSoundService.class));
    }

    @Override
    public void onDestroy() {
        stopPlayer();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}