package com.example.personalvoiceassistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import java.text.DateFormat
import java.util.Date

class CommandRouter(private val context: Context, private val db: AppDatabase) {

    suspend fun execute(command: String): String {
        val c = command.trim()
        val lower = c.lowercase()

        return when {
            lower.startsWith("save note ") -> {
                val note = c.substringAfter("save note ").trim()
                if (note.isBlank()) "Please say the note after 'save note'."
                else {
                    db.noteDao().insert(Note(text = note))
                    "Saved your note."
                }
            }

            lower.startsWith("remember ") -> {
                val note = c.substringAfter("remember ").trim()
                if (note.isBlank()) "Please tell me what to remember."
                else {
                    db.noteDao().insert(Note(text = note))
                    "I saved that in your notes."
                }
            }

            lower == "show notes" || lower == "read my notes" -> {
                val notes = db.noteDao().getAll()
                if (notes.isEmpty()) "You have no saved notes."
                else notes.take(10).joinToString(". ") { it.text }
            }

            lower == "what time is it" || lower == "tell me the time" -> {
                DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
            }

            lower.contains("open camera") -> {
                val intent = Intent("android.media.action.IMAGE_CAPTURE")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "Opening the camera."
            }

            lower.contains("open browser") || lower.contains("open google") -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "Opening the browser."
            }

            lower.contains("open settings") -> {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "Opening settings."
            }

            else -> "I heard: $c. Add this command to CommandRouter.kt or connect an AI API for more natural-language actions."
        }
    }
}
