package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getPlanById(planId: String): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<PlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity)

    @Query("SELECT * FROM user_subscriptions WHERE userEmail = :email AND isActive = 1 LIMIT 1")
    fun getActiveSubscription(email: String): Flow<UserSubscriptionEntity?>

    @Query("SELECT * FROM user_subscriptions WHERE userEmail = :email AND isActive = 1 LIMIT 1")
    suspend fun getActiveSubscriptionSync(email: String): UserSubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: UserSubscriptionEntity)

    @Query("UPDATE user_subscriptions SET isActive = 0 WHERE userEmail = :email")
    suspend fun deactivateSubscriptions(email: String)

    @Query("SELECT * FROM token_transactions WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getTokenTransactions(email: String): Flow<List<TokenTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokenTransaction(transaction: TokenTransactionEntity)

    @Query("SELECT SUM(amount) FROM token_transactions WHERE userEmail = :email")
    fun getTokenBalance(email: String): Flow<Int?>

    @Query("SELECT SUM(amount) FROM token_transactions WHERE userEmail = :email")
    suspend fun getTokenBalanceSync(email: String): Int?

    @Query("SELECT * FROM daily_usage WHERE userEmail = :email AND dateStr = :dateStr LIMIT 1")
    suspend fun getDailyUsage(email: String, dateStr: String): DailyUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyUsage(usage: DailyUsageEntity)
}
