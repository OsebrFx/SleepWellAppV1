package com.sleepwell.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sleepwell.data.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdLiveData(userId: Long): LiveData<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Int

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("UPDATE users SET darkModeEnabled = :enabled WHERE id = :userId")
    suspend fun updateDarkMode(userId: Long, enabled: Boolean)

    @Query("UPDATE users SET language = :language WHERE id = :userId")
    suspend fun updateLanguage(userId: Long, language: String)

    @Query("UPDATE users SET sleepReminderEnabled = :enabled, sleepReminderHour = :hour, sleepReminderMinute = :minute WHERE id = :userId")
    suspend fun updateSleepReminder(userId: Long, enabled: Boolean, hour: Int, minute: Int)

    @Query("UPDATE users SET wakeupReminderEnabled = :enabled, wakeupReminderHour = :hour, wakeupReminderMinute = :minute WHERE id = :userId")
    suspend fun updateWakeupReminder(userId: Long, enabled: Boolean, hour: Int, minute: Int)
}
