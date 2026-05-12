package com.iptvplayer.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.presentation.theme.AppColors

@Composable
fun HomeScreen(
    playlists: List<Playlist> = emptyList(),
    selectedPlaylistId: String? = null,
    onNavigateToEpg: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddPlaylist: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onPlaylistSelected: (String) -> Unit = {},
    onPlaylistRemoved: (String) -> Unit = {},
    onPlaylistRefreshed: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        // ── Header ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.HeaderGradient)
                .padding(horizontal = 48.dp, vertical = 24.dp),
        ) {
            Column {
                Text(
                    text = "IPTV Player",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                )
                if (playlists.isNotEmpty()) {
                    val selected = playlists.find { it.id == selectedPlaylistId }
                    selected?.let {
                        Text(
                            text = "${it.name}  ·  ${it.type.name}",
                            fontSize = 14.sp,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Main Content ────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
        ) {
            // Left: Navigation cards
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Quick actions row
                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ActionCard(
                        icon = Icons.Default.List,
                        label = "EPG Guide",
                        subtitle = "Browse channels",
                        accentColor = AppColors.Primary,
                        onClick = onNavigateToEpg,
                        modifier = Modifier.weight(1f),
                    )
                    ActionCard(
                        icon = Icons.Default.Star,
                        label = "Favorites",
                        subtitle = "Your channels",
                        accentColor = AppColors.Warning,
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.weight(1f),
                    )
                    ActionCard(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        subtitle = "Preferences",
                        accentColor = AppColors.Secondary,
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Playlist section
                Text(
                    text = "Playlists",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                if (playlists.isEmpty()) {
                    Surface(
                        onClick = onNavigateToAddPlaylist,
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = AppColors.SurfaceCard,
                            focusedContainerColor = AppColors.SurfaceFocus,
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                        modifier = Modifier.fillMaxWidth(0.6f).height(80.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
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
                                Column {
                                    Text(
                                        text = "Add Playlist",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.TextPrimary,
                                    )
                                    Text(
                                        text = "Import M3U or Xtream",
                                        fontSize = 12.sp,
                                        color = AppColors.TextTertiary,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(0.7f),
                    ) {
                        playlists.forEach { playlist ->
                            val selected = playlist.id == selectedPlaylistId
                            PlaylistRow(
                                playlist = playlist,
                                isSelected = selected,
                                onSelect = { onPlaylistSelected(playlist.id) },
                                onRefresh = { onPlaylistRefreshed(playlist.id) },
                                onRemove = { onPlaylistRemoved(playlist.id) },
                            )
                        }

                        // Add more playlist button
                        Surface(
                            onClick = onNavigateToAddPlaylist,
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent,
                                focusedContainerColor = AppColors.SurfaceFocus,
                            ),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AppColors.TextTertiary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "Add another playlist",
                                    fontSize = 13.sp,
                                    color = AppColors.TextTertiary,
                                )
                            }
                        }
                    }
                }
            }

            // Right: Info / Status panel
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .padding(start = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // Stats card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = AppColors.SurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(20.dp),
                ) {
                    val totalChannels = playlists.size
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Channels",
                            fontSize = 13.sp,
                            color = AppColors.TextTertiary,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = if (totalChannels == 0) "—" else "$totalChannels",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = "${playlists.size} playlist${if (playlists.size != 1) "s" else ""} loaded",
                            fontSize = 12.sp,
                            color = AppColors.TextTertiary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tip card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF1A2030),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(20.dp),
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Tip",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextPrimary,
                            )
                        }
                        Text(
                            text = "Use D-pad to navigate. Press Center to select channels and EPG entries.",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AppColors.SurfaceCard,
            focusedContainerColor = AppColors.SurfaceFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        modifier = modifier.height(100.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = AppColors.TextTertiary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRefresh: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) AppColors.PrimaryContainer else AppColors.SurfaceCard,
            focusedContainerColor = AppColors.SurfaceFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = playlist.type.name,
                        fontSize = 11.sp,
                        color = AppColors.TextTertiary,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    onClick = onRefresh,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("↻", fontSize = 16.sp, color = AppColors.TextSecondary)
                    }
                }
                Surface(
                    onClick = onRemove,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✕", fontSize = 14.sp, color = AppColors.Error)
                    }
                }
            }
        }
    }
}
