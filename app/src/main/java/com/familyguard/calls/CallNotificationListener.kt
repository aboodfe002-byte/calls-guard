package com.familyguard.calls

import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.os.BundleCompat

/**
 * Rejects incoming calls from WhatsApp, Telegram, Signal, Messenger, etc.
 *
 * Those apps do not go through Android's call screening, so instead we watch for their
 * "incoming call" notification and press its Decline button, unless the caller is in the family list.
 * We only ever press Decline/Reject on a RINGING call, never "Hang up", so your own outgoing
 * app calls and answered family calls are never touched.
 */
class CallNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        // Keep the process warm alongside the listener.
        KeepAliveService.startIfEnabled(this)
    }

    override fun onListenerDisconnected() {
        // Ask Android to bind us again if we get disconnected.
        requestRebind(ComponentName(this, CallNotificationListener::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        runCatching { handle(sbn) }.onFailure { Log.w(TAG, "handle failed", it) }
    }

    private fun handle(sbn: StatusBarNotification) {
        val n = sbn.notification ?: return
        if (n.category != Notification.CATEGORY_CALL) return
        if (sbn.packageName == packageName || sbn.packageName in phonePackages()) return  // cellular calls: CallScreeningService handles them

        val store = AllowlistStore(this)
        if (!store.enabled || !store.blockAppCalls) return

        val extras = n.extras ?: Bundle.EMPTY
        val callType = extras.getInt(EXTRA_CALL_TYPE, 0)   // CallStyle: 1 = incoming, 2 = ongoing, 3 = screening
        if (callType == CALL_TYPE_ONGOING) return

        val decline = findDeclineIntent(n, extras) ?: return   // no Decline button = not a ringing call
        if (callType == 0 && n.fullScreenIntent == null && !hasAnswerAction(n)) return

        if (isFamily(this, store, extras)) return

        Log.i(TAG, "Declining app call from ${sbn.packageName}")
        decline.send()

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val number = PhoneNormalizer.normalize(title)?.takeIf { it.startsWith("+") }
        RejectLog(this).add(
            RejectedCall(System.currentTimeMillis(), sbn.packageName, number, title.takeIf { number == null })
        )
    }

    private fun findDeclineIntent(n: Notification, extras: Bundle): PendingIntent? {
        BundleCompat.getParcelable(extras, EXTRA_DECLINE_INTENT, PendingIntent::class.java)?.let { return it }
        return n.actions?.firstOrNull { a -> matches(a.title, DECLINE_WORDS) }?.actionIntent
    }

    private fun hasAnswerAction(n: Notification) =
        BundleCompat.getParcelable(n.extras ?: Bundle.EMPTY, EXTRA_ANSWER_INTENT, PendingIntent::class.java) != null ||
            n.actions?.any { matches(it.title, ANSWER_WORDS) } == true

    private fun matches(title: CharSequence?, words: List<String>): Boolean {
        val t = title?.toString()?.trim()?.lowercase() ?: return false
        return words.any { t.contains(it) }
    }

    /** Default dialer + known system phone UIs (Samsung / AOSP / Google). */
    private fun phonePackages(): Set<String> {
        val dialer = runCatching { getSystemService(TelecomManager::class.java)?.defaultDialerPackage }.getOrNull()
        return SYSTEM_PHONE_PACKAGES + listOfNotNull(dialer)
    }

    companion object {
        private const val TAG = "FamilyCallGuard"

        private const val EXTRA_CALL_TYPE = "android.callType"
        private const val EXTRA_CALL_PERSON = "android.callPerson"
        private const val EXTRA_DECLINE_INTENT = "android.declineIntent"
        private const val EXTRA_ANSWER_INTENT = "android.answerIntent"
        private const val CALL_TYPE_ONGOING = 2

        private val DECLINE_WORDS = listOf("decline", "reject", "رفض", "refuser", "ablehnen", "rechazar")
        private val ANSWER_WORDS = listOf("answer", "accept", "رد", "إجابة", "اجابة", "قبول")

        private val SYSTEM_PHONE_PACKAGES = setOf(
            "com.samsung.android.incallui",
            "com.samsung.android.dialer",
            "com.android.incallui",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.server.telecom",
            "com.android.phone",
        )

        /**
         * Family if any number attached to the notification is allowlisted, or if the caller name shown
         * (WhatsApp uses the name saved in your contacts) matches a family entry's name or contact name.
         */
        fun isFamily(context: Context, store: AllowlistStore, extras: Bundle): Boolean {
            val entries = store.entries()
            val starred = if (store.allowStarred) ContactsHelper.starredContacts(context) else emptyList()
            val allowedNumbers = entries.map { it.number }.toSet() + starred.flatMap { it.numbers }

            // 1) Numbers from Person objects (CallStyle person / people list).
            val persons = buildList {
                BundleCompat.getParcelable(extras, EXTRA_CALL_PERSON, Person::class.java)?.let(::add)
                BundleCompat.getParcelableArrayList(extras, Notification.EXTRA_PEOPLE_LIST, Person::class.java)
                    ?.let(::addAll)
            }
            val personNumbers = persons.flatMap { ContactsHelper.numbersForPersonUri(context, it.uri) }

            // 2) Title itself may be a phone number (caller not saved in contacts).
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
            // Only treat the title as a number if it contains nothing but phone characters ("Ali 123" is a name).
            val titleIsNumber = title.isNotEmpty() &&
                title.all { it.isWhitespace() || it in "+-()" || Character.digit(it, 10) >= 0 }
            val candidateNumbers = (personNumbers + listOfNotNull(title.takeIf { titleIsNumber }))
                .mapNotNull { PhoneNormalizer.normalize(it) }
            if (candidateNumbers.any { it in allowedNumbers || store.matchesRule(it) }) return true

            // 3) Name match.
            val shownNames = (persons.mapNotNull { it.name?.toString() } + title)
                .map(::normName).filter { it.isNotEmpty() }.toSet()
            if (shownNames.isEmpty()) return false
            val allowedNames = entries.flatMap { e ->
                listOf(e.name) + ContactsHelper.displayNamesFor(context, e.number)
            }.plus(starred.map { it.name }).map(::normName).filter { it.isNotEmpty() }.toSet()
            return shownNames.any { it in allowedNames }
        }

        private fun normName(s: String) = s.trim().lowercase().replace(Regex("\\s+"), " ")
    }
}
