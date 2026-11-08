package app.ocphotos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AssetEntity::class, AlbumEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PhotosDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun albumDao(): AlbumDao
}
