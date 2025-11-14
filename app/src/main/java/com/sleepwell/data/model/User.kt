package com.sleepwell.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val age: Int,
    val gender: String? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val darkModeEnabled: Boolean = false,
    val language: String = "fr",
    val sleepReminderEnabled: Boolean = false,
    val sleepReminderHour: Int = 22,
    val sleepReminderMinute: Int = 0,
    val wakeupReminderEnabled: Boolean = false,
    val wakeupReminderHour: Int = 7,
    val wakeupReminderMinute: Int = 0
)
