package com.sleepwell.utils

object Constants {
    // Database
    const val DATABASE_NAME = "sleepwell_database"
    const val DATABASE_VERSION = 1

    // Shared Preferences
    const val PREFS_NAME = "sleepwell_prefs"
    const val PREF_IS_FIRST_LAUNCH = "is_first_launch"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_USER_ID = "user_id"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_LANGUAGE = "language"
    const val PREF_SLEEP_REMINDER_ENABLED = "sleep_reminder_enabled"
    const val PREF_SLEEP_REMINDER_TIME = "sleep_reminder_time"
    const val PREF_WAKEUP_REMINDER_ENABLED = "wakeup_reminder_enabled"
    const val PREF_WAKEUP_REMINDER_TIME = "wakeup_reminder_time"

    // OpenAI API
    const val OPENAI_BASE_URL = "https://api.openai.com/"
    const val OPENAI_MODEL = "gpt-3.5-turbo"
    const val OPENAI_MAX_TOKENS = 500
    const val OPENAI_TEMPERATURE = 0.7

    // Notifications
    const val NOTIFICATION_CHANNEL_SLEEP_ID = "sleep_reminder_channel"
    const val NOTIFICATION_CHANNEL_SLEEP_NAME = "Rappels de sommeil"
    const val NOTIFICATION_CHANNEL_WAKEUP_ID = "wakeup_reminder_channel"
    const val NOTIFICATION_CHANNEL_WAKEUP_NAME = "Rappels de réveil"
    const val NOTIFICATION_ID_SLEEP = 1001
    const val NOTIFICATION_ID_WAKEUP = 1002

    // WorkManager
    const val WORK_SLEEP_REMINDER = "sleep_reminder_work"
    const val WORK_WAKEUP_REMINDER = "wakeup_reminder_work"

    // Sleep Quality
    const val MIN_SLEEP_QUALITY = 0
    const val MAX_SLEEP_QUALITY = 100
    const val MIN_SLEEP_HOURS = 0f
    const val MAX_SLEEP_HOURS = 24f

    // Goals
    const val MIN_GOAL_HOURS = 4f
    const val MAX_GOAL_HOURS = 12f
    const val DEFAULT_GOAL_HOURS = 8f
    const val MIN_GOAL_QUALITY = 50
    const val DEFAULT_GOAL_QUALITY = 80

    // Sleep Phases (in percentage)
    const val DEEP_SLEEP_PERCENTAGE = 0.25f
    const val LIGHT_SLEEP_PERCENTAGE = 0.50f
    const val REM_SLEEP_PERCENTAGE = 0.25f

    // Date formats
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_FULL = "dd MMMM yyyy HH:mm"
    const val TIME_FORMAT = "HH:mm"
    const val DATE_FORMAT_CSV = "yyyy-MM-dd HH:mm:ss"

    // Export
    const val EXPORT_FILE_NAME = "sleepwell_export.csv"
    const val EXPORT_MIME_TYPE = "text/csv"

    // Animation durations
    const val ANIM_DURATION_SHORT = 300L
    const val ANIM_DURATION_MEDIUM = 500L
    const val ANIM_DURATION_LONG = 800L

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_AGE = 18
    const val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    // Chart
    const val CHART_DAYS_TO_SHOW = 7
    const val CHART_ANIMATION_DURATION = 1000

    // Tip Categories
    const val TIP_CATEGORY_SLEEP_HYGIENE = "sleep_hygiene"
    const val TIP_CATEGORY_LIFESTYLE = "lifestyle"
    const val TIP_CATEGORY_DIET = "diet"
    const val TIP_CATEGORY_EXERCISE = "exercise"
    const val TIP_CATEGORY_ENVIRONMENT = "environment"
    const val TIP_CATEGORY_RELAXATION = "relaxation"
}
