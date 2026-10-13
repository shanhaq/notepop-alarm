# NotePop – Alarm + Note Popup App

Native Android app (Kotlin/Java, Room DB). Features:
- Add notes (Room), list with empty/loading-friendly UI (Material 3)
- Set per-note alarm (time + custom sound from Files/System – audio picker with URI permission persisted)
- Screen OFF → ON: shows saved notes one-by-one in glass popup (Next/Open/Later)
- Alarm time: full-screen glass popup + custom/default alarm sound (foreground service, loop, wake lock)
- Reschedules alarms on boot/package replace

## Build
Push to main → GitHub Actions builds `app-debug.apk` and attaches to release `apk-<run>`.
Repo: shanhaq/notepop-alarm (public).