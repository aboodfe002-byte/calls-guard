package com.familyguard.calls.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.familyguard.calls.R

@Composable
fun SettingsTab(state: AppState) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { SectionHeader(stringResource(R.string.set_section_blocking)) }
        item { SwitchRow(R.string.set_enabled, R.string.set_enabled_desc, state.enabled) }
        item { SwitchRow(R.string.set_app_calls, R.string.set_app_calls_desc, state.blockAppCalls) }
        item { SwitchRow(R.string.set_starred, R.string.set_starred_desc, state.allowStarred) }
        item { SwitchRow(R.string.set_short, R.string.set_short_desc, state.allowShort) }
        item { SwitchRow(R.string.set_hidden, R.string.set_hidden_desc, state.allowHidden) }

        item { SectionHeader(stringResource(R.string.set_section_notif)) }
        item { SwitchRow(R.string.set_skip_notif, R.string.set_skip_notif_desc, state.skipNotification) }
        item { SwitchRow(R.string.set_skip_log, R.string.set_skip_log_desc, state.skipCallLog) }

        item { SectionHeader(stringResource(R.string.set_section_system)) }
        item { SwitchRow(R.string.set_keepalive, R.string.set_keepalive_desc, state.keepAlive) }
    }
}

@Composable
private fun SwitchRow(@StringRes title: Int, @StringRes subtitle: Int, toggle: AppState.Toggle) {
    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = { Text(stringResource(subtitle)) },
        trailingContent = { Switch(checked = toggle.value, onCheckedChange = { toggle.update(it) }) },
    )
}
