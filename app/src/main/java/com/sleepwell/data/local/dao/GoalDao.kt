package com.sleepwell.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sleepwell.data.model.Goal

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): Goal?

    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveGoal(userId: Long): Goal?

    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getActiveGoalLiveData(userId: Long): LiveData<Goal?>

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllGoalsByUser(userId: Long): List<Goal>

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoalsByUserLiveData(userId: Long): LiveData<List<Goal>>

    @Query("UPDATE goals SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllGoals(userId: Long)

    @Query("UPDATE goals SET streak = :streak WHERE id = :goalId")
    suspend fun updateStreak(goalId: Long, streak: Int)

    @Query("UPDATE goals SET bestStreak = :bestStreak WHERE id = :goalId")
    suspend fun updateBestStreak(goalId: Long, bestStreak: Int)

    @Query("UPDATE goals SET streak = :streak, bestStreak = :bestStreak WHERE id = :goalId")
    suspend fun updateStreaks(goalId: Long, streak: Int, bestStreak: Int)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllGoalsByUser(userId: Long)

    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId")
    suspend fun getGoalsCount(userId: Long): Int
}
