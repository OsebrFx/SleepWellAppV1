package com.sleepwell.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sleepwell.data.model.User
import com.sleepwell.data.repository.SleepRepository
import com.sleepwell.utils.Constants
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SleepRepository(application)

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Long>>()
    val registerResult: LiveData<Result<Long>> = _registerResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) {
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val user = repository.loginUser(email, password)
                if (user != null) {
                    _loginResult.value = Result.success(user)
                } else {
                    _loginResult.value = Result.failure(Exception("Email ou mot de passe incorrect"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String, age: Int) {
        if (!validateRegisterInput(name, email, password, age)) {
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                // Check if email already exists
                if (repository.isEmailExists(email)) {
                    _registerResult.value = Result.failure(Exception("Cet email est déjà utilisé"))
                    _loading.value = false
                    return@launch
                }

                val user = User(
                    name = name,
                    email = email,
                    password = password,
                    age = age
                )

                val userId = repository.registerUser(user)
                _registerResult.value = Result.success(userId)
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _loginResult.value = Result.failure(Exception("L'email est requis"))
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _loginResult.value = Result.failure(Exception("Email invalide"))
                false
            }
            password.isBlank() -> {
                _loginResult.value = Result.failure(Exception("Le mot de passe est requis"))
                false
            }
            else -> true
        }
    }

    private fun validateRegisterInput(name: String, email: String, password: String, age: Int): Boolean {
        return when {
            name.isBlank() -> {
                _registerResult.value = Result.failure(Exception("Le nom est requis"))
                false
            }
            email.isBlank() -> {
                _registerResult.value = Result.failure(Exception("L'email est requis"))
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _registerResult.value = Result.failure(Exception("Email invalide"))
                false
            }
            password.length < Constants.MIN_PASSWORD_LENGTH -> {
                _registerResult.value = Result.failure(
                    Exception("Le mot de passe doit contenir au moins ${Constants.MIN_PASSWORD_LENGTH} caractères")
                )
                false
            }
            age < Constants.MIN_AGE -> {
                _registerResult.value = Result.failure(
                    Exception("Vous devez avoir au moins ${Constants.MIN_AGE} ans")
                )
                false
            }
            else -> true
        }
    }
}
