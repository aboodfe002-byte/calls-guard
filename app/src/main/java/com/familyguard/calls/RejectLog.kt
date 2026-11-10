package com.familyguard.calls

import android.content.Context
import java.io.File
import java.util.Calendar

/** One rejected call. [source] is [RejectLog.SOURCE_PHONE] or the package name of the calling app. */
data class RejectedCall(val time: Long, val source: String, val number: String?, val name: String?)

/**
 * Local log of rejected calls (last [MAX] entries), stored in device-protected storage.
 * Plain text, one call per line, never leaves the device.
 */
class RejectLog(context: Context) {

    private val file = File(context.createDeviceProtectedStorageContext().filesDir, "rejected.log")

    fun add(call: RejectedCall) = synchronized(LOCK) {
        val line = listOf(
            call.time.toString(), call.source, call.number.orEmpty(), call.name.orEmpty().replace('\t', ' ')
        ).joinToString("\t").replace('\n', ' ')
        runCatching {
            file.appendText(line + "\n")
            val lines = file.readLines()
            if (lines.size > MAX + 100) file.writeText(lines.takeLast(MAX).joinToString("\n", postfix = "\n"))
        }
        Unit
    }

    /** Newest first. */
    fun all(): List<RejectedCall> = synchronized(LOCK) {
        if (!file.exists()) return emptyList()
        runCatching {
            file.readLines().mapNotNull { line ->
                val p = line.split('\t')
                val time = p.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
                RejectedCall(
                    time, p.getOrElse(1) { SOURCE_PHONE },
                    p.getOrNull(2)?.ifEmpty { null }, p.getOrNull(3)?.ifEmpty { null }
                )
            }.asReversed()
        }.getOrDefault(emptyList())
    }

    fun clear() = synchronized(LOCK) { runCatching { file.delete() }; Unit }

    companion object {
        const val SOURCE_PHONE = "phone"
        private const val MAX = 500
        private val LOCK = Any()

        fun startOfToday(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
