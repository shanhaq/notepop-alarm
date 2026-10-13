package com.zyvo.notepop.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NoteRepository(Application application) {
        noteDao = NoteDatabase.getInstance(application).noteDao();
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public void insert(Note note, InsertCallback callback) {
        executor.execute(() -> {
            long id = noteDao.insert(note);
            if (callback != null) callback.onInserted(id);
        });
    }

    public void update(Note note) {
        executor.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        executor.execute(() -> noteDao.delete(note));
    }

    public void getNotesWithAlarms(AlarmListCallback callback) {
        executor.execute(() -> {
            List<Note> list = noteDao.getNotesWithAlarms();
            if (callback != null) callback.onResult(list);
        });
    }

    public interface InsertCallback {
        void onInserted(long id);
    }

    public interface AlarmListCallback {
        void onResult(List<Note> notes);
    }
}