package com.sleepwell.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sleepwell.data.model.SleepSession
import com.sleepwell.data.model.User
import com.sleepwell.data.repository.SleepRepository
import com.sleepwell.utils.Constants
import com.sleepwell.utils.toCsvFormat
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SleepRepository(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _exportSuccess = MutableLiveData<File?>()
    val exportSuccess: LiveData<File?> = _exportSuccess

    fun loadUser(userId: Long) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                _user.value = user
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement du profil: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateDarkMode(userId: Long, enabled: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateDarkMode(userId, enabled)
                loadUser(userId)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun updateLanguage(userId: Long, language: String) {
        viewModelScope.launch {
            try {
                repository.updateLanguage(userId, language)
                loadUser(userId)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun updateSleepReminder(userId: Long, enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                repository.updateSleepReminder(userId, enabled, hour, minute)
                loadUser(userId)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun updateWakeupReminder(userId: Long, enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                repository.updateWakeupReminder(userId, enabled, hour, minute)
                loadUser(userId)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun exportDataToCSV(context: Context, userId: Long) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val sessions = repository.getAllSessionsByUserSync(userId)
                val file = createCSVFile(context, sessions)
                _exportSuccess.value = file
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'export: ${e.message}"
                _exportSuccess.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    private fun createCSVFile(context: Context, sessions: List<SleepSession>): File {
        val file = File(context.getExternalFilesDir(null), Constants.EXPORT_FILE_NAME)
        val writer = FileWriter(file)

        // Header
        writer.append("Date,Start Time,End Time,Duration (hours),Quality (%),Deep Sleep (%),Light Sleep (%),REM Sleep (%),Mood,Notes\n")

        // Data rows
        sessions.forEach { session ->
            writer.append("${session.startTime.toCsvFormat()},")
            writer.append("${session.startTime.toCsvFormat()},")
            writer.append("${session.endTime.toCsvFormat()},")
            writer.append("${session.durationHours},")
            writer.append("${session.quality},")
            writer.append("${session.deepSleepPercentage},")
            writer.append("${session.lightSleepPercentage},")
            writer.append("${session.remSleepPercentage},")
            writer.append("${session.mood ?: ""},")
            writer.append("\"${session.notes ?: ""}\"\n")
        }

        writer.flush()
        writer.close()

        return file
    }

    fun resetError() {
        _error.value = null
    }

    fun resetExportSuccess() {
        _exportSuccess.value = null
    }
}
