package com.sleepwell.fitness

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks physical activity metrics using device sensors.
 *
 * Features:
 * - Step counting using TYPE_STEP_COUNTER sensor
 * - Distance calculation based on steps and stride length
 * - Calorie estimation based on activity level
 * - Real-time updates via StateFlow
 */
class ActivityTracker(context: Context) : SensorEventListener {

    data class ActivityStats(
        val steps: Int = 0,
        val distanceMeters: Double = 0.0,
        val caloriesBurned: Int = 0,
        val isTracking: Boolean = false
    )

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var initialStepCount: Int? = null
    private var currentStepCount = 0

    // Average stride length in meters (can be customized based on user height)
    private var strideLengthMeters = 0.762 // ~30 inches, average adult stride

    // Calories burned per step (rough estimate: 0.04-0.06 calories/step)
    private val caloriesPerStep = 0.04

    private val _activityStats = MutableStateFlow(ActivityStats())
    val activityStats: StateFlow<ActivityStats> = _activityStats.asStateFlow()

    val isSensorAvailable: Boolean
        get() = stepCounterSensor != null

    /**
     * Sets custom stride length for more accurate distance calculation.
     * @param lengthMeters Stride length in meters
     */
    fun setStrideLength(lengthMeters: Double) {
        strideLengthMeters = lengthMeters
        updateStats()
    }

    /**
     * Calculates stride length based on user height.
     * Formula: stride length ≈ height * 0.415
     * @param heightCm User height in centimeters
     */
    fun setStrideLengthFromHeight(heightCm: Double) {
        strideLengthMeters = (heightCm / 100.0) * 0.415
        updateStats()
    }

    /**
     * Starts tracking activity using the step counter sensor.
     * Returns true if tracking started successfully, false if sensor unavailable.
     */
    fun startTracking(): Boolean {
        if (stepCounterSensor == null) {
            Log.w(TAG, "Step counter sensor not available")
            return false
        }

        val registered = sensorManager.registerListener(
            this,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        if (registered) {
            _activityStats.value = _activityStats.value.copy(isTracking = true)
            Log.d(TAG, "Activity tracking started")
        } else {
            Log.e(TAG, "Failed to register step counter listener")
        }

        return registered
    }

    /**
     * Stops tracking activity and unregisters sensor listener.
     */
    fun stopTracking() {
        sensorManager.unregisterListener(this)
        _activityStats.value = _activityStats.value.copy(isTracking = false)
        Log.d(TAG, "Activity tracking stopped")
    }

    /**
     * Resets step counter and all statistics.
     */
    fun reset() {
        initialStepCount = null
        currentStepCount = 0
        _activityStats.value = ActivityStats(isTracking = _activityStats.value.isTracking)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()

            // Initialize on first reading
            if (initialStepCount == null) {
                initialStepCount = totalSteps
                Log.d(TAG, "Initial step count: $totalSteps")
            }

            // Calculate steps since tracking started
            currentStepCount = totalSteps - (initialStepCount ?: totalSteps)
            updateStats()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    private fun updateStats() {
        val steps = currentStepCount
        val distance = steps * strideLengthMeters
        val calories = (steps * caloriesPerStep).toInt()

        _activityStats.value = ActivityStats(
            steps = steps,
            distanceMeters = distance,
            caloriesBurned = calories,
            isTracking = _activityStats.value.isTracking
        )

        Log.d(TAG, "Stats updated - Steps: $steps, Distance: ${"%.2f".format(distance)}m, Calories: $calories")
    }

    companion object {
        private const val TAG = "ActivityTracker"

        /**
         * Formats distance for display
         * @param meters Distance in meters
         * @return Formatted string (km if >= 1000m, m otherwise)
         */
        fun formatDistance(meters: Double): String {
            return if (meters >= 1000) {
                "${"%.2f".format(meters / 1000)} km"
            } else {
                "${meters.toInt()} m"
            }
        }

        /**
         * Estimates stride length based on height
         * @param heightCm Height in centimeters
         * @return Stride length in meters
         */
        fun estimateStrideLength(heightCm: Double): Double {
            return (heightCm / 100.0) * 0.415
        }
    }
}
