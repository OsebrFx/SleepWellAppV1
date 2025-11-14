package com.sleepwell.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val targetHours: Float,
    val targetQuality: Int,
    val streak: Int = 0, // Consecutive days meeting goal
    val bestStreak: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    fun isGoalMet(sleepHours: Float, quality: Int): Boolean {
        return sleepHours >= targetHours && quality >= targetQuality
    }

    fun getProgressPercentage(sleepHours: Float, quality: Int): Int {
        val hoursProgress = (sleepHours / targetHours * 50).coerceAtMost(50f)
        val qualityProgress = (quality.toFloat() / targetQuality * 50).coerceAtMost(50f)
        return (hoursProgress + qualityProgress).toInt()
    }
}
