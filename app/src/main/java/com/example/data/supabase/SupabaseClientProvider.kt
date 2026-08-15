package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core Supabase Client Configuration & Singleton Provider
 * Provides unified access to Supabase Auth, Postgrest (Database), Realtime, and Storage.
 */
object SupabaseClientProvider {

    private const val TAG = "SupabaseClientProvider"

    // Default configuration fallbacks if not yet injected via BuildConfig
    private const val DEFAULT_SUPABASE_URL = "https://wqvqjzkbmbcvyyqjvfop.supabase.co"
    private const val DEFAULT_SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.placeholder"

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    private var _supabaseUrl: String = DEFAULT_SUPABASE_URL
    private var _supabaseAnonKey: String = DEFAULT_SUPABASE_ANON_KEY

    val supabaseUrl: String get() = _supabaseUrl
    val supabaseAnonKey: String get() = _supabaseAnonKey

    /**
     * Check if valid custom credentials are active (not placeholder/dummy domain)
     */
    fun hasValidCredentials(): Boolean {
        return _supabaseUrl.isNotBlank() && 
               !_supabaseUrl.contains("placeholder") &&
               !_supabaseUrl.contains("wqvqjzkbmbcvyyqjvfop") &&
               _supabaseAnonKey.isNotBlank() &&
               !_supabaseAnonKey.contains("placeholder")
    }

    val client: SupabaseClient by lazy {
        initClient(_supabaseUrl, _supabaseAnonKey)
    }

    private fun initClient(url: String, key: String): SupabaseClient {
        Log.i(TAG, "Initializing Supabase client (configured=${hasValidCredentials()})")
        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Auth) {
                // Only enable auto token refresh when valid active project credentials are provided
                alwaysAutoRefresh = hasValidCredentials()
                autoLoadFromStorage = hasValidCredentials()
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }.also {
            _isConfigured.value = hasValidCredentials()
        }
    }

    /**
     * Helper accessors for Supabase modules
     */
    val auth: Auth get() = client.auth
    val postgrest: Postgrest get() = client.postgrest
    val realtime: Realtime get() = client.realtime
    val storage: Storage get() = client.storage
}
