package com.familyguard.calls.ui

import androidx.annotation.StringRes
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.familyguard.calls.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Step(@StringRes val title: Int, @StringRes val body: Int)

private val steps = listOf(
    Step(R.string.ob1_title, R.string.ob1_body),
    Step(R.string.ob2_title, R.string.ob2_body),
    Step(R.string.ob3_title, R.string.ob3_body),
    Step(R.string.ob4_title, R.string.ob4_body),
)

/** First-run setup wizard: 4 steps. */
@Composable
fun Onboarding(state: AppState) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val last = steps.lastIndex

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .padding(24.dp)
        ) {
            LinearProgressIndicator(progress = { (step + 1) / steps.size.toFloat() }, modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.ob_step, step + 1, steps.size),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(24.dp))

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(steps[step].title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(steps[step].body), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                when (step) {
                    0 -> PermissionChecklist(state, setOf(PermItem.ROLE))
                    1 -> PermissionChecklist(state, setOf(PermItem.CONTACTS, PermItem.NOTIFICATIONS))
                    2 -> PermissionChecklist(state, setOf(PermItem.LISTENER, PermItem.BATTERY))
                    else -> {
                        AddNumberFields(onAdd = { e164, name -> state.add(e164, name) })
                        if (state.entries.isNotEmpty()) {
                            Text(stringResource(R.string.ob_added, state.entries.joinToString(", ") { it.name.ifEmpty { it.number } }))
                        }
                        if (state.starred.isNotEmpty()) {
                            Text(pluralStringResource(R.plurals.ob_starred_count, state.starred.size, state.starred.size))
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (step > 0) TextButton(onClick = { step-- }) { Text(stringResource(R.string.back)) } else Spacer(Modifier)
                Button(onClick = { if (step < last) step++ else state.finishOnboarding() }) {
                    Text(stringResource(if (step < last) R.string.next else R.string.finish))
                }
            }
        }
    }
}
