package com.sleepwell.fitness

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Workout data classes and utilities.
 */
class WorkoutDataTest {

    @Test
    fun `test workout creation`() {
        val workout = Workout(
            type = WorkoutType.RUNNING,
            durationMinutes = 30,
            caloriesBurned = 300,
            notes = "Morning run"
        )

        assertEquals(WorkoutType.RUNNING, workout.type)
        assertEquals(30, workout.durationMinutes)
        assertEquals(300, workout.caloriesBurned)
        assertEquals("Morning run", workout.notes)
    }

    @Test
    fun `test workout formatted duration`() {
        val workout1 = Workout(type = WorkoutType.RUNNING, durationMinutes = 30)
        assertEquals("30min", workout1.getFormattedDuration())

        val workout2 = Workout(type = WorkoutType.RUNNING, durationMinutes = 90)
        assertEquals("1h 30min", workout2.getFormattedDuration())

        val workout3 = Workout(type = WorkoutType.RUNNING, durationMinutes = 125)
        assertEquals("2h 5min", workout3.getFormattedDuration())
    }

    @Test
    fun `test workout date formatting`() {
        val workout = Workout(
            type = WorkoutType.RUNNING,
            durationMinutes = 30,
            timestamp = 1700000000000L // Fixed timestamp
        )

        val formatted = workout.getFormattedDate()
        assertTrue(formatted.isNotEmpty())
        // Format should contain date and time
        assertTrue(formatted.contains(","))
    }

    @Test
    fun `test workout type calories per minute`() {
        assertEquals(10.0, WorkoutType.RUNNING.caloriesPerMinute)
        assertEquals(4.0, WorkoutType.WALKING.caloriesPerMinute)
        assertEquals(8.0, WorkoutType.CYCLING.caloriesPerMinute)
        assertEquals(11.0, WorkoutType.SWIMMING.caloriesPerMinute)
        assertEquals(3.0, WorkoutType.YOGA.caloriesPerMinute)
        assertEquals(6.0, WorkoutType.GYM.caloriesPerMinute)
        assertEquals(7.0, WorkoutType.SPORTS.caloriesPerMinute)
        assertEquals(5.0, WorkoutType.OTHER.caloriesPerMinute)
    }

    @Test
    fun `test workout type from string`() {
        assertEquals(WorkoutType.RUNNING, WorkoutType.fromString("RUNNING"))
        assertEquals(WorkoutType.WALKING, WorkoutType.fromString("WALKING"))
        assertEquals(WorkoutType.OTHER, WorkoutType.fromString("INVALID"))
        assertEquals(WorkoutType.OTHER, WorkoutType.fromString(""))
    }

    @Test
    fun `test workout stats formatted duration`() {
        val stats1 = WorkoutStats(
            totalWorkouts = 5,
            totalMinutes = 45,
            totalCalories = 450,
            daysBack = 7
        )
        assertEquals("45min", stats1.getFormattedDuration())

        val stats2 = WorkoutStats(
            totalWorkouts = 10,
            totalMinutes = 185,
            totalCalories = 1850,
            daysBack = 7
        )
        assertEquals("3h 5min", stats2.getFormattedDuration())
    }

    @Test
    fun `test workout unique IDs`() {
        val workout1 = Workout(type = WorkoutType.RUNNING, durationMinutes = 30)
        val workout2 = Workout(type = WorkoutType.RUNNING, durationMinutes = 30)

        // Each workout should have a unique ID
        assertTrue(workout1.id != workout2.id)
    }
}
