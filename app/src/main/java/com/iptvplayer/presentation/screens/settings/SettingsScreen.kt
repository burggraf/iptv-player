package com.iptvplayer.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.iptvplayer.presentation.theme.AppColors

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
            .background(AppColors.Background),
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.HeaderGradient)
                .padding(horizontal = 48.dp, vertical = 24.dp),
        ) {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // EPG Section
            SettingsCard(title = "EPG") {
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

            // Playback Section
            SettingsCard(title = "Playback") {
                NumberSetting(
                    label = "Channel Switch Delay (ms)",
                    value = state.channelSwitchDelayMs.toInt(),
                    onDecrement = { onChannelSwitchDelayChange((state.channelSwitchDelayMs - 100).coerceAtLeast(100)) },
                    onIncrement = { onChannelSwitchDelayChange((state.channelSwitchDelayMs + 100).coerceAtMost(1000)) },
                )
                ToggleSetting(
                    label = "Start on Last Channel",
                    description = "Resume playback from last viewed channel",
                    checked = state.startOnLastChannel,
                    onToggle = onStartOnLastChannelToggle,
                )
                BufferSizeSetting(
                    value = state.bufferSize,
                    onChange = onBufferSizeChange,
                )
            }

            // Display Section
            SettingsCard(title = "Display") {
                ToggleSetting(
                    label = "Show Channel Numbers",
                    description = "Display channel numbers in the EPG grid",
                    checked = state.showChannelNumbers,
                    onToggle = onShowChannelNumbersToggle,
                )
            }

            // Messages
            state.error?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3A1515), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                ) {
                    Text(text = " $it", color = AppColors.Error, fontSize = 14.sp)
                }
            }
            state.successMessage?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF153A20), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                ) {
                    Text(text = "✓ $it", color = AppColors.Success, fontSize = 14.sp)
                }
            }

            // Back button
            Button(
                onClick = onBack,
                modifier = Modifier.padding(top = 8.dp).width(140.dp),
                colors = ButtonDefaults.colors(
                    containerColor = AppColors.SurfaceCard,
                    focusedContainerColor = AppColors.SurfaceFocus,
                ),
            ) {
                Text(
                    text = "← Back",
                    fontSize = 15.sp,
                    color = AppColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.SurfaceCard, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onDecrement,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("−", fontSize = 20.sp, color = AppColors.TextPrimary)
                }
            }
            Text(
                text = value.toString(),
                color = AppColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center,
            )
            Surface(
                onClick = onIncrement,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", fontSize = 20.sp, color = AppColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun ToggleSetting(
    label: String,
    description: String? = null,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = AppColors.TextSecondary, fontSize = 14.sp)
            description?.let {
                Text(
                    text = it,
                    color = AppColors.TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
        )
    }
}

@Composable
private fun BufferSizeSetting(value: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Buffer Size", color = AppColors.TextSecondary, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Small", "Medium", "Large").forEach { size ->
                val isSelected = size == value
                Surface(
                    onClick = { onChange(size) },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected) AppColors.Primary else AppColors.SurfaceElevated,
                        focusedContainerColor = AppColors.SurfaceFocus,
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                    modifier = Modifier.width(80.dp).height(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = size,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else AppColors.TextSecondary,
                        )
                    }
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
