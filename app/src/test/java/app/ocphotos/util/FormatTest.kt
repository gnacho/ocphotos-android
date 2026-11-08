package app.ocphotos.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class FormatTest {

    @Test
    fun relativeTime() {
        val now = System.currentTimeMillis() / 1000
        assertEquals("now", TimeFormat.formatRelativeTime(now - 30))
        assertEquals("5 min ago", TimeFormat.formatRelativeTime(now - 300))
        assertEquals("2 h ago", TimeFormat.formatRelativeTime(now - 7200))
        assertEquals("3 d ago", TimeFormat.formatRelativeTime(now - 3 * 86400))
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
