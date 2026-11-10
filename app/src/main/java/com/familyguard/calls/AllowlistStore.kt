package com.familyguard.calls

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** One allowed family number. [number] is always E.164. */
data class FamilyEntry(val number: String, val name: String)

/**
 * Local storage (SharedPreferences, private to the app). Nothing leaves the device.
 * Uses device-protected storage so screening also works right after a reboot, before first unlock.
 */
class AllowlistStore(context: Context) {

    private val prefs: SharedPreferences =
        context.createDeviceProtectedStorageContext()
            .getSharedPreferences("family_allowlist", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(v) = prefs.edit { putBoolean(KEY_ENABLED, v) }

    /** Allow calls that arrive with no number (hidden / private / unknown). Default: reject. */
    var allowHidden: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_HIDDEN, false)
        set(v) = prefs.edit { putBoolean(KEY_ALLOW_HIDDEN, v) }

    /** Do not show a missed-call notification for rejected calls. Default: hide it. */
    var skipNotification: Boolean
        get() = prefs.getBoolean(KEY_SKIP_NOTIFICATION, true)
        set(v) = prefs.edit { putBoolean(KEY_SKIP_NOTIFICATION, v) }

    /** Do not write rejected calls to the call log. Default: keep them in the log. */
    var skipCallLog: Boolean
        get() = prefs.getBoolean(KEY_SKIP_CALL_LOG, false)
        set(v) = prefs.edit { putBoolean(KEY_SKIP_CALL_LOG, v) }

    /** Also reject WhatsApp / Telegram / other app calls (via the notification listener). Default: on. */
    var blockAppCalls: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_APP_CALLS, true)
        set(v) = prefs.edit { putBoolean(KEY_BLOCK_APP_CALLS, v) }

    /** Contacts marked as favourite (★) in the Contacts app count as family. Default: on. */
    var allowStarred: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_STARRED, true)
        set(v) = prefs.edit { putBoolean(KEY_ALLOW_STARRED, v) }

    /** Short service numbers (up to 6 digits: 911, 999, 1909 ...) are allowed. Default: on. */
    var allowShort: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_SHORT, true)
        set(v) = prefs.edit { putBoolean(KEY_ALLOW_SHORT, v) }

    /** Allowed prefixes, e.g. "+966920" (unified company numbers). [FamilyEntry.number] holds the prefix. */
    fun prefixes(): List<FamilyEntry> =
        (prefs.getStringSet(KEY_PREFIXES, emptySet()) ?: emptySet())
            .map { raw ->
                val parts = raw.split(SEP, limit = 2)
                FamilyEntry(parts[0], parts.getOrElse(1) { "" })
            }
            .sortedBy { it.number }

    /** True if a normalized incoming number starts with an allowed prefix, or is an allowed short number. */
    fun matchesRule(normalized: String): Boolean =
        (allowShort && PhoneNormalizer.isShortNumber(normalized)) ||
            (normalized.startsWith("+") && prefixes().any { normalized.startsWith(it.number) })

    fun addPrefix(prefix: String, label: String) {
        val updated = prefixes().filterNot { it.number == prefix } + FamilyEntry(prefix, label.replace(SEP, " ").trim())
        prefs.edit { putStringSet(KEY_PREFIXES, updated.map { it.number + SEP + it.name }.toSet()) }
    }

    fun removePrefix(prefix: String) {
        val updated = prefixes().filterNot { it.number == prefix }
        prefs.edit { putStringSet(KEY_PREFIXES, updated.map { it.number + SEP + it.name }.toSet()) }
    }

    /** First-run setup wizard finished. */
    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(v) = prefs.edit { putBoolean(KEY_ONBOARDING_DONE, v) }

    /** Keep a permanent foreground service so the system does not kill the app. Default: on. */
    var keepAlive: Boolean
        get() = prefs.getBoolean(KEY_KEEP_ALIVE, true)
        set(v) = prefs.edit { putBoolean(KEY_KEEP_ALIVE, v) }

    fun entries(): List<FamilyEntry> =
        (prefs.getStringSet(KEY_NUMBERS, emptySet()) ?: emptySet())
            .map { raw ->
                val parts = raw.split(SEP, limit = 2)
                FamilyEntry(parts[0], parts.getOrElse(1) { "" })
            }
            .sortedWith(compareBy({ it.name.ifEmpty { "￿" } }, { it.number }))

    fun contains(e164: String): Boolean = entries().any { it.number == e164 }

    /** Adds or renames. [e164] must already be normalized. */
    fun add(e164: String, name: String) {
        val cleanName = name.replace(SEP, " ").trim()
        val updated = entries().filterNot { it.number == e164 } + FamilyEntry(e164, cleanName)
        save(updated)
    }

    fun remove(e164: String) = save(entries().filterNot { it.number == e164 })

    private fun save(list: List<FamilyEntry>) {
        prefs.edit { putStringSet(KEY_NUMBERS, list.map { it.number + SEP + it.name }.toSet()) }
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
        const val KEY_ALLOW_HIDDEN = "allow_hidden"
        const val KEY_SKIP_NOTIFICATION = "skip_notification"
        const val KEY_SKIP_CALL_LOG = "skip_call_log"
        const val KEY_BLOCK_APP_CALLS = "block_app_calls"
        const val KEY_KEEP_ALIVE = "keep_alive"
        const val KEY_ALLOW_STARRED = "allow_starred"
        const val KEY_ONBOARDING_DONE = "onboarding_done"
        const val KEY_ALLOW_SHORT = "allow_short"
        const val KEY_PREFIXES = "prefixes"
        const val KEY_NUMBERS = "numbers"
        const val SEP = "\t"
    }
}
