package com.iptvplayer.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Switch
import androidx.tv.material3.Text

@Composable
fun SettingsScreen(
    state: SettingsScreenState = SettingsScreenState(),
    onEpgRefreshIntervalChange: (Int) -> Unit = {},
    onPixelPerMinuteChange: (Int) -> Unit = {},
    onChannelSwitchDelayChange: (Long) -> Unit = {},
    onStartOnLastChannelToggle: () -> Unit = {},
    onShowChannelNumbersToggle: () -> Unit = {},
    onBufferSizeChange: (String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Settings",
            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // EPG section
        SettingSection(title = "EPG") {
            NumberSetting(
                label = "Refresh Interval (hours)",
                value = state.epgRefreshIntervalHours,
                onDecrement = { onEpgRefreshIntervalChange((state.epgRefreshIntervalHours - 1).coerceAtLeast(1)) },
                onIncrement = { onEpgRefreshIntervalChange((state.epgRefreshIntervalHours + 1).coerceAtMost(24)) },
            )
            NumberSetting(
                label = "Pixels Per Minute",
                value = state.pixelPerMinute,
                onDecrement = { onPixelPerMinuteChange((state.pixelPerMinute - 1).coerceAtLeast(1)) },
                onIncrement = { onPixelPerMinuteChange((state.pixelPerMinute + 1).coerceAtMost(4)) },
            )
        }

        // Playback section
        SettingSection(title = "Playback") {
            NumberSetting(
                label = "Channel Switch Delay (ms)",
                value = state.channelSwitchDelayMs.toInt(),
                onDecrement = { onChannelSwitchDelayChange((state.channelSwitchDelayMs - 100).coerceAtLeast(100)) },
                onIncrement = { onChannelSwitchDelayChange((state.channelSwitchDelayMs + 100).coerceAtMost(1000)) },
            )
            ToggleSetting(
                label = "Start on Last Channel",
                checked = state.startOnLastChannel,
                onToggle = onStartOnLastChannelToggle,
            )
            BufferSizeSetting(
                value = state.bufferSize,
                onChange = onBufferSizeChange,
            )
        }

        // Display section
        SettingSection(title = "Display") {
            ToggleSetting(
                label = "Show Channel Numbers",
                checked = state.showChannelNumbers,
                onToggle = onShowChannelNumbersToggle,
            )
        }

        // Error / success
        state.error?.let {
            Text(text = "Error: $it", color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 16.dp))
        }
        state.successMessage?.let {
            Text(text = it, color = androidx.compose.ui.graphics.Color.Green, modifier = Modifier.padding(top = 8.dp))
        }

        Button(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) {
            Text("Back")
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(bottom = 12.dp),
        )
        content()
    }
}

@Composable
private fun NumberSetting(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = TextStyle(fontSize = 14.sp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onDecrement) { Text("-") }
            Text(value.toString(), style = TextStyle(fontSize = 16.sp))
            Button(onClick = onIncrement) { Text("+") }
        }
    }
}

@Composable
private fun ToggleSetting(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = TextStyle(fontSize = 14.sp))
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun BufferSizeSetting(value: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Buffer Size", style = TextStyle(fontSize = 14.sp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Small", "Medium", "Large").forEach { size ->
                Button(
                    onClick = { onChange(size) },
                ) {
                    Text(
                        text = size,
                        color = if (size == value) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
        }
    }
}

data class SettingsScreenState(
    val epgRefreshIntervalHours: Int = 6,
    val pixelPerMinute: Int = 2,
    val channelSwitchDelayMs: Long = 300,
    val startOnLastChannel: Boolean = true,
    val showChannelNumbers: Boolean = true,
    val bufferSize: String = "Medium",
    val error: String? = null,
    val successMessage: String? = null,
)
