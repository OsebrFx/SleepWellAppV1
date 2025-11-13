package com.sleepwell.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sleepwell.data.local.dao.GoalDao
import com.sleepwell.data.local.dao.SleepSessionDao
import com.sleepwell.data.local.dao.UserDao
import com.sleepwell.data.model.Goal
import com.sleepwell.data.model.SleepSession
import com.sleepwell.data.model.User
import com.sleepwell.utils.Constants

@Database(
    entities = [User::class, SleepSession::class, Goal::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
