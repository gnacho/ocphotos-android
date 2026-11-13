package app.ocphotos.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class FormatTest {

    @Test
    fun monthShortNameFollowsTheLocale() {
        val original = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("es-ES"))
            assertEquals("ene", TimeFormat.monthShortName(1))
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("en-US"))
            assertEquals("Jan", TimeFormat.monthShortName(1))
        } finally {
            java.util.Locale.setDefault(original)
        }
    }

    @Test
    fun dayKeyUsesLocalDate() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        assertEquals("1970-01-01", TimeFormat.dayKey(0))
    }

    @Test
    fun yearUsesLocalDate() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        assertEquals(1970, TimeFormat.year(0))
    }

    @Test
    fun byteFormat() {
        assertEquals("500 B", ByteFormat.human(500))
        assertEquals("2.0 KB", ByteFormat.human(2048))
        assertEquals("1.0 MB", ByteFormat.human(1024 * 1024))
        assertEquals("1.5 GB", ByteFormat.human(1536L * 1024 * 1024))
    }

    @Test
    fun geoRound() {
        assertEquals(40.12, Geo.round(40.12345), 0.0)
        assertEquals(2.0, Geo.round(1.999), 0.0)
    }
}
