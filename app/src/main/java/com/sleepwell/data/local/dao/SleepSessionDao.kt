package com.sleepwell.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sleepwell.data.model.SleepSession
import java.util.Date

@Dao
interface SleepSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SleepSession): Long

    @Update
    suspend fun update(session: SleepSession)

    @Delete
    suspend fun delete(session: SleepSession)

    @Query("SELECT * FROM sleep_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SleepSession?

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllSessionsByUser(userId: Long): LiveData<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun getAllSessionsByUserSync(userId: Long): List<SleepSession>

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId AND startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    suspend fun getSessionsBetweenDates(userId: Long, startDate: Date, endDate: Date): List<SleepSession>

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId AND startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    fun getSessionsBetweenDatesLiveData(userId: Long, startDate: Date, endDate: Date): LiveData<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getLastNSessions(userId: Long, limit: Int): List<SleepSession>

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastSession(userId: Long): SleepSession?

    @Query("SELECT AVG(durationHours) FROM sleep_sessions WHERE userId = :userId")
    suspend fun getAverageSleepDuration(userId: Long): Float?

    @Query("SELECT AVG(quality) FROM sleep_sessions WHERE userId = :userId")
    suspend fun getAverageSleepQuality(userId: Long): Float?

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId ORDER BY quality DESC LIMIT 1")
    suspend fun getBestSleepSession(userId: Long): SleepSession?

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId ORDER BY quality ASC LIMIT 1")
    suspend fun getWorstSleepSession(userId: Long): SleepSession?

    @Query("SELECT COUNT(*) FROM sleep_sessions WHERE userId = :userId")
    suspend fun getTotalSessionsCount(userId: Long): Int

    @Query("SELECT SUM(durationHours) FROM sleep_sessions WHERE userId = :userId")
    suspend fun getTotalSleepHours(userId: Long): Float?

    @Query("DELETE FROM sleep_sessions WHERE userId = :userId")
    suspend fun deleteAllSessionsByUser(userId: Long)

    @Query("SELECT AVG(deepSleepPercentage) FROM sleep_sessions WHERE userId = :userId")
    suspend fun getAverageDeepSleepPercentage(userId: Long): Float?

    @Query("SELECT AVG(lightSleepPercentage) FROM sleep_sessions WHERE userId = :userId")
    suspend fun getAverageLightSleepPercentage(userId: Long): Float?

    @Query("SELECT AVG(remSleepPercentage) FROM sleep_sessions WHERE userId = :userId")
    suspend fun getAverageRemSleepPercentage(userId: Long): Float?

    @Query("SELECT * FROM sleep_sessions WHERE userId = :userId AND startTime >= :weekStart ORDER BY startTime DESC")
    suspend fun getSessionsThisWeek(userId: Long, weekStart: Date): List<SleepSession>
}
