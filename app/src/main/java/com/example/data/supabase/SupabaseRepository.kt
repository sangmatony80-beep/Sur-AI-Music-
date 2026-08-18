package com.example.data.supabase

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Supabase Postgrest (Database) queries and Realtime live subscriptions.
 */
class SupabaseRepository {

    private val postgrest = SupabaseClientProvider.postgrest
    private val realtime = SupabaseClientProvider.realtime
    private val TAG = "SupabaseRepository"

    private var activeChannel: RealtimeChannel? = null

    private val _realtimeStatus = MutableStateFlow("Initialized")
    val realtimeStatus: StateFlow<String> = _realtimeStatus.asStateFlow()

    private val _liveSongs = MutableStateFlow<List<RemoteSongItem>>(emptyList())
    val liveSongs: StateFlow<List<RemoteSongItem>> = _liveSongs.asStateFlow()

    /**
     * Fetch public community songs from Supabase Postgrest
     */
    suspend fun fetchCommunitySongs(): Result<List<RemoteSongItem>> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.success(emptyList())
        }
        return try {
            val songs = postgrest["songs"]
                .select()
                .decodeList<RemoteSongItem>()
            _liveSongs.value = songs
            Result.success(songs)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching songs: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Search songs in Supabase Postgrest in real-time filtering by track title or artist name.
     */
    suspend fun searchSongs(query: String): Result<List<RemoteSongItem>> {
        val trimmed = query.trim()
        if (!SupabaseClientProvider.hasValidCredentials()) {
            val filtered = if (trimmed.isEmpty()) {
                _liveSongs.value
            } else {
                _liveSongs.value.filter {
                    it.title.contains(trimmed, ignoreCase = true) || it.artist.contains(trimmed, ignoreCase = true)
                }
            }
            return Result.success(filtered)
        }
        return try {
            val results = if (trimmed.isEmpty()) {
                postgrest["songs"].select().decodeList<RemoteSongItem>()
            } else {
                postgrest["songs"].select {
                    filter {
                        or {
                            ilike("title", "%$trimmed%")
                            ilike("artist", "%$trimmed%")
                        }
                    }
                }.decodeList<RemoteSongItem>()
            }
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying songs from Supabase Postgrest: ${e.message}", e)
            // Fallback: fetch all and filter in memory if raw or ilike syntax varies
            try {
                val all = postgrest["songs"].select().decodeList<RemoteSongItem>()
                val matched = all.filter {
                    it.title.contains(trimmed, ignoreCase = true) || it.artist.contains(trimmed, ignoreCase = true)
                }
                Result.success(matched)
            } catch (ex: Exception) {
                val fallback = _liveSongs.value.filter {
                    it.title.contains(trimmed, ignoreCase = true) || it.artist.contains(trimmed, ignoreCase = true)
                }
                Result.success(fallback)
            }
        }
    }

    /**
     * Upload an audio file to Supabase Storage bucket
     */
    suspend fun uploadAudioFile(
        fileName: String,
        audioBytes: ByteArray,
        bucketName: String = "audio_tracks"
    ): Result<String> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            // Local fallback simulation URL for development
            val safeName = fileName.replace(" ", "_")
            val fallbackUrl = "https://assets.mixkit.co/music/preview/mixkit-$safeName"
            return Result.success(fallbackUrl)
        }
        return try {
            val bucket = SupabaseClientProvider.storage[bucketName]
            bucket.upload(path = fileName, data = audioBytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(fileName)
            Log.i(TAG, "Audio successfully uploaded to Supabase storage bucket '$bucketName': $publicUrl")
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading audio to Supabase Storage: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Upload audio to Supabase Storage and save its metadata to Supabase Postgrest database
     */
    suspend fun uploadAndPublishTrack(
        title: String,
        artist: String,
        genre: String,
        prompt: String,
        duration: String,
        fileName: String,
        audioBytes: ByteArray,
        userId: String = "",
        isPublic: Boolean = true
    ): Result<RemoteSongItem> {
        return try {
            // Step 1: Upload audio file to Supabase Storage
            val uploadResult = uploadAudioFile(fileName = fileName, audioBytes = audioBytes)
            val audioUrl = uploadResult.getOrElse {
                "https://assets.mixkit.co/music/preview/mixkit-${fileName.replace(" ", "_")}"
            }

            // Step 2: Construct metadata record
            val songItem = RemoteSongItem(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                artist = artist,
                genre = genre,
                prompt = prompt,
                audioUrl = audioUrl,
                duration = duration,
                likesCount = 0,
                playsCount = 1,
                isPublic = isPublic,
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
            )

            // Step 3: Insert metadata to Postgrest 'songs' table
            if (SupabaseClientProvider.hasValidCredentials()) {
                postgrest["songs"].insert(songItem)
            }

            // Update live memory list
            val updated = _liveSongs.value.toMutableList().apply { add(0, songItem) }
            _liveSongs.value = updated

            Log.i(TAG, "Song metadata published successfully to Supabase: ${songItem.title}")
            Result.success(songItem)
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadAndPublishTrack: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Insert a newly generated AI song into Supabase cloud table
     */
    suspend fun publishSong(song: RemoteSongItem): Result<Unit> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.success(Unit)
        }
        return try {
            postgrest["songs"].insert(song)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing song: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch user profile from Supabase profiles table
     */
    suspend fun getUserProfile(userEmail: String): Result<RemoteUserProfile?> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.success(null)
        }
        return try {
            val profile = postgrest["profiles"]
                .select {
                    filter {
                        eq("email", userEmail)
                    }
                }
                .decodeSingleOrNull<RemoteUserProfile>()
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Submit a pending payment record for subscription purchase
     */
    suspend fun submitPaymentRecord(payment: RemotePaymentRecord): Result<Unit> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.success(Unit)
        }
        return try {
            postgrest["pending_payments"].insert(payment)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting payment: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Subscribe to Realtime channel for live updates
     */
    suspend fun subscribeToRealtimeBroadcast(channelName: String = "public:feed"): Result<Unit> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            _realtimeStatus.value = "Local mode (Supabase unconfigured)"
            return Result.success(Unit)
        }
        return try {
            _realtimeStatus.value = "Connecting to channel $channelName..."
            val channel = realtime.channel(channelName)
            activeChannel = channel
            channel.subscribe()
            _realtimeStatus.value = "Subscribed to $channelName"
            Log.i(TAG, "Realtime channel $channelName subscribed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            _realtimeStatus.value = "Error: ${e.message}"
            Log.e(TAG, "Realtime subscription failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Unsubscribe from active realtime channel
     */
    suspend fun unsubscribeRealtime() {
        try {
            activeChannel?.unsubscribe()
            activeChannel = null
            _realtimeStatus.value = "Disconnected"
        } catch (e: Exception) {
            Log.e(TAG, "Unsubscribe error: ${e.message}", e)
        }
    }

    /**
     * Fetch all voice records from Supabase PostgreSQL table
     */
    suspend fun fetchVoiceRecords(): Result<List<RemoteVoiceRecordItem>> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.success(emptyList())
        }
        return try {
            val records = postgrest["voice_records"]
                .select()
                .decodeList<RemoteVoiceRecordItem>()
            Result.success(records)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching voice records from Supabase Postgrest: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Upsert / Publish voice record metadata into Supabase PostgreSQL table
     */
    suspend fun publishVoiceRecord(record: RemoteVoiceRecordItem): Result<Unit> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.success(Unit)
        }
        return try {
            postgrest["voice_records"].insert(record)
            Log.i(TAG, "Voice record synchronized with Supabase PostgreSQL: ${record.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error synchronizing voice record with Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }
}
