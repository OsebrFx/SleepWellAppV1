package com.sleepwell.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.sleepwell.data.local.SleepDatabase
import com.sleepwell.data.model.Goal
import com.sleepwell.data.model.SleepSession
import com.sleepwell.data.model.User
import com.sleepwell.utils.DateUtils
import java.util.Date

class SleepRepository(context: Context) {

    private val database = SleepDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val sleepSessionDao = database.sleepSessionDao()
    private val goalDao = database.goalDao()

    // User operations
    suspend fun registerUser(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun loginUser(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    fun getUserByIdLiveData(userId: Long): LiveData<User?> {
        return userDao.getUserByIdLiveData(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun isEmailExists(email: String): Boolean {
        return userDao.isEmailExists(email) > 0
    }

    suspend fun updateDarkMode(userId: Long, enabled: Boolean) {
        userDao.updateDarkMode(userId, enabled)
    }

    suspend fun updateLanguage(userId: Long, language: String) {
        userDao.updateLanguage(userId, language)
    }

    suspend fun updateSleepReminder(userId: Long, enabled: Boolean, hour: Int, minute: Int) {
        userDao.updateSleepReminder(userId, enabled, hour, minute)
    }

    suspend fun updateWakeupReminder(userId: Long, enabled: Boolean, hour: Int, minute: Int) {
        userDao.updateWakeupReminder(userId, enabled, hour, minute)
    }

    // Sleep Session operations
    suspend fun insertSleepSession(session: SleepSession): Long {
        return sleepSessionDao.insert(session)
    }

    suspend fun updateSleepSession(session: SleepSession) {
        sleepSessionDao.update(session)
    }

    suspend fun deleteSleepSession(session: SleepSession) {
        sleepSessionDao.delete(session)
    }

    fun getAllSessionsByUser(userId: Long): LiveData<List<SleepSession>> {
        return sleepSessionDao.getAllSessionsByUser(userId)
    }

    suspend fun getAllSessionsByUserSync(userId: Long): List<SleepSession> {
        return sleepSessionDao.getAllSessionsByUserSync(userId)
    }

    suspend fun getSessionsBetweenDates(userId: Long, startDate: Date, endDate: Date): List<SleepSession> {
        return sleepSessionDao.getSessionsBetweenDates(userId, startDate, endDate)
    }

    suspend fun getLastNSessions(userId: Long, limit: Int): List<SleepSession> {
        return sleepSessionDao.getLastNSessions(userId, limit)
    }

    suspend fun getLastSession(userId: Long): SleepSession? {
        return sleepSessionDao.getLastSession(userId)
    }

    suspend fun getAverageSleepDuration(userId: Long): Float {
        return sleepSessionDao.getAverageSleepDuration(userId) ?: 0f
    }

    suspend fun getAverageSleepQuality(userId: Long): Float {
        return sleepSessionDao.getAverageSleepQuality(userId) ?: 0f
    }

    suspend fun getBestSleepSession(userId: Long): SleepSession? {
        return sleepSessionDao.getBestSleepSession(userId)
    }

    suspend fun getWorstSleepSession(userId: Long): SleepSession? {
        return sleepSessionDao.getWorstSleepSession(userId)
    }

    suspend fun getTotalSessionsCount(userId: Long): Int {
        return sleepSessionDao.getTotalSessionsCount(userId)
    }

    suspend fun getTotalSleepHours(userId: Long): Float {
        return sleepSessionDao.getTotalSleepHours(userId) ?: 0f
    }

    suspend fun getSessionsThisWeek(userId: Long): List<SleepSession> {
        val weekStart = DateUtils.getStartOfWeek()
        return sleepSessionDao.getSessionsThisWeek(userId, weekStart)
    }

    suspend fun getAverageDeepSleepPercentage(userId: Long): Float {
        return sleepSessionDao.getAverageDeepSleepPercentage(userId) ?: 0f
    }

    suspend fun getAverageLightSleepPercentage(userId: Long): Float {
        return sleepSessionDao.getAverageLightSleepPercentage(userId) ?: 0f
    }

    suspend fun getAverageRemSleepPercentage(userId: Long): Float {
        return sleepSessionDao.getAverageRemSleepPercentage(userId) ?: 0f
    }

    // Goal operations
    suspend fun insertGoal(goal: Goal): Long {
        // Deactivate all existing goals before inserting a new one
        goalDao.deactivateAllGoals(goal.userId)
        return goalDao.insert(goal)
    }

    suspend fun updateGoal(goal: Goal) {
        goalDao.update(goal)
    }

    suspend fun deleteGoal(goal: Goal) {
        goalDao.delete(goal)
    }

    suspend fun getActiveGoal(userId: Long): Goal? {
        return goalDao.getActiveGoal(userId)
    }

    fun getActiveGoalLiveData(userId: Long): LiveData<Goal?> {
        return goalDao.getActiveGoalLiveData(userId)
    }

    suspend fun getAllGoalsByUser(userId: Long): List<Goal> {
        return goalDao.getAllGoalsByUser(userId)
    }

    suspend fun updateStreaks(goalId: Long, streak: Int, bestStreak: Int) {
        goalDao.updateStreaks(goalId, streak, bestStreak)
    }

    suspend fun checkAndUpdateGoalProgress(userId: Long) {
        val activeGoal = getActiveGoal(userId) ?: return
        val lastSession = getLastSession(userId) ?: return

        val goalMet = activeGoal.isGoalMet(lastSession.durationHours, lastSession.quality)

        if (goalMet) {
            val newStreak = activeGoal.streak + 1
            val newBestStreak = maxOf(newStreak, activeGoal.bestStreak)
            updateStreaks(activeGoal.id, newStreak, newBestStreak)
        } else {
            if (activeGoal.streak > 0) {
                updateStreaks(activeGoal.id, 0, activeGoal.bestStreak)
            }
        }
    }

    // Statistics
    suspend fun getSleepStatistics(userId: Long): SleepStatistics {
        val totalSessions = getTotalSessionsCount(userId)
        val totalHours = getTotalSleepHours(userId)
        val averageDuration = getAverageSleepDuration(userId)
        val averageQuality = getAverageSleepQuality(userId)
        val bestSession = getBestSleepSession(userId)
        val worstSession = getWorstSleepSession(userId)
        val deepSleepAvg = getAverageDeepSleepPercentage(userId)
        val lightSleepAvg = getAverageLightSleepPercentage(userId)
        val remSleepAvg = getAverageRemSleepPercentage(userId)

        return SleepStatistics(
            totalSessions = totalSessions,
            totalHours = totalHours,
            averageDuration = averageDuration,
            averageQuality = averageQuality,
            bestSession = bestSession,
            worstSession = worstSession,
            deepSleepPercentage = deepSleepAvg,
            lightSleepPercentage = lightSleepAvg,
            remSleepPercentage = remSleepAvg
        )
    }
}

data class SleepStatistics(
    val totalSessions: Int,
    val totalHours: Float,
    val averageDuration: Float,
    val averageQuality: Float,
    val bestSession: SleepSession?,
    val worstSession: SleepSession?,
    val deepSleepPercentage: Float,
    val lightSleepPercentage: Float,
    val remSleepPercentage: Float
)
