package com.familyguard.calls

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restarts the keep-alive service after reboot (even before first unlock) and after app updates. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        KeepAliveService.startIfEnabled(context)
    }
}
