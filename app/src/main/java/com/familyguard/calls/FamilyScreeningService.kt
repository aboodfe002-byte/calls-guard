package com.familyguard.calls

import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * Bound by Android for every incoming call once this app holds ROLE_CALL_SCREENING
 * (and READ_CONTACTS, otherwise calls from saved contacts are not passed here).
 * Must answer within ~5 seconds; everything here is local and instant.
 */
class FamilyScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val store = AllowlistStore(this)

        // Never interfere with outgoing calls.
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, allow())
            return
        }

        // Feature switched off -> allow everything.
        if (!store.enabled) {
            respondToCall(callDetails, allow())
            return
        }

        val raw = callDetails.handle?.schemeSpecificPart
        val incoming = PhoneNormalizer.normalize(raw)

        val allowed = when {
            // Hidden / private / unknown caller: arrives with no number at all.
            incoming == null -> store.allowHidden
            store.contains(incoming) -> true
            store.matchesRule(incoming) -> true          // allowed prefix or short service number
            store.allowStarred && ContactsHelper.isStarredNumber(this, incoming) -> true
            else -> false
        }

        respondToCall(callDetails, if (allowed) allow() else reject(store))

        if (!allowed) {
            val name = incoming?.let { ContactsHelper.displayNamesFor(this, it).firstOrNull() }
            RejectLog(this).add(RejectedCall(System.currentTimeMillis(), RejectLog.SOURCE_PHONE, incoming, name))
        }
    }

    private fun allow(): CallResponse = CallResponse.Builder()
        .setDisallowCall(false)
        .setRejectCall(false)
        .setSkipCallLog(false)
        .setSkipNotification(false)
        .build()

    private fun reject(store: AllowlistStore): CallResponse = CallResponse.Builder()
        .setDisallowCall(true)                       // do not let the call through
        .setRejectCall(true)                         // actively reject (caller gets busy/voicemail), not just silence
        .setSkipCallLog(store.skipCallLog)           // default false: rejected calls stay visible in the call log
        .setSkipNotification(store.skipNotification) // default true: no missed-call notification
        .build()
}
