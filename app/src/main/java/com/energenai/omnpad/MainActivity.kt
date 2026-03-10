package com.energenai.omnpad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.energenai.omnpad.ui.screens.EditorScreen
import com.energenai.omnpad.ui.theme.OmniPadTheme
import com.energenai.omnpad.ui.viewmodels.EditorViewModel
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
    private var pendingUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize PDFBox
        PDFBoxResourceLoader.init(applicationContext)

        // Handle intent (file opened from file manager / share)
        pendingUri = extractUri(intent)

        setContent {
            OmniPadTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    val vm: EditorViewModel = viewModel()
                    EditorScreen(
                        vm = vm,
                        initialUri = pendingUri,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new file opened while app is already running
        extractUri(intent)?.let { uri ->
            // Will need to communicate to the composable — using a simple approach
            pendingUri = uri
            recreate()
        }
    }

    private fun extractUri(intent: Intent?): Uri? {
        if (intent == null) return null
        // Direct file open
        intent.data?.let { return it }
        // Share intent
        if (intent.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { return it }
        }
        return null
    }
}
