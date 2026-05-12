package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.iptvplayer.presentation.theme.AppColors

/**
 * TV-friendly text field with visible label above the input.
 * Supports IME action buttons (Next/Done) for keyboard navigation.
 *
 * @param onImeAction Called when IME Next/Done is pressed.
 *   If null, focus automatically moves to the next field (Next) or dismisses (Done).
 */
@Composable
fun TvTextFieldLabeled(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFocused) AppColors.Primary else AppColors.TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )

        Surface(
            onClick = { focusRequester.requestFocus() },
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = AppColors.SurfaceElevated,
                focusedContainerColor = AppColors.SurfaceFocus,
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = AppColors.TextPrimary,
                    ),
                    cursorBrush = SolidColor(AppColors.Primary),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (onImeAction != null) {
                                onImeAction.invoke()
                            } else {
                                focusManager.moveFocus(FocusDirection.Next)
                            }
                        },
                        onDone = {
                            if (onImeAction != null) {
                                onImeAction.invoke()
                            } else {
                                focusManager.clearFocus()
                            }
                        },
                    ),
                    enabled = enabled,
                )

                if (value.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder ?: label,
                        fontSize = 16.sp,
                        color = AppColors.TextTertiary,
                    )
                }
            }
        }
    }
}
