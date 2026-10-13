package com.zyvo.notepop.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "alarmTimeMillis")
    public long alarmTimeMillis; // 0 means no alarm

    @ColumnInfo(name = "alarmSoundUri")
    public String alarmSoundUri; // null means default/system? keep as is

    @ColumnInfo(name = "alarmSoundTitle")
    public String alarmSoundTitle;
}