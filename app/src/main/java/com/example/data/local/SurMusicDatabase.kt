package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        SongEntity::class,
        PlanEntity::class,
        UserSubscriptionEntity::class,
        TokenTransactionEntity::class,
        DailyUsageEntity::class,
        LyricsHistoryEntity::class,
        ClonedVoiceEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class SurMusicDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun musicDao(): MusicDao
    abstract fun planDao(): PlanDao
    abstract fun lyricsDao(): LyricsDao

    companion object {
        @Volatile
        private var INSTANCE: SurMusicDatabase? = null

        fun getDatabase(context: Context): SurMusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SurMusicDatabase::class.java,
                    "sur_music_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
