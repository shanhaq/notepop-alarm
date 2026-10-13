package com.zyvo.notepop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.zyvo.notepop.data.Note;
import com.zyvo.notepop.data.NoteRepository;
import com.zyvo.notepop.receiver.AlarmReceiver;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private final NoteRepository repo;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repo = new NoteRepository(application);
        allNotes = repo.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        repo.insert(note, id -> {
            // if we want to do anything with id, can here
        });
    }

    public void update(Note note) {
        repo.update(note);
    }

    public void delete(Note note) {
        repo.delete(note);
        AlarmReceiver.cancelAlarm(getApplication(), note.id);
    }

    public void clearAlarm(Note note) {
        note.alarmTimeMillis = 0;
        note.alarmSoundUri = null;
        note.alarmSoundTitle = null;
        repo.update(note);
        AlarmReceiver.cancelAlarm(getApplication(), note.id);
    }
}