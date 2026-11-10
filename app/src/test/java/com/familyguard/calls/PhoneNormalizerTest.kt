package com.familyguard.calls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNormalizerTest {

    private val expected = "+966512345678"

    @Test fun localMobile() = assertEquals(expected, PhoneNormalizer.normalize("0512345678"))
    @Test fun localWithSpacesAndDashes() = assertEquals(expected, PhoneNormalizer.normalize("051-234 5678"))
    @Test fun bareNineDigits() = assertEquals(expected, PhoneNormalizer.normalize("512345678"))
    @Test fun plusForm() = assertEquals(expected, PhoneNormalizer.normalize("+966 51 234 5678"))
    @Test fun doubleZero() = assertEquals(expected, PhoneNormalizer.normalize("00966512345678"))
    @Test fun noPlus() = assertEquals(expected, PhoneNormalizer.normalize("966512345678"))
    @Test fun plusWithTrunkZero() = assertEquals(expected, PhoneNormalizer.normalize("+966 0512345678"))
    @Test fun brackets() = assertEquals(expected, PhoneNormalizer.normalize("(+966) 51-234-5678"))
    @Test fun arabicDigits() = assertEquals(expected, PhoneNormalizer.normalize("٠٥١٢٣٤٥٦٧٨"))
    @Test fun foreignNumberKept() = assertEquals("+971501234567", PhoneNormalizer.normalize("00971 50 123 4567"))
    @Test fun saudiLandline() = assertEquals("+966112345678", PhoneNormalizer.normalize("011 234 5678"))
    @Test fun hiddenIsNull() = assertNull(PhoneNormalizer.normalize(""))
    @Test fun nullIsNull() = assertNull(PhoneNormalizer.normalize(null))
    @Test fun noDigitsIsNull() = assertNull(PhoneNormalizer.normalize("Private"))
    @Test fun unifiedNumber920() = assertEquals("+966920012345", PhoneNormalizer.normalize("920012345"))
    @Test fun tollFree800() = assertEquals("+9668001234567", PhoneNormalizer.normalize("800 123 4567"))
    @Test fun shortCodeKept() = assertEquals("999", PhoneNormalizer.normalize("999"))

    @Test fun prefixLocal01() = assertEquals("+9661", PhoneNormalizer.normalizePrefix("01"))
    @Test fun prefixRiyadh011() = assertEquals("+96611", PhoneNormalizer.normalizePrefix("011"))
    @Test fun prefix9661() = assertEquals("+9661", PhoneNormalizer.normalizePrefix("9661"))
    @Test fun prefixPlus9661() = assertEquals("+9661", PhoneNormalizer.normalizePrefix("+966 1"))
    @Test fun prefix920() = assertEquals("+966920", PhoneNormalizer.normalizePrefix("920"))
    @Test fun prefix800() = assertEquals("+966800", PhoneNormalizer.normalizePrefix("800"))
    @Test fun prefixForeign() = assertEquals("+971", PhoneNormalizer.normalizePrefix("00971"))
    @Test fun prefixEmpty() = assertNull(PhoneNormalizer.normalizePrefix(" - "))
}
