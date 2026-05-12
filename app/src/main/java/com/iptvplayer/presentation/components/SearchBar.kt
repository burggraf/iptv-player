package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.presentation.theme.AppColors

/**
 * Search bar for filtering channels by name, number, or group.
 * Input is debounced at the ViewModel level (300ms).
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search channels...",
                color = AppColors.TextTertiary,
                fontSize = 14.sp,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = AppColors.TextTertiary,
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = AppColors.TextPrimary),
        singleLine = true,
        maxLines = 1,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            focusedContainerColor = AppColors.SurfaceElevated,
            unfocusedContainerColor = AppColors.SurfaceElevated,
            focusedBorderColor = AppColors.Primary,
            unfocusedBorderColor = Color(0xFF2A3444),
            focusedPlaceholderColor = AppColors.TextTertiary,
            unfocusedPlaceholderColor = AppColors.TextTertiary.copy(alpha = 0.5f),
            focusedLeadingIconColor = AppColors.TextSecondary,
            unfocusedLeadingIconColor = AppColors.TextTertiary,
            cursorColor = AppColors.Primary,
        ),
    )
}
