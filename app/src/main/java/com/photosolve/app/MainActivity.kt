package com.photosolve.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.photosolve.app.capture.ACTION_SCREENSHOT_READY
import com.photosolve.app.capture.EXTRA_SCREENSHOT_URI
import com.photosolve.app.data.history.HistoryRepository
import com.photosolve.app.data.history.PhotoSolveDatabase
import com.photosolve.app.data.network.ImageDataEncoder
import com.photosolve.app.data.network.OpenAiCompatibleClient
import com.photosolve.app.data.settings.ApiSettingsRepository
import com.photosolve.app.domain.solve.SolveQuestionUseCase
import com.photosolve.app.ui.PhotoSolveApp

class MainActivity : ComponentActivity() {
    private var screenshot by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readCapture(intent)
        val settings = ApiSettingsRepository(applicationContext)
        val history = HistoryRepository(applicationContext, PhotoSolveDatabase.getInstance(this).historyDao())
        val solve = SolveQuestionUseCase(OpenAiCompatibleClient(ImageDataEncoder(contentResolver)))
        setContent {
            PhotoSolveApp(settings, history, solve, screenshot) {
                screenshot = null
                intent?.removeExtra(EXTRA_SCREENSHOT_URI)
                intent?.action = Intent.ACTION_MAIN
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readCapture(intent)
    }

    private fun readCapture(intent: Intent?) {
        if (intent?.action == ACTION_SCREENSHOT_READY) {
            screenshot = intent.getStringExtra(EXTRA_SCREENSHOT_URI)?.let(Uri::parse)
        }
    }
}
