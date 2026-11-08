package app.ocphotos.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.ocphotos.PhotosApplication
import kotlinx.coroutines.CancellationException

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as PhotosApplication
        // No session bound yet: nothing to sync, avoid a retry loop.
        if (!app.container.repository.isBound) return Result.success()
        return try {
            app.container.repository.sync()
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
