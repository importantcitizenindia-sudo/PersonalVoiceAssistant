# Personal Voice Assistant — Android MVP

This is a working starter architecture for a personal phone assistant:

- Push-to-talk voice input using Android SpeechRecognizer.
- Spoken responses using Android TextToSpeech.
- Local Room/SQLite database for notes and command history expansion.
- Basic phone actions: camera, browser, settings, time.
- Easy place to add more commands or connect an AI model.

## Database / 15 GB

The app uses Room over SQLite. This is local phone storage, not a cloud quota. It can grow according to available device storage, so a phone with enough free storage can hold many GB.

A free cloud database with a guaranteed 15 GB database quota is not generally available. Current examples:
- Firebase Realtime Database Spark: 1 GB database storage.
- Cloud Firestore free quota: 1 GiB stored data.
- Supabase Free: 500 MB database + 1 GB file storage.
- A Google Account provides up to 15 GB cloud storage shared by Drive, Gmail and Photos, but Google Drive is file storage, not a SQL database.

For a private assistant, the recommended design is:
1. Room/SQLite as the primary local database.
2. Optional Google Drive backup/export for large files.
3. Optional cloud database later if multi-device synchronization is needed.

## Build

Open this folder in Android Studio, let Gradle sync, then Run on an Android phone.

To create an APK:
Android Studio -> Build -> Generate App Bundles or APKs -> Generate APKs.

## Current voice commands

- "save note buy milk"
- "remember call mom tomorrow"
- "show notes"
- "read my notes"
- "what time is it"
- "open camera"
- "open browser"
- "open settings"

## Important next stage

Natural-language commands such as "send a WhatsApp message to Ravi", "set an alarm for 7 AM", "read my latest email", or "turn on Bluetooth" need additional Android permissions/APIs and, for some actions, user confirmation or OS restrictions.

For a full AI assistant, add an AI backend/API behind `CommandRouter`. Do not hard-code an API key inside the APK.
