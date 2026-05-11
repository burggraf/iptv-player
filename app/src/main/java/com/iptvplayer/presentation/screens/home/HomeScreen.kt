package com.iptvplayer.presentation.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button

@Composable
fun HomeScreen(
    onNavigateToEpg: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("IPTV Player", modifier = Modifier.padding(bottom = 24.dp))

        Button(onClick = onNavigateToEpg) {
            Text("Open EPG")
        }

        Button(onClick = onNavigateToSettings) {
            Text("Settings")
        }
    }
}
