package app.ocphotos.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.ocphotos.util.UiError

// Resolves a UiError with the current locale inside composition.
@Composable
fun UiError.asText(): String =
    if (arg == null) stringResource(messageRes) else stringResource(messageRes, arg)

// Same, for places without composition (snackbars, callbacks).
fun UiError.asText(context: Context): String =
    if (arg == null) context.getString(messageRes) else context.getString(messageRes, arg)
