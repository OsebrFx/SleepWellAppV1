package com.sleepwell.fitness

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a workout session.
 */
data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val type: WorkoutType,
    val durationMinutes: Int,
    val caloriesBurned: Int = 0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return if (hours > 0) {
            "${hours}h ${minutes}min"
        } else {
            "${minutes}min"
        }
    }
}

/**
 * Enum representing workout types.
 */
enum class WorkoutType(val displayName: String, val caloriesPerMinute: Double) {
    RUNNING("Course", 10.0),
    WALKING("Marche", 4.0),
    CYCLING("Vélo", 8.0),
    SWIMMING("Natation", 11.0),
    YOGA("Yoga", 3.0),
    GYM("Musculation", 6.0),
    SPORTS("Sports", 7.0),
    OTHER("Autre", 5.0);

    companion object {
        fun fromString(value: String): WorkoutType {
            return values().find { it.name == value } ?: OTHER
        }
    }
}

/**
 * Repository for managing workout data using SharedPreferences.
 */
class WorkoutRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    /**
     * Saves a workout to storage.
     */
    fun saveWorkout(workout: Workout) {
        val workouts = getAllWorkouts().toMutableList()
        workouts.add(workout)
        saveWorkouts(workouts)
    }

    /**
     * Gets all workouts, sorted by timestamp (most recent first).
     */
    fun getAllWorkouts(): List<Workout> {
        val json = prefs.getString(KEY_WORKOUTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Workout>>() {}.type
        val workouts: List<Workout> = gson.fromJson(json, type) ?: emptyList()
        return workouts.sortedByDescending { it.timestamp }
    }

    /**
     * Deletes a workout by ID.
     */
    fun deleteWorkout(workoutId: String) {
        val workouts = getAllWorkouts().filterNot { it.id == workoutId }
        saveWorkouts(workouts)
    }

    /**
     * Gets workout statistics for a time period.
     */
    fun getStats(daysBack: Int = 7): WorkoutStats {
        val cutoffTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        val recentWorkouts = getAllWorkouts().filter { it.timestamp >= cutoffTime }

        val totalWorkouts = recentWorkouts.size
        val totalMinutes = recentWorkouts.sumOf { it.durationMinutes }
        val totalCalories = recentWorkouts.sumOf { it.caloriesBurned }

        return WorkoutStats(
            totalWorkouts = totalWorkouts,
            totalMinutes = totalMinutes,
            totalCalories = totalCalories,
            daysBack = daysBack
        )
    }

    /**
     * Clears all workout data.
     */
    fun clearAll() {
        prefs.edit().remove(KEY_WORKOUTS).apply()
    }

    private fun saveWorkouts(workouts: List<Workout>) {
        val json = gson.toJson(workouts)
        prefs.edit().putString(KEY_WORKOUTS, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "workout_prefs"
        private const val KEY_WORKOUTS = "workouts"
    }
}

/**
 * Statistics for workouts over a period.
 */
data class WorkoutStats(
    val totalWorkouts: Int,
    val totalMinutes: Int,
    val totalCalories: Int,
    val daysBack: Int
) {
    fun getFormattedDuration(): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) {
            "${hours}h ${minutes}min"
        } else {
            "${minutes}min"
        }
    }
}
