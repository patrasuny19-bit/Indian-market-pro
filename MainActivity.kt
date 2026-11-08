package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainScreen
import com.example.ui.MarketViewModel
import com.example.ui.theme.IndiaMarketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure App Check before any Compose UI or Firebase operations
        com.example.util.configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            val marketViewModel: MarketViewModel = viewModel()
            val aiChatViewModel: com.example.ui.ai.AiChatViewModel = viewModel()
            val uiState by marketViewModel.uiState.collectAsState()

            IndiaMarketTheme(darkTheme = uiState.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        viewModel = marketViewModel,
                        aiViewModel = aiChatViewModel
                    )
                }
            }
        }
    }
}
