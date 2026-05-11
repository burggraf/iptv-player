package com.iptvplayer.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text("Settings", modifier = Modifier.padding(bottom = 24.dp))

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
