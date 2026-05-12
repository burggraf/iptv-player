package com.iptvplayer.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.presentation.theme.AppColors

private val shimmerColors = listOf(
    AppColors.ShimmerBase,
    AppColors.ShimmerHighlight,
    AppColors.ShimmerBase,
)

/**
 * Shimmer placeholder effect.
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    if (!visible) return

    var width by remember { mutableStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -width,
        targetValue = width,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer-translate",
    )

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim, 0f),
                    end = Offset(translateAnim + width, 0f),
                ),
                shape = RoundedCornerShape(6.dp),
            )
            .onGloballyPositioned { width = it.size.width.toFloat() },
    )
}

/**
 * Skeleton EPG grid shown while loading channel data.
 */
@Composable
fun EpgSkeletonLoader(
    channelCount: Int = 8,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Skeleton for search bar
        ShimmerPlaceholder(
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 8.dp),
        )

        repeat(channelCount) {
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                // Channel name placeholder
                ShimmerPlaceholder(
                    modifier = Modifier.width(100.dp).height(40.dp).padding(end = 8.dp),
                )
                // Programme placeholders
                ShimmerPlaceholder(
                    modifier = Modifier.fillMaxWidth(0.3f).height(40.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                ShimmerPlaceholder(
                    modifier = Modifier.fillMaxWidth(0.2f).height(40.dp),
                )
            }
        }
    }
}

/**
 * Shimmer for channel logos.
 */
@Composable
fun LogoShimmer(
    modifier: Modifier = Modifier,
) {
    ShimmerPlaceholder(
        modifier = modifier,
    )
}

/**
 * Loading state with spinner and label.
 */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    label: String = "Loading...",
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.Primary)
            Text(
                text = label,
                color = AppColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * Error state with message and retry button.
 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    retryLabel: String = "Retry",
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = " $message",
                color = AppColors.Error,
                fontSize = 14.sp,
            )
            androidx.tv.material3.Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text(retryLabel)
            }
        }
    }
}
