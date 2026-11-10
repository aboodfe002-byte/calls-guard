package com.familyguard.calls

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class StarredContact(val name: String, val numbers: List<String>)

/** Small helpers over the phone's contacts (needs READ_CONTACTS; returns empty results without it). */
object ContactsHelper {

    private fun canRead(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    /** Display names saved in the phone for [e164] (e.g. "أمي"). WhatsApp shows these names in its call notification. */
    fun displayNamesFor(context: Context, e164: String): List<String> {
        if (!canRead(context)) return emptyList()
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(e164))
        val names = mutableListOf<String>()
        runCatching {
            context.contentResolver.query(
                uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
            )?.use { c -> while (c.moveToNext()) c.getString(0)?.let(names::add) }
        }
        return names
    }

    /** True if [number] belongs to a contact marked as favourite (★) in the Contacts app. */
    fun isStarredNumber(context: Context, number: String?): Boolean {
        if (number.isNullOrBlank() || !canRead(context)) return false
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return runCatching {
            context.contentResolver.query(
                uri, arrayOf(ContactsContract.PhoneLookup.STARRED), null, null, null
            )?.use { c ->
                var starred = false
                while (c.moveToNext()) if (c.getInt(0) == 1) starred = true
                starred
            } ?: false
        }.getOrDefault(false)
    }

    /** All favourite (★) contacts with their numbers in E.164, sorted by name. */
    fun starredContacts(context: Context): List<StarredContact> {
        if (!canRead(context)) return emptyList()
        val byId = LinkedHashMap<Long, Pair<String, MutableSet<String>>>()
        runCatching {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                ),
                "${ContactsContract.CommonDataKinds.Phone.STARRED} = 1",
                null,
                null
            )?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    val name = c.getString(1).orEmpty()
                    val number = PhoneNormalizer.normalize(c.getString(2)) ?: continue
                    byId.getOrPut(id) { name to mutableSetOf() }.second.add(number)
                }
            }
        }
        return byId.values.map { StarredContact(it.first, it.second.sorted()) }.sortedBy { it.name }
    }

    /** Phone numbers behind a Person URI from a notification: "tel:..." or a contacts lookup URI. */
    fun numbersForPersonUri(context: Context, raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        val uri = Uri.parse(raw)
        if (uri.scheme == "tel") return listOfNotNull(uri.schemeSpecificPart)
        if (uri.scheme != "content" || !canRead(context)) return emptyList()

        return runCatching {
            val contactUri = ContactsContract.Contacts.lookupContact(context.contentResolver, uri) ?: uri
            val id = ContentUris.parseId(contactUri)
            val out = mutableListOf<String>()
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(id.toString()),
                null
            )?.use { c -> while (c.moveToNext()) c.getString(0)?.let(out::add) }
            out
        }.getOrDefault(emptyList())
    }
}
