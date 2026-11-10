package com.familyguard.calls.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.familyguard.calls.R
import kotlinx.coroutines.launch

/** "المسموحة" tab, top to bottom: favourite contacts, allowed numbers, allowed prefixes. */
@Composable
fun FamilyTab(state: AppState, snackbar: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    var showPrefix by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }

    val resources = LocalResources.current

    fun undoable(label: String, remove: () -> Unit, restore: () -> Unit) {
        remove()
        scope.launch {
            val r = snackbar.showSnackbar(
                resources.getString(R.string.removed_x, label),
                actionLabel = resources.getString(R.string.undo),
                withDismissAction = true,
            )
            if (r == SnackbarResult.ActionPerformed) restore()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp),
    ) {
        // ---- 1. Favourites ----
        if (state.allowStarred.value) {
            item { SectionHeader(stringResource(R.string.section_starred, state.starred.size)) }
            if (state.starred.isEmpty()) {
                item {
                    Hint(
                        stringResource(if (state.perms.contacts) R.string.starred_empty else R.string.starred_need_contacts)
                    )
                }
            }
            items(state.starred, key = { "s:" + it.name + it.numbers.joinToString() }) { c ->
                ListItem(
                    leadingContent = { Avatar(c.name) },
                    headlineContent = { Text(c.name) },
                    supportingContent = { Text(c.numbers.joinToString("  ·  "), style = ltrStyle()) },
                    trailingContent = {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                )
            }
        }

        // ---- 2. Allowed numbers ----
        item { HeaderWithAdd(stringResource(R.string.section_numbers, state.entries.size)) { showAdd = true } }
        item { Hint(stringResource(if (state.entries.isEmpty()) R.string.numbers_empty else R.string.swipe_to_delete)) }
        items(state.entries, key = { "n:" + it.number }) { entry ->
            SwipeRow(onDismiss = {
                undoable(entry.name.ifEmpty { entry.number }, { state.remove(entry) }, { state.add(entry.number, entry.name) })
            }) {
                ListItem(
                    leadingContent = { Avatar(entry.name.ifEmpty { "#" }) },
                    headlineContent = { Text(entry.name.ifEmpty { entry.number }) },
                    supportingContent = if (entry.name.isNotEmpty()) {
                        { Text(entry.number, style = ltrStyle()) }
                    } else null,
                )
            }
        }

        // ---- 3. Allowed prefixes ----
        item { HeaderWithAdd(stringResource(R.string.section_prefixes, state.prefixes.size)) { showPrefix = true } }
        if (state.prefixes.isEmpty()) {
            item { Hint(stringResource(R.string.prefixes_hint)) }
        }
        items(state.prefixes, key = { "p:" + it.number }) { p ->
            SwipeRow(onDismiss = {
                undoable(p.number, { state.removePrefix(p) }, { state.addPrefix(p.number, p.name) })
            }) {
                ListItem(
                    leadingContent = { Avatar("…") },
                    headlineContent = { Text(p.number + "…", style = ltrStyle()) },
                    supportingContent = if (p.name.isNotEmpty()) {
                        { Text(p.name) }
                    } else null,
                )
            }
        }
    }

    if (showAdd) AddDialog(state) { showAdd = false }
    if (showPrefix) PrefixDialog(state) { showPrefix = false }
}

/** Section title with a "+ إضافة" button at the end of the row. */
@Composable
private fun HeaderWithAdd(title: String, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.weight(1f)) { SectionHeader(title) }
        TextButton(onClick = onAdd, modifier = Modifier.padding(top = 8.dp, end = 8.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(stringResource(R.string.add))
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

/** Swipe either way to delete. */
@Composable
private fun SwipeRow(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val swipe = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipe.currentValue) {
        if (swipe.currentValue != SwipeToDismissBoxValue.Settled) onDismiss()
    }
    SwipeToDismissBox(
        state = swipe,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { content() }
}
