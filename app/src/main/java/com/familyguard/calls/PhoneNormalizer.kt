package com.familyguard.calls

/**
 * Converts phone numbers to E.164 (e.g. +9665XXXXXXXX), defaulting to Saudi Arabia.
 *
 * Handles: spaces, dashes, brackets, Arabic-Indic digits (٠-٩), "+966", "00966", "966...",
 * local "05XXXXXXXX", bare "5XXXXXXXX", and the common "+966 05..." mistake.
 */
object PhoneNormalizer {

    const val DEFAULT_COUNTRY_CODE = "966"

    /** Returns the E.164 form, or null if there are no digits at all (hidden/unknown number). */
    fun normalize(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim()
        val hasPlus = trimmed.startsWith("+")

        // Keep digits only; Character.digit also converts Arabic-Indic / Persian digits.
        val digits = buildString {
            for (c in trimmed) {
                val d = Character.digit(c, 10)
                if (d >= 0) append(d)
            }
        }
        if (digits.isEmpty()) return null

        val e164 = when {
            hasPlus -> "+$digits"
            digits.startsWith("00") -> "+" + digits.drop(2)
            digits.startsWith("0") -> "+$DEFAULT_COUNTRY_CODE" + digits.drop(1)
            digits.startsWith(DEFAULT_COUNTRY_CODE) && digits.length >= 11 -> "+$digits"
            digits.length == 9 -> "+$DEFAULT_COUNTRY_CODE$digits"   // national number without the leading 0 (also 920XXXXXX)
            digits.length == 10 && digits.startsWith("800") -> "+$DEFAULT_COUNTRY_CODE$digits"  // Saudi toll-free 800XXXXXXX
            digits.length <= 8 -> return digits   // short service codes (e.g. 1909) are kept as-is
            else -> "+$digits"
        }

        // "+966 05XXXXXXXX" -> "+9665XXXXXXXX"
        return if (e164.startsWith("+${DEFAULT_COUNTRY_CODE}0")) {
            "+$DEFAULT_COUNTRY_CODE" + e164.drop(DEFAULT_COUNTRY_CODE.length + 2)
        } else {
            e164
        }
    }

    /**
     * Normalizes an allowed *prefix* to the start of an E.164 number:
     * "01" / "011" -> "+9661" / "+96611", "9661" -> "+9661", "00971" -> "+971",
     * "920" -> "+966920", "800" -> "+966800", "+44" -> "+44".
     */
    fun normalizePrefix(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim()
        val digits = buildString {
            for (c in trimmed) {
                val d = Character.digit(c, 10)
                if (d >= 0) append(d)
            }
        }
        if (digits.isEmpty()) return null
        return when {
            trimmed.startsWith("+") -> "+$digits"
            digits.startsWith("00") -> ("+" + digits.drop(2)).takeIf { it.length > 1 }
            digits.startsWith("0") -> "+$DEFAULT_COUNTRY_CODE" + digits.drop(1)
            digits.startsWith(DEFAULT_COUNTRY_CODE) -> "+$digits"
            else -> "+$DEFAULT_COUNTRY_CODE$digits"
        }
    }

    /** Short service numbers (police, emergency, government short codes): up to 6 digits, no country code. */
    fun isShortNumber(normalized: String?): Boolean =
        normalized != null && !normalized.startsWith("+") && normalized.length in 2..6

    /** True if [e164] looks like a full international number we can store. */
    fun isValidForStorage(e164: String?): Boolean =
        e164 != null && e164.startsWith("+") && e164.length in 9..16 &&
            e164.drop(1).all { it.isDigit() }
}
