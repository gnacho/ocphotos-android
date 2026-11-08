package app.ocphotos.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Upsert
    suspend fun upsertAll(assets: List<AssetEntity>)

    @Query("SELECT * FROM assets WHERE archived = 0 ORDER BY takenAt DESC, id DESC")
    fun timeline(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE favorite = 1 AND archived = 0 ORDER BY takenAt DESC, id DESC")
    fun favorites(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE archived = 1 ORDER BY takenAt DESC, id DESC")
    fun archived(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE lat IS NOT NULL ORDER BY takenAt DESC, id DESC")
    suspend fun withGeo(): List<AssetEntity>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun byId(id: Long): AssetEntity?

    @Query(
        "SELECT * FROM assets WHERE archived = 0 AND " +
            "(filename LIKE '%' || :q || '%' COLLATE NOCASE OR camera LIKE '%' || :q || '%' COLLATE NOCASE) " +
            "ORDER BY takenAt DESC, id DESC"
    )
    fun search(q: String): Flow<List<AssetEntity>>

    @Query("SELECT COUNT(*) FROM assets WHERE archived = 0")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assets WHERE favorite = 1 AND archived = 0")
    fun favoriteCount(): Flow<Int>

    @Query("UPDATE assets SET favorite = :value WHERE id = :id")
    suspend fun setFavorite(id: Long, value: Boolean)

    @Query("UPDATE assets SET archived = :value WHERE id = :id")
    suspend fun setArchived(id: Long, value: Boolean)

    @Query("DELETE FROM assets WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<Long>)

    @Query("DELETE FROM assets")
    suspend fun clear()
}
