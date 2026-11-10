package com.familyguard.calls

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Snapshot of everything the app needs to work fully. */
data class Perms(
    val roleAvailable: Boolean,
    val role: Boolean,
    val contacts: Boolean,
    val listener: Boolean,
    val battery: Boolean,
    val notifications: Boolean,
) {
    /** Number of missing items (an unavailable role is not counted: nothing can fix it). */
    val missing: Int
        get() = listOf(role || !roleAvailable, contacts, listener, battery, notifications).count { !it }

    companion object {
        fun read(context: Context): Perms {
            val rm = context.getSystemService(RoleManager::class.java)
            val available = rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
            fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
            return Perms(
                roleAvailable = available,
                role = available && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING),
                contacts = granted(Manifest.permission.READ_CONTACTS),
                listener = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName),
                battery = context.getSystemService(PowerManager::class.java)
                    .isIgnoringBatteryOptimizations(context.packageName),
                notifications = Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS),
            )
        }
    }
}
