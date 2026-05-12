package com.iptvplayer.presentation.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.iptvplayer.presentation.theme.AppColors

@Composable
fun FavoritesScreen(
    favorites: List<String> = emptyList(),
    onChannelClick: (String) -> Unit = {},
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AppColors.Warning,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "Favorites",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                )
                if (favorites.isNotEmpty()) {
                    Text(
                        text = "(${favorites.size})",
                        fontSize = 16.sp,
                        color = AppColors.TextTertiary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        // Content
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.TextTertiary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp),
                    )
                    Text(
                        text = "No favorites yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary,
                    )
                    Text(
                        text = "Mark channels as favorites from the EPG to see them here",
                        fontSize = 14.sp,
                        color = AppColors.TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.6f),
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
            ) {
                items(favorites, key = { it }) { channelId ->
                    FavoriteChannelRow(
                        channelId = channelId,
                        onClick = { onChannelClick(channelId) },
                    )
                }
            }
        }

        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 24.dp).width(140.dp),
            colors = ButtonDefaults.colors(
                containerColor = AppColors.SurfaceCard,
                focusedContainerColor = AppColors.SurfaceFocus,
            ),
        ) {
            Text(text = "← Back", fontSize = 15.sp, color = AppColors.TextPrimary)
        }
    }
}

@Composable
private fun FavoriteChannelRow(
    channelId: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AppColors.SurfaceCard,
            focusedContainerColor = AppColors.SurfaceFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorite",
                tint = AppColors.Warning,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = channelId,
                color = AppColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
