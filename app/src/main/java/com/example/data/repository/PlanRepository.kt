package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class PlanRepository(private val planDao: PlanDao) {

    val allPlans: Flow<List<PlanEntity>> = planDao.getAllPlans()

    suspend fun seedDefaultPlans() {
        val defaultPlans = listOf(
            PlanEntity(
                id = "free",
                name = "Free Tier",
                priceMonthly = 0,
                priceYearly = 0,
                lyricsLimitPerDay = 3,
                lyricsLimitPerMonth = 0,
                tokensPerMonth = 100,
                hasWatermark = true,
                commercialLicense = false
            ),
            PlanEntity(
                id = "starter",
                name = "Starter Pass",
                priceMonthly = 199,
                priceYearly = 1990,
                lyricsLimitPerDay = 9999,
                lyricsLimitPerMonth = 30,
                tokensPerMonth = 500,
                hasWatermark = true,
                commercialLicense = false
            ),
            PlanEntity(
                id = "pro",
                name = "Pro Creator",
                priceMonthly = 499,
                priceYearly = 4990,
                lyricsLimitPerDay = 9999,
                lyricsLimitPerMonth = 100,
                tokensPerMonth = 2000,
                hasWatermark = false,
                commercialLicense = true
            ),
            PlanEntity(
                id = "ultra",
                name = "Ultra Studio",
                priceMonthly = 1199,
                priceYearly = 11990,
                lyricsLimitPerDay = 9999,
                lyricsLimitPerMonth = 350,
                tokensPerMonth = 6000,
                hasWatermark = false,
                commercialLicense = true
            ),
            PlanEntity(
                id = "studio",
                name = "Unicorn Enterprise",
                priceMonthly = 3999,
                priceYearly = 39990,
                lyricsLimitPerDay = 9999,
                lyricsLimitPerMonth = -1,
                tokensPerMonth = 25000,
                hasWatermark = false,
                commercialLicense = true
            )
        )
        planDao.insertPlans(defaultPlans)
    }

    fun getActiveSubscription(email: String): Flow<UserSubscriptionEntity?> {
        return planDao.getActiveSubscription(email)
    }

    suspend fun getActiveSubscriptionSync(email: String): UserSubscriptionEntity? {
        return planDao.getActiveSubscriptionSync(email)
    }

    suspend fun subscribeUser(email: String, planId: String, billingCycle: String) {
        planDao.deactivateSubscriptions(email)
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        if (billingCycle == "yearly") {
            calendar.add(Calendar.YEAR, 1)
        } else {
            calendar.add(Calendar.MONTH, 1)
        }
        val renewal = calendar.timeInMillis

        val sub = UserSubscriptionEntity(
            userEmail = email,
            planId = planId,
            billingCycle = billingCycle,
            startDate = now,
            renewalDate = renewal,
            isActive = true
        )
        planDao.insertSubscription(sub)

        // Add monthly tokens based on plan
        val plan = planDao.getPlanById(planId)
        if (plan != null && plan.tokensPerMonth > 0) {
            addTokenTransaction(
                email = email,
                amount = plan.tokensPerMonth,
                type = "subscription_monthly",
                description = "Monthly tokens for ${plan.name} Plan"
            )
        }
    }

    fun getTokenBalance(email: String): Flow<Int?> {
        return planDao.getTokenBalance(email)
    }

    suspend fun getTokenBalanceSync(email: String): Int {
        return planDao.getTokenBalanceSync(email) ?: 0
    }

    fun getTokenTransactions(email: String): Flow<List<TokenTransactionEntity>> {
        return planDao.getTokenTransactions(email)
    }

    suspend fun addTokenTransaction(email: String, amount: Int, type: String, description: String) {
        val tx = TokenTransactionEntity(
            userEmail = email,
            amount = amount,
            type = type,
            description = description,
            timestamp = System.currentTimeMillis()
        )
        planDao.insertTokenTransaction(tx)
    }

    suspend fun canCreateLyrics(email: String): Boolean {
        val sub = getActiveSubscriptionSync(email)
        val planId = sub?.planId ?: "free"
        val plan = planDao.getPlanById(planId) ?: return true

        if (plan.lyricsLimitPerDay >= 9999) return true

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val usage = planDao.getDailyUsage(email, todayStr)
        val count = usage?.lyricsCreatedCount ?: 0

        return count < plan.lyricsLimitPerDay
    }

    suspend fun recordLyricCreation(email: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val usage = planDao.getDailyUsage(email, todayStr)
        if (usage == null) {
            planDao.insertDailyUsage(DailyUsageEntity(userEmail = email, dateStr = todayStr, lyricsCreatedCount = 1))
        } else {
            planDao.insertDailyUsage(usage.copy(lyricsCreatedCount = usage.lyricsCreatedCount + 1))
        }
    }

    suspend fun hasWatermark(email: String): Boolean {
        val sub = getActiveSubscriptionSync(email)
        val planId = sub?.planId ?: "free"
        val plan = planDao.getPlanById(planId)
        return plan?.hasWatermark ?: true
    }

    suspend fun getPlanDetails(planId: String): PlanEntity? {
        return planDao.getPlanById(planId)
    }

    suspend fun updatePlanPrice(planId: String, monthly: Int, yearly: Int) {
        val plan = planDao.getPlanById(planId)
        if (plan != null) {
            planDao.insertPlan(plan.copy(priceMonthly = monthly, priceYearly = yearly))
        }
    }
}
