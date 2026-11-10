package com.familyguard.calls.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.familyguard.calls.PhoneNormalizer
import com.familyguard.calls.R

/** Current text style, but left-to-right: phone numbers stay readable inside the RTL layout. */
@Composable
fun ltrStyle(): TextStyle = LocalTextStyle.current.merge(TextStyle(textDirection = TextDirection.Ltr))

/** Round avatar with the first letter of the name. */
@Composable
fun Avatar(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label.trim().firstOrNull()?.uppercase() ?: "#",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** One permission line: ✓ or ⚠ with a button that opens the right system screen. */
@Composable
fun PermRow(title: String, description: String, ok: Boolean, onFix: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (ok) Color(0xFF2E9E4F) else Color(0xFFE08A00),
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        if (!ok) FilledTonalButton(onClick = onFix) { Text(stringResource(R.string.grant)) }
    }
}

/** Every permission the app needs, each with its fix button. Used in the status card and the setup wizard. */
@Composable
fun PermissionChecklist(state: AppState, items: Set<PermItem> = PermItem.entries.toSet()) {
    val context = LocalContext.current
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { state.refresh() }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { state.refresh() }
    val p = state.perms

    Column {
        if (PermItem.ROLE in items) PermRow(
            stringResource(R.string.perm_role_title),
            stringResource(if (p.roleAvailable) R.string.perm_role_desc else R.string.perm_role_unavailable),
            p.role || !p.roleAvailable,
        ) {
            val rm = context.getSystemService(RoleManager::class.java)
            roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        }
        if (PermItem.CONTACTS in items) PermRow(
            stringResource(R.string.perm_contacts_title),
            stringResource(R.string.perm_contacts_desc),
            p.contacts,
        ) { permLauncher.launch(Manifest.permission.READ_CONTACTS) }
        if (PermItem.LISTENER in items) PermRow(
            stringResource(R.string.perm_listener_title),
            stringResource(R.string.perm_listener_desc),
            p.listener,
        ) { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        if (PermItem.BATTERY in items) PermRow(
            stringResource(R.string.perm_battery_title),
            stringResource(R.string.perm_battery_desc),
            p.battery,
        ) {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
            )
        }
        if (PermItem.NOTIFICATIONS in items) PermRow(
            stringResource(R.string.perm_notif_title),
            stringResource(R.string.perm_notif_desc),
            p.notifications,
        ) { if (Build.VERSION.SDK_INT >= 33) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    }
}

enum class PermItem { ROLE, CONTACTS, LISTENER, BATTERY, NOTIFICATIONS }

/** Number + name fields with "pick from contacts". Returns through [onAdd] only valid E.164 numbers. */
@Composable
fun AddNumberFields(onAdd: (e164: String, name: String) -> Unit, buttonLabel: String = stringResource(R.string.add)) {
    val context = LocalContext.current
    var number by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ),
            null, null, null,
        )?.use { c ->
            if (c.moveToFirst()) {
                number = c.getString(0).orEmpty()
                name = c.getString(1).orEmpty()
                error = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = number,
            onValueChange = { number = it; error = false },
            label = { Text(stringResource(R.string.field_number)) },
            placeholder = { Text(stringResource(R.string.field_number_hint)) },
            isError = error,
            supportingText = if (error) {{ Text(stringResource(R.string.field_number_invalid)) }} else null,
            singleLine = true,
            textStyle = ltrStyle(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.field_name_optional)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = {
                val e164 = PhoneNormalizer.normalize(number)
                if (PhoneNormalizer.isValidForStorage(e164)) {
                    onAdd(e164!!, name.trim())
                    number = ""; name = ""
                } else {
                    error = true
                }
            }) { Text(buttonLabel) }
            TextButton(onClick = {
                picker.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
            }) { Text(stringResource(R.string.from_contacts)) }
        }
    }
}

@Composable
fun AddDialog(state: AppState, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_number_title)) },
        text = {
            AddNumberFields(onAdd = { e164, name ->
                state.add(e164, name)
                onDismiss()
            })
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
