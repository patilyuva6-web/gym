package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.gymapp.GymApplication
import com.example.gymapp.navigation.GymAppRoot
import com.example.gymapp.viewmodel.GymViewModel
import com.example.gymapp.worker.NotificationHelper
import com.example.ui.theme.GymAppTheme

class MainActivity : ComponentActivity() {

    private var openDueScreenSignal by mutableIntStateOf(0)

    private val viewModel: GymViewModel by viewModels {
        val app = application as GymApplication
        GymViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            GymAppTheme {
                GymAppRoot(
                    viewModel = viewModel,
                    openDueScreenSignal = openDueScreenSignal
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationHelper.EXTRA_OPEN_DUE_SCREEN, false) == true) {
            openDueScreenSignal++
            intent.removeExtra(NotificationHelper.EXTRA_OPEN_DUE_SCREEN)
        }
    }
}
