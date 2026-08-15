package com.example.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteUserProfile(
    @SerialName("id") val id: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("role") val role: String = "USER",
    @SerialName("plan") val plan: String = "FREE",
    @SerialName("token_balance") val tokenBalance: Int = 50,
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class RemoteSongItem(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("artist") val artist: String = "Sur AI",
    @SerialName("genre") val genre: String = "Bangla Pop",
    @SerialName("prompt") val prompt: String = "",
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("duration") val duration: String = "03:15",
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("plays_count") val playsCount: Int = 0,
    @SerialName("is_public") val isPublic: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class RemotePaymentRecord(
    @SerialName("id") val id: String = "",
    @SerialName("user_email") val userEmail: String = "",
    @SerialName("plan_name") val planName: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("method") val method: String = "BKASH",
    @SerialName("trx_id") val trxId: String = "",
    @SerialName("status") val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class RemoteLiveAnnouncement(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("message") val message: String = "",
    @SerialName("severity") val severity: String = "INFO",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)
