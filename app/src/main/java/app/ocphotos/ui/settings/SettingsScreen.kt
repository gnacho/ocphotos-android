package app.ocphotos.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ocphotos.data.settings.AppSettings
import app.ocphotos.data.settings.ThemeMode
import app.ocphotos.ui.components.SignOutDialog

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onTheme: (ThemeMode) -> Unit,
    onInterval: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    var confirmSignOut by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        Text("Theme", style = MaterialTheme.typography.titleMedium)
        ThemeMode.entries.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTheme(mode) }
                    .padding(vertical = 4.dp),
            ) {
                RadioButton(selected = settings.theme == mode, onClick = { onTheme(mode) })
                Text(themeLabel(mode))
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        Text("Sync interval", style = MaterialTheme.typography.titleMedium)
        listOf(5, 15, 30, 60).forEach { minutes ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInterval(minutes) }
                    .padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = settings.syncIntervalMinutes == minutes,
                    onClick = { onInterval(minutes) },
                )
                Text("$minutes min")
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        Text("Account", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { confirmSignOut = true }
                .padding(vertical = 12.dp),
        ) {
            Icon(
                Icons.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(16.dp))
            Text("Sign out", color = MaterialTheme.colorScheme.error)
        }
    }

    if (confirmSignOut) {
        SignOutDialog(
            onConfirm = {
                confirmSignOut = false
                onLogout()
            },
            onDismiss = { confirmSignOut = false },
        )
    }
}

private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}
