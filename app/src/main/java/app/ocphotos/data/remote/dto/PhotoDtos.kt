package app.ocphotos.data.remote.dto

import kotlinx.serialization.Serializable

// JSON of an indexed asset (ocapps internal/photos/store Asset). takenAt is Unix SECONDS.
@Serializable
data class AssetDto(
    val id: Long,
    val path: String,
    val filename: String,
    val mediaType: String = "image",
    val takenAt: Long,
    val width: Int = 0,
    val height: Int = 0,
    val camera: String? = null,
    val lens: String? = null,
    val iso: Int? = null,
    val aperture: String? = null,
    val shutter: String? = null,
    val focal: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val size: Long = 0,
    val favorite: Boolean = false,
    val archived: Boolean = false,
)

@Serializable
data class AssetsResponse(val assets: List<AssetDto> = emptyList())

@Serializable
data class ResolveResponse(val asset: AssetDto? = null)

@Serializable
data class StatsResponse(
    val assets: Long = 0,
    val favorites: Long = 0,
    val geo: Long = 0,
    val exifPending: Long = 0,
    val videos: Long = 0,
)

@Serializable
data class AlbumDto(
    val id: Long,
    val name: String,
    val count: Int = 0,
    val coverId: Long? = null,
    val createdAt: Long = 0,
)

@Serializable
data class AlbumsResponse(val albums: List<AlbumDto> = emptyList())

@Serializable
data class CreateAlbumResponse(val id: Long)

@Serializable
data class TagDto(val name: String, val count: Int = 0)

@Serializable
data class TagsResponse(val tags: List<TagDto> = emptyList())

@Serializable
data class AssetTagsResponse(val tags: List<String> = emptyList())

@Serializable
data class PlaceDto(
    val lat: Double,
    val lon: Double,
    val count: Int = 0,
    val coverId: Long = 0,
    val name: String? = null,
)

@Serializable
data class PlacesResponse(val places: List<PlaceDto> = emptyList())

@Serializable
data class MonthDto(val month: Int, val count: Long = 0)

@Serializable
data class YearDto(val year: Int, val count: Long = 0, val months: List<MonthDto> = emptyList())

@Serializable
data class CalendarResponse(val years: List<YearDto> = emptyList())

@Serializable
data class MemoriesResponse(val scope: String = "", val assets: List<AssetDto> = emptyList())

@Serializable
data class OnThisDayResponse(val assets: List<AssetDto> = emptyList(), val days: Int = 0)

@Serializable
data class FolderDto(val name: String, val path: String, val count: Int = 0)

@Serializable
data class FoldersResponse(
    val path: String = "",
    val folders: List<FolderDto> = emptyList(),
    val assets: List<AssetDto> = emptyList(),
)

@Serializable
data class DuplicatesResponse(
    val groups: List<List<AssetDto>> = emptyList(),
    val computed: Int = 0,
    val pending: Int = 0,
)

@Serializable
data class GeoResponse(val assets: List<AssetDto> = emptyList())

@Serializable
data class VideoUrlResponse(val url: String)

@Serializable
data class RescanResponse(val status: String = "")

// Request bodies
@Serializable
data class FavoriteBody(val favorite: Boolean)

@Serializable
data class ArchiveBody(val archived: Boolean)

@Serializable
data class AddTagBody(val tag: String)

@Serializable
data class NameBody(val name: String)

@Serializable
data class AddToAlbumBody(val assetIds: List<Long>)
