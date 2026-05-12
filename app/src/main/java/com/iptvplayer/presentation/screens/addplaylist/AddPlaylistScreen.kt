package com.iptvplayer.presentation.screens.addplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.iptvplayer.presentation.components.TvTextFieldLabeled
import com.iptvplayer.presentation.navigation.AddPlaylistRouteArgs
import com.iptvplayer.presentation.theme.AppColors

enum class AddPlaylistTab {
    M3U_URL,
    XTREAM,
}

object AddPlaylistScreen {
    var initialArgs: com.iptvplayer.presentation.navigation.AddPlaylistRouteArgs? = null
}

@Composable
fun AddPlaylistScreen(
    onAddM3uUrl: (String, String) -> Unit,
    onAddXtream: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    initialArgs: com.iptvplayer.presentation.navigation.AddPlaylistRouteArgs? = null,
) {
    val args = initialArgs ?: AddPlaylistScreen.initialArgs
    val isXtream = args?.serverUrl != null || args?.type == "XTREAM"
    var selectedTab by remember { mutableStateOf(if (isXtream) AddPlaylistTab.XTREAM else AddPlaylistTab.M3U_URL) }
    var name by remember { mutableStateOf(args?.name ?: "") }
    var serverUrl by remember { mutableStateOf(args?.serverUrl ?: "") }
    var username by remember { mutableStateOf(args?.username ?: "") }
    var password by remember { mutableStateOf(args?.password ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.HeaderGradient)
                .padding(horizontal = 48.dp, vertical = 24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "Add Playlist",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tab selector
        Row(
            modifier = Modifier
                .background(AppColors.SurfaceCard, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TabButton(
                icon = Icons.Default.List,
                label = "M3U URL",
                selected = selectedTab == AddPlaylistTab.M3U_URL,
                onClick = { selectedTab = AddPlaylistTab.M3U_URL },
            )
            TabButton(
                icon = Icons.Default.Settings,
                label = "Xtream",
                selected = selectedTab == AddPlaylistTab.XTREAM,
                onClick = { selectedTab = AddPlaylistTab.XTREAM },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Form content
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .weight(1f),
        ) {
            when (selectedTab) {
                AddPlaylistTab.M3U_URL -> M3uUrlForm(
                    name = name,
                    onNameChange = { name = it },
                    url = serverUrl,
                    onUrlChange = { serverUrl = it },
                    onSubmit = { onAddM3uUrl(name, serverUrl) },
                )
                AddPlaylistTab.XTREAM -> XtreamForm(
                    name = name,
                    onNameChange = { name = it },
                    serverUrl = serverUrl,
                    onServerUrlChange = { serverUrl = it },
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it },
                    onSubmit = { onAddXtream(name, serverUrl, username, password) },
                )
            }
        }

        // Cancel button
        Button(
            onClick = onBack,
            modifier = Modifier.padding(vertical = 24.dp).width(140.dp),
            colors = ButtonDefaults.colors(
                containerColor = AppColors.SurfaceCard,
                focusedContainerColor = AppColors.SurfaceFocus,
            ),
        ) {
            Text(text = "Cancel", fontSize = 15.sp, color = AppColors.TextPrimary)
        }
    }
}

@Composable
private fun TabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) AppColors.Primary else Color.Transparent,
            focusedContainerColor = AppColors.SurfaceFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        modifier = Modifier.width(140.dp).height(48.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else AppColors.TextSecondary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else AppColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun M3uUrlForm(
    name: String,
    onNameChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val valid = name.isNotBlank() && url.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        TvTextFieldLabeled(
            value = name,
            onValueChange = onNameChange,
            label = "Playlist Name",
            placeholder = "My IPTV List",
        )
        TvTextFieldLabeled(
            value = url,
            onValueChange = onUrlChange,
            label = "M3U URL",
            placeholder = "http://example.com/playlist.m3u",
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done,
            onImeAction = { if (valid) onSubmit() },
        )
        Button(
            onClick = { onSubmit() },
            enabled = valid,
            modifier = Modifier.align(Alignment.End).width(180.dp),
            colors = ButtonDefaults.colors(
                containerColor = if (valid) AppColors.Primary else AppColors.SurfaceElevated,
                focusedContainerColor = if (valid) AppColors.PrimaryDim else AppColors.SurfaceElevated,
            ),
        ) {
            Text(
                text = "Add Playlist",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (valid) Color.White else AppColors.TextTertiary,
            )
        }
    }
}

@Composable
private fun XtreamForm(
    name: String,
    onNameChange: (String) -> Unit,
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val valid = name.isNotBlank() && serverUrl.isNotBlank() &&
        username.isNotBlank() && password.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        TvTextFieldLabeled(
            value = name,
            onValueChange = onNameChange,
            label = "Playlist Name",
            placeholder = "My IPTV List",
        )
        TvTextFieldLabeled(
            value = serverUrl,
            onValueChange = onServerUrlChange,
            label = "Server URL",
            placeholder = "http://example.com:port",
            keyboardType = KeyboardType.Uri,
        )
        TvTextFieldLabeled(
            value = username,
            onValueChange = onUsernameChange,
            label = "Username",
            placeholder = "Enter username",
        )
        TvTextFieldLabeled(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "Enter password",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = { if (valid) onSubmit() },
        )
        Button(
            onClick = { onSubmit() },
            enabled = valid,
            modifier = Modifier.align(Alignment.End).width(180.dp),
            colors = ButtonDefaults.colors(
                containerColor = if (valid) AppColors.Primary else AppColors.SurfaceElevated,
                focusedContainerColor = if (valid) AppColors.PrimaryDim else AppColors.SurfaceElevated,
            ),
        ) {
            Text(
                text = "Add Playlist",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (valid) Color.White else AppColors.TextTertiary,
            )
        }
    }
}
