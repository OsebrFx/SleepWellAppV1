package com.sleepwell.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "sleep_sessions",
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
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val startTime: Date,
    val endTime: Date,
    val durationHours: Float,
    val quality: Int, // 0-100
    val deepSleepPercentage: Float,
    val lightSleepPercentage: Float,
    val remSleepPercentage: Float,
    val notes: String? = null,
    val mood: String? = null, // "excellent", "good", "fair", "poor"
    val createdAt: Date = Date()
) {
    fun getTotalDeepSleepHours(): Float = durationHours * (deepSleepPercentage / 100f)
    fun getTotalLightSleepHours(): Float = durationHours * (lightSleepPercentage / 100f)
    fun getTotalRemSleepHours(): Float = durationHours * (remSleepPercentage / 100f)

    fun getQualityLabel(): String {
        return when {
            quality >= 80 -> "excellent"
            quality >= 60 -> "good"
            quality >= 40 -> "fair"
            else -> "poor"
        }
    }
}
