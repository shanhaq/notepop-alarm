package com.zyvo.notepop.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY id DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(long id);

    @Query("SELECT * FROM notes WHERE alarmTimeMillis > 0 ORDER BY alarmTimeMillis ASC")
    List<Note> getNotesWithAlarms();

    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);
}