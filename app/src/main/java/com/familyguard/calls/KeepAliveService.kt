package com.familyguard.calls

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

/**
 * A small permanent foreground service. It does no work; it only keeps the app's process at
 * foreground priority so One UI's battery manager does not kill it (the call screening service and
 * the notification listener live in the same process).
 *
 * Since Android 14 the user can swipe a foreground-service notification away. When that happens,
 * [NotificationDismissedReceiver] posts it again immediately. No timer, no polling: code runs only
 * at the moment of the swipe.
 */
class KeepAliveService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!AllowlistStore(this).keepAlive) {
            stopSelf()
            return START_NOT_STICKY
        }
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        runCatching { ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(this), type) }
            .onFailure { Log.w(TAG, "startForeground failed", it) }
        return START_STICKY
    }

    /** Fired by the notification's delete intent when the user swipes it away or taps "clear all". */
    class NotificationDismissedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!AllowlistStore(context).keepAlive) return
            // The service is normally still running: just post the same notification again.
            runCatching {
                context.getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, buildNotification(context))
            }.onFailure { Log.w(TAG, "re-post failed", it) }
            // If the process had been restarted without the service, bring the service back too.
            startIfEnabled(context)
        }
    }

    companion object {
        private const val TAG = "FamilyCallGuard"
        private const val CHANNEL_ID = "keepalive"
        private const val NOTIFICATION_ID = 1

        private fun createChannel(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID, context.getString(R.string.keepalive_channel), NotificationManager.IMPORTANCE_MIN
                ).apply { setShowBadge(false) }
            )
        }

        fun buildNotification(context: Context): Notification {
            createChannel(context)
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
            val onDismiss = PendingIntent.getBroadcast(
                context, 1, Intent(context, NotificationDismissedReceiver::class.java), PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_shield)
                .setColor(0xFF2A2F55.toInt())   // navy accent matching the app icon tile
                .setContentTitle(context.getString(R.string.keepalive_title))
                .setContentText(context.getString(R.string.keepalive_text))
                .setContentIntent(open)
                .setDeleteIntent(onDismiss)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        }

        fun startIfEnabled(context: Context) {
            if (!AllowlistStore(context).keepAlive) return
            runCatching {
                ContextCompat.startForegroundService(context, Intent(context, KeepAliveService::class.java))
            }.onFailure { Log.w(TAG, "could not start keep-alive", it) }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, KeepAliveService::class.java))
        }
    }
}
