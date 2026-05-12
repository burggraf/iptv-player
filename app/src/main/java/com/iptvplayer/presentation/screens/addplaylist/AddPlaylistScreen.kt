package com.iptvplayer.presentation.screens.addplaylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.iptvplayer.domain.model.PlaylistType

enum class AddPlaylistTab {
    M3U_URL,
    XTREAM,
}

@Composable
fun AddPlaylistScreen(
    onAddM3uUrl: (String, String) -> Unit,
    onAddXtream: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(AddPlaylistTab.M3U_URL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "Add Playlist",
            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Tab selector
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TabButton(
                label = "M3U URL",
                selected = selectedTab == AddPlaylistTab.M3U_URL,
                onClick = { selectedTab = AddPlaylistTab.M3U_URL },
            )
            TabButton(
                label = "Xtream",
                selected = selectedTab == AddPlaylistTab.XTREAM,
                onClick = { selectedTab = AddPlaylistTab.XTREAM },
            )
        }

        when (selectedTab) {
            AddPlaylistTab.M3U_URL -> M3uUrlForm(onSubmit = onAddM3uUrl)
            AddPlaylistTab.XTREAM -> XtreamForm(onSubmit = onAddXtream)
        }

        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("Cancel")
        }
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        selected = selected,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Text(text = label, style = TextStyle(fontSize = 16.sp))
    }
}

@Composable
private fun M3uUrlForm(onSubmit: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && url.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(0.6f),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Playlist Name") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp),
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("M3U URL") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp),
        )
        Button(
            onClick = { onSubmit(name, url) },
            enabled = valid,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Add Playlist")
        }
    }
}

@Composable
private fun XtreamForm(onSubmit: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && serverUrl.isNotBlank() &&
        username.isNotBlank() && password.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(0.6f),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Playlist Name") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp),
        )
        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("Server URL") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp),
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp),
        )
        Button(
            onClick = { onSubmit(name, serverUrl, username, password) },
            enabled = valid,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Add Playlist")
        }
    }
}
