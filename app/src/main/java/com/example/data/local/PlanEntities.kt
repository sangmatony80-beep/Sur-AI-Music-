package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey
    val id: String, // "free", "starter", "pro", "ultra", "studio"
    val name: String,
    val priceMonthly: Int, // in ৳
    val priceYearly: Int, // in ৳
    val lyricsLimitPerDay: Int, // 5 for free, else 9999
    val lyricsLimitPerMonth: Int, // 0 (or daily), 30, 100, 300, -1 for unlimited
    val tokensPerMonth: Int, // 150, 500, 2000, 6000, 20000
    val hasWatermark: Boolean, // true for Free, Starter, Pro, Ultra; false for Studio
    val commercialLicense: Boolean // false for Free, Starter, Pro, Ultra; true for Studio
)

@Entity(tableName = "user_subscriptions")
data class UserSubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val planId: String,
    val billingCycle: String, // "monthly" or "yearly"
    val startDate: Long,
    val renewalDate: Long,
    val isActive: Boolean
)

@Entity(tableName = "token_transactions")
data class TokenTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val amount: Int, // positive for credit, negative for debit
    val type: String, // "subscription_monthly", "token_pack_purchase", "gift", "song_generation", "admin"
    val description: String,
    val timestamp: Long
)

@Entity(tableName = "daily_usage")
data class DailyUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val dateStr: String, // "yyyy-MM-dd"
    val lyricsCreatedCount: Int
)
