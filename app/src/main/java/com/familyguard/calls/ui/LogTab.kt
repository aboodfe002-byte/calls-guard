package com.familyguard.calls.ui

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.familyguard.calls.R
import com.familyguard.calls.RejectLog
import com.familyguard.calls.RejectedCall

@Composable
fun LogTab(state: AppState) {
    val context = LocalContext.current
    val today = remember(state.calls) {
        val start = RejectLog.startOfToday()
        state.calls.count { it.time >= start }
    }
    val labels = remember { mutableMapOf<String, String>() }
    var confirmClear by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (today == 0) stringResource(R.string.log_none_today)
                        else pluralStringResource(R.plurals.log_rejected_today, today, today),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.calls.isNotEmpty()) TextButton(onClick = { confirmClear = true }) { Text(stringResource(R.string.log_clear)) }
                }
            }
        }

        if (state.calls.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.log_empty),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        items(state.calls) { call ->
            val source = labels.getOrPut(call.source) { sourceLabel(context, call.source) }
            ListItem(
                leadingContent = { Avatar(call.name ?: source) },
                headlineContent = {
                    val label = call.name ?: call.number ?: stringResource(R.string.hidden_number)
                    if (call.name == null && call.number != null) Text(label, style = ltrStyle()) else Text(label)
                },
                supportingContent = {
                    Text(listOfNotNull(source, call.number.takeIf { call.name != null }).joinToString(" · "))
                },
                trailingContent = { Text(formatTime(context, call), style = MaterialTheme.typography.bodySmall) },
            )
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            text = { Text(stringResource(R.string.log_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = { state.clearLog(); confirmClear = false }) { Text(stringResource(R.string.clear)) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

private fun sourceLabel(context: Context, source: String): String {
    if (source == RejectLog.SOURCE_PHONE) return context.getString(R.string.source_phone)
    return runCatching {
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        pm.getApplicationLabel(pm.getApplicationInfo(source, 0)).toString()
    }.getOrDefault(source)
}

private fun formatTime(context: Context, call: RejectedCall): String {
    val flags = if (call.time >= RejectLog.startOfToday()) DateUtils.FORMAT_SHOW_TIME
    else DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH
    return DateUtils.formatDateTime(context, call.time, flags)
}
