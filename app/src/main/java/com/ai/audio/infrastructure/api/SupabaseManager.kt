package com.ai.audio.infrastructure.api

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.auth.Auth
import kotlinx.serialization.Serializable

// ডেটাবেস স্কিমা মডেল
@Serializable
data class SyncedLicense(
    val projectId: String,
    val assetId: String,
    val licenseKey: String,
    val timestamp: Long
)

object SupabaseManager {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://your-project.supabase.co", // Replace with com.example.BuildConfig.SUPABASE_URL
            supabaseKey = "your-anon-key" // Replace with com.example.BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }
}
