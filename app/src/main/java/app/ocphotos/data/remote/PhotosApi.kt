package app.ocphotos.data.remote

import app.ocphotos.data.remote.dto.AddTagBody
import app.ocphotos.data.remote.dto.AddToAlbumBody
import app.ocphotos.data.remote.dto.AlbumsResponse
import app.ocphotos.data.remote.dto.ArchiveBody
import app.ocphotos.data.remote.dto.AssetTagsResponse
import app.ocphotos.data.remote.dto.AssetsResponse
import app.ocphotos.data.remote.dto.CalendarResponse
import app.ocphotos.data.remote.dto.CreateAlbumResponse
import app.ocphotos.data.remote.dto.DuplicatesResponse
import app.ocphotos.data.remote.dto.FavoriteBody
import app.ocphotos.data.remote.dto.FoldersResponse
import app.ocphotos.data.remote.dto.GeoResponse
import app.ocphotos.data.remote.dto.MemoriesResponse
import app.ocphotos.data.remote.dto.NameBody
import app.ocphotos.data.remote.dto.OnThisDayResponse
import app.ocphotos.data.remote.dto.PlacesResponse
import app.ocphotos.data.remote.dto.RescanResponse
import app.ocphotos.data.remote.dto.ResolveResponse
import app.ocphotos.data.remote.dto.StatsResponse
import app.ocphotos.data.remote.dto.TagsResponse
import app.ocphotos.data.remote.dto.VideoUrlResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// REST client for the ocphotos API served by ocapps under /ocphotos-api/api/.
// Endpoints mirror ocapps internal/photos/api/api.go.
interface PhotosApi {
    @GET("stats")
    suspend fun stats(): StatsResponse

    @GET("assets")
    suspend fun assets(
        @Query("before_taken") beforeTaken: Long? = null,
        @Query("before_id") beforeId: Long? = null,
        @Query("limit") limit: Int? = null,
        @Query("favorites") favorites: Int? = null,
        @Query("archived") archived: Int? = null,
        @Query("q") q: String? = null,
    ): AssetsResponse

    @GET("assets/resolve")
    suspend fun resolve(@Query("driveAliasAndItem") driveAliasAndItem: String): ResolveResponse

    @POST("assets/{id}/favorite")
    suspend fun favorite(@Path("id") id: Long, @Body body: FavoriteBody)

    @POST("assets/{id}/archive")
    suspend fun archive(@Path("id") id: Long, @Body body: ArchiveBody)

    @GET("duplicates")
    suspend fun duplicates(): DuplicatesResponse

    @POST("assets/{id}/video-url")
    suspend fun videoUrl(@Path("id") id: Long): VideoUrlResponse

    @GET("memories/on-this-day")
    suspend fun onThisDay(@Query("days") days: Int? = null): OnThisDayResponse

    @GET("memories/highlights")
    suspend fun highlights(): MemoriesResponse

    @GET("timeline/calendar")
    suspend fun calendar(): CalendarResponse

    @GET("geo")
    suspend fun geo(): GeoResponse

    @GET("places")
    suspend fun places(): PlacesResponse

    @GET("tags")
    suspend fun tags(): TagsResponse

    @GET("tags/{tag}/assets")
    suspend fun tagAssets(@Path("tag") tag: String): AssetsResponse

    @GET("assets/{id}/tags")
    suspend fun assetTags(@Path("id") id: Long): AssetTagsResponse

    @POST("assets/{id}/tags")
    suspend fun addTag(@Path("id") id: Long, @Body body: AddTagBody)

    @DELETE("assets/{id}/tags/{tag}")
    suspend fun removeTag(@Path("id") id: Long, @Path("tag") tag: String)

    @GET("folders")
    suspend fun folders(@Query("path") path: String? = null): FoldersResponse

    @GET("albums")
    suspend fun albums(): AlbumsResponse

    @POST("albums")
    suspend fun createAlbum(@Body body: NameBody): CreateAlbumResponse

    @PATCH("albums/{id}")
    suspend fun renameAlbum(@Path("id") id: Long, @Body body: NameBody)

    @DELETE("albums/{id}")
    suspend fun deleteAlbum(@Path("id") id: Long)

    @GET("albums/{id}/assets")
    suspend fun albumAssets(@Path("id") id: Long): AssetsResponse

    @POST("albums/{id}/assets")
    suspend fun addToAlbum(@Path("id") id: Long, @Body body: AddToAlbumBody)

    @DELETE("albums/{id}/assets/{assetId}")
    suspend fun removeFromAlbum(@Path("id") id: Long, @Path("assetId") assetId: Long)

    @POST("admin/rescan")
    suspend fun rescan(): RescanResponse
}
