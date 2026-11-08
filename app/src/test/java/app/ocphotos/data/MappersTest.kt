package app.ocphotos.data

import app.ocphotos.testutil.assetDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun assetDtoToEntity() {
        val dto = assetDto(7, 12345, favorite = true)
        val e = dto.toEntity()
        assertEquals(7L, e.id)
        assertEquals("x7.jpg", e.filename)
        assertEquals(12345L, e.takenAt)
        assertEquals("image", e.mediaType)
        assertTrue(e.favorite)
        assertFalse(e.archived)
    }
}
