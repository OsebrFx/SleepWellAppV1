package com.sleepwell.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sleepwell.data.model.SleepSession
import com.sleepwell.data.repository.AIRepository
import com.sleepwell.data.repository.SleepRepository
import com.sleepwell.data.repository.SleepStatistics
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sleepRepository = SleepRepository(application)
    private val aiRepository = AIRepository()

    private val _weeklyStats = MutableLiveData<List<SleepSession>>()
    val weeklyStats: LiveData<List<SleepSession>> = _weeklyStats

    private val _statistics = MutableLiveData<SleepStatistics>()
    val statistics: LiveData<SleepStatistics> = _statistics

    private val _aiInsight = MutableLiveData<String?>()
    val aiInsight: LiveData<String?> = _aiInsight

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDashboardData(userId: Long) {
        _loading.value = true
        viewModelScope.launch {
            try {
                // Load weekly stats
                val weeklySessions = sleepRepository.getSessionsThisWeek(userId)
                _weeklyStats.value = weeklySessions

                // Load general statistics
                val stats = sleepRepository.getSleepStatistics(userId)
                _statistics.value = stats

                // Generate AI insight if we have data
                if (stats.totalSessions > 0) {
                    generateAIInsight(stats)
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement des données"
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun generateAIInsight(stats: SleepStatistics) {
        val result = aiRepository.getPersonalizedSleepAdvice(
            averageSleepDuration = stats.averageDuration,
            averageQuality = stats.averageQuality
        )

        result.onSuccess { advice ->
            _aiInsight.value = advice
        }.onFailure {
            _aiInsight.value = null
        }
    }

    fun addSleepSession(userId: Long, session: SleepSession) {
        viewModelScope.launch {
            try {
                sleepRepository.insertSleepSession(session)
                sleepRepository.checkAndUpdateGoalProgress(userId)
                loadDashboardData(userId)
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'ajout de la session: ${e.message}"
            }
        }
    }

    fun deleteSleepSession(userId: Long, session: SleepSession) {
        viewModelScope.launch {
            try {
                sleepRepository.deleteSleepSession(session)
                loadDashboardData(userId)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }
}
