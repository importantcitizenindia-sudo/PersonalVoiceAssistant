package com.example.personalvoiceassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var router: CommandRouter
    private lateinit var transcript: TextView
    private lateinit var status: TextView
    private lateinit var notesView: TextView

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening()
            else status.text = "Microphone permission is required."
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transcript = findViewById(R.id.transcript)
        status = findViewById(R.id.status)
        notesView = findViewById(R.id.notes)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "assistant.db"
        ).build()

        router = CommandRouter(this, db)

        tts = TextToSpeech(this) { tts.language = Locale.getDefault() }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                status.text = "Listening..."
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()

                transcript.text = text
                lifecycleScope.launch {
                    val response = router.execute(text)
                    status.text = response
                    tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, "assistant")
                    refreshNotes(db)
                }
            }

            override fun onError(error: Int) {
                status.text = "I couldn't understand that. Please try again."
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "Processing..." }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        findViewById<Button>(R.id.micButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                startListening()
            }
        }

        findViewById<Button>(R.id.notesButton).setOnClickListener {
            lifecycleScope.launch { refreshNotes(db) }
        }

        lifecycleScope.launch { refreshNotes(db) }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        speechRecognizer.startListening(intent)
    }

    private suspend fun refreshNotes(db: AppDatabase) {
        val notes = db.noteDao().getAll()
        notesView.text = if (notes.isEmpty()) {
            "No notes yet."
        } else {
            notes.take(20).joinToString("\n\n") { "• ${it.text}" }
        }
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
