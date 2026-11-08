package app.ocphotos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Local mirror of an album (see ocapps internal/photos/store Album).
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val count: Int,
    val coverId: Long?,
    val createdAt: Long,
)
