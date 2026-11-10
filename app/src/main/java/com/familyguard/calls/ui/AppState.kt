package com.familyguard.calls.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.familyguard.calls.AllowlistStore
import com.familyguard.calls.ContactsHelper
import com.familyguard.calls.FamilyEntry
import com.familyguard.calls.KeepAliveService
import com.familyguard.calls.Perms
import com.familyguard.calls.RejectLog
import com.familyguard.calls.RejectedCall
import com.familyguard.calls.StarredContact

/** Observable UI state backed by [AllowlistStore] and [RejectLog]. */
class AppState(private val context: Context) {

    val store = AllowlistStore(context)
    private val log = RejectLog(context)

    var perms by mutableStateOf(Perms.read(context)); private set
    var entries by mutableStateOf(store.entries()); private set
    var starred by mutableStateOf(emptyList<StarredContact>()); private set
    var calls by mutableStateOf(emptyList<RejectedCall>()); private set
    var onboardingDone by mutableStateOf(store.onboardingDone); private set

    /** A boolean setting that writes through to the store. */
    inner class Toggle(private val read: () -> Boolean, private val write: (Boolean) -> Unit) {
        var value by mutableStateOf(read()); private set
        fun update(v: Boolean) {
            write(v)
            value = v
            afterToggle()
        }
    }

    val enabled = Toggle({ store.enabled }, { store.enabled = it })
    val blockAppCalls = Toggle({ store.blockAppCalls }, { store.blockAppCalls = it })
    val allowStarred = Toggle({ store.allowStarred }, { store.allowStarred = it })
    val allowHidden = Toggle({ store.allowHidden }, { store.allowHidden = it })
    val allowShort = Toggle({ store.allowShort }, { store.allowShort = it })

    var prefixes by mutableStateOf(store.prefixes()); private set

    fun addPrefix(prefix: String, label: String) {
        store.addPrefix(prefix, label)
        prefixes = store.prefixes()
    }

    fun removePrefix(entry: FamilyEntry) {
        store.removePrefix(entry.number)
        prefixes = store.prefixes()
    }
    val skipNotification = Toggle({ store.skipNotification }, { store.skipNotification = it })
    val skipCallLog = Toggle({ store.skipCallLog }, { store.skipCallLog = it })
    val keepAlive = Toggle({ store.keepAlive }, { store.keepAlive = it })

    private fun afterToggle() {
        if (store.keepAlive) KeepAliveService.startIfEnabled(context) else KeepAliveService.stop(context)
        refreshStarred()
    }

    fun refresh() {
        perms = Perms.read(context)
        entries = store.entries()
        prefixes = store.prefixes()
        calls = log.all()
        refreshStarred()
    }

    private fun refreshStarred() {
        starred = if (store.allowStarred) ContactsHelper.starredContacts(context) else emptyList()
    }

    fun add(e164: String, name: String) {
        store.add(e164, name)
        entries = store.entries()
    }

    fun remove(entry: FamilyEntry) {
        store.remove(entry.number)
        entries = store.entries()
    }

    fun clearLog() {
        log.clear()
        calls = emptyList()
    }

    fun finishOnboarding() {
        store.onboardingDone = true
        onboardingDone = true
        refresh()
    }
}
