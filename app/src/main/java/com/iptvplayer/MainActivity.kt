package com.iptvplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iptvplayer.presentation.navigation.AppNavigation
import com.iptvplayer.presentation.theme.IptvPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IptvPlayerTheme {
                AppNavigation()
            }
        }
    }
}
