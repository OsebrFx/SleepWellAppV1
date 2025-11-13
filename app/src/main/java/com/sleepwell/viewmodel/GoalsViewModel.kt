package com.sleepwell.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sleepwell.data.model.Goal
import com.sleepwell.data.repository.SleepRepository
import com.sleepwell.utils.Constants
import kotlinx.coroutines.launch
import java.util.Date

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SleepRepository(application)

    private val _activeGoal = MutableLiveData<Goal?>()
    val activeGoal: LiveData<Goal?> = _activeGoal

    private val _goalHistory = MutableLiveData<List<Goal>>()
    val goalHistory: LiveData<List<Goal>> = _goalHistory

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _goalCreated = MutableLiveData<Boolean>()
    val goalCreated: LiveData<Boolean> = _goalCreated

    fun loadGoals(userId: Long) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val active = repository.getActiveGoal(userId)
                _activeGoal.value = active

                val history = repository.getAllGoalsByUser(userId)
                _goalHistory.value = history

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des objectifs: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createGoal(userId: Long, targetHours: Float, targetQuality: Int) {
        if (!validateGoalInput(targetHours, targetQuality)) {
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val goal = Goal(
                    userId = userId,
                    targetHours = targetHours,
                    targetQuality = targetQuality,
                    streak = 0,
                    bestStreak = 0,
                    isActive = true,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                repository.insertGoal(goal)
                _goalCreated.value = true
                loadGoals(userId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors de la création de l'objectif: ${e.message}"
                _goalCreated.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateGoal(goal: Goal) {
        _loading.value = true
        viewModelScope.launch {
            try {
                repository.updateGoal(goal)
                loadGoals(goal.userId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        _loading.value = true
        viewModelScope.launch {
            try {
                repository.deleteGoal(goal)
                loadGoals(goal.userId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun validateGoalInput(targetHours: Float, targetQuality: Int): Boolean {
        return when {
            targetHours < Constants.MIN_GOAL_HOURS -> {
                _error.value = "La durée minimale est de ${Constants.MIN_GOAL_HOURS}h"
                false
            }
            targetHours > Constants.MAX_GOAL_HOURS -> {
                _error.value = "La durée maximale est de ${Constants.MAX_GOAL_HOURS}h"
                false
            }
            targetQuality < Constants.MIN_GOAL_QUALITY -> {
                _error.value = "La qualité minimale est de ${Constants.MIN_GOAL_QUALITY}%"
                false
            }
            targetQuality > Constants.MAX_SLEEP_QUALITY -> {
                _error.value = "La qualité maximale est de ${Constants.MAX_SLEEP_QUALITY}%"
                false
            }
            else -> true
        }
    }

    fun resetError() {
        _error.value = null
    }

    fun resetGoalCreated() {
        _goalCreated.value = false
    }
}
