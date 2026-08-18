package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.SurMusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioCacheWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("AudioCacheWorker", "Starting periodic audio cache cleanup...")

            // 1. Delete temporary cached files in the app's cache directory (if any)
            val cacheDir = applicationContext.cacheDir
            val audioCacheDir = cacheDir.resolve("audio_cache")
            if (audioCacheDir.exists() && audioCacheDir.isDirectory) {
                audioCacheDir.listFiles()?.forEach { file ->
                    // Optionally, check if it's explicitly saved elsewhere, but since it's temp audio cache we just delete
                    if (file.isFile) {
                        file.delete()
                    }
                }
                Log.d("AudioCacheWorker", "Deleted physical audio cache files.")
            }

            // 2. Delete unsaved tracks (not favorite, not generated) from the Room database cache
            val database = SurMusicDatabase.getDatabase(applicationContext)
            database.musicDao().deleteUnsavedSongs()
            
            Log.d("AudioCacheWorker", "Successfully removed unsaved temporary tracks from local database.")

            Result.success()
        } catch (e: Exception) {
            Log.e("AudioCacheWorker", "Failed to clear audio cache: \${e.message}", e)
            Result.retry()
        }
    }
}
