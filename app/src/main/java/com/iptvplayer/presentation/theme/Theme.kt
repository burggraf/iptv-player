package com.iptvplayer.presentation.theme

import androidx.tv.material3.Typography
import androidx.tv.material3.MaterialTheme

object IptvPlayerTheme {
    val typography = Typography()

    @androidx.compose.runtime.Composable
    fun Content(content: @androidx.compose.runtime.Composable () -> Unit) {
        MaterialTheme(
            typography = typography,
            content = content
        )
    }
}
