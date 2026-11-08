package app.ocphotos

import android.app.Application
import app.ocphotos.di.AppContainer
import app.ocphotos.sync.SyncScheduler
import coil3.SingletonImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

class PhotosApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // osmdroid requires a user agent (OSM tile policy) and a cache dir under the
        // app storage (avoids WRITE_EXTERNAL_STORAGE on scoped storage).
        Configuration.getInstance().apply {
            userAgentValue = BuildConfig.APPLICATION_ID
            osmdroidBasePath = java.io.File(cacheDir, "osmdroid")
            osmdroidTileCache = java.io.File(cacheDir, "osmdroid/tiles")
        }

        // Coil singleton: AsyncImage picks up the authenticated OkHttp client.
        SingletonImageLoader.setSafe { container.imageLoader }

        // Rebind api + image credentials whenever the persisted session changes.
        appScope.launch {
            container.repository.session.collect { container.repository.bind(it) }
        }

        SyncScheduler.schedule(this, 15)
    }
}
