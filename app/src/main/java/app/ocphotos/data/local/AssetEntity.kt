package app.ocphotos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Local mirror of an indexed photo/video asset (see ocapps internal/photos/store Asset).
// `takenAt` is Unix SECONDS (backend stores time.Unix()), not millis.
@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: Long,
    val path: String,
    val filename: String,
    val mediaType: String,
    val takenAt: Long,
    val width: Int,
    val height: Int,
    val camera: String?,
    val lens: String?,
    val iso: Int?,
    val aperture: String?,
    val shutter: String?,
    val focal: String?,
    val lat: Double?,
    val lon: Double?,
    val size: Long,
    val favorite: Boolean,
    val archived: Boolean,
)
