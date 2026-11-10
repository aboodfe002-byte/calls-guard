package com.familyguard.calls.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.familyguard.calls.PhoneNormalizer
import com.familyguard.calls.R

/** Ready-made prefixes for Saudi numbers that companies and government bodies call from. */
private val presets = listOf(
    // Companies & government
    "920" to R.string.preset_920,
    "800" to R.string.preset_800,
    // Saudi landlines
    "01" to R.string.preset_01,
    "011" to R.string.preset_011,
    "012" to R.string.preset_012,
    "013" to R.string.preset_013,
    "014" to R.string.preset_014,
    "016" to R.string.preset_016,
    "017" to R.string.preset_017,
    // Wider rules
    "05" to R.string.preset_05,
    "+966" to R.string.preset_966,
    // Gulf countries
    "+971" to R.string.preset_uae,
    "+965" to R.string.preset_kw,
    "+973" to R.string.preset_bh,
    "+974" to R.string.preset_qa,
    "+968" to R.string.preset_om,
)

/** Prefixes the user may want to understand: shown under the field as the normalized result. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrefixDialog(state: AppState, onDismiss: () -> Unit) {
    var prefix by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    val normalized = PhoneNormalizer.normalizePrefix(prefix)
    val existing = state.prefixes.map { it.number }.toSet()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prefix_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.prefix_explain), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text(stringResource(R.string.prefix_field)) },
                    placeholder = { Text(stringResource(R.string.prefix_hint)) },
                    supportingText = {
                        Text(
                            when {
                                prefix.isBlank() -> stringResource(R.string.prefix_help_empty)
                                normalized == null -> stringResource(R.string.prefix_invalid)
                                else -> stringResource(R.string.prefix_result, normalized)
                            }
                        )
                    },
                    isError = prefix.isNotBlank() && normalized == null,
                    singleLine = true,
                    textStyle = ltrStyle(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.prefix_label_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.suggestions), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { (p, labelRes) ->
                        val l = stringResource(labelRes)
                        val already = PhoneNormalizer.normalizePrefix(p) in existing
                        AssistChip(
                            onClick = { prefix = p; label = l },
                            enabled = !already,
                            label = { Text("$p · $l") },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = normalized != null,
                onClick = {
                    state.addPrefix(normalized!!, label.ifBlank { prefix.trim() })
                    onDismiss()
                },
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
