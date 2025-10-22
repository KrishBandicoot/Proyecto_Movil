package com.example.kkarhua.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kkarhua.data.remote.AuthResponse
import com.example.kkarhua.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData<AuthResponse?>()
    val loginSuccess: LiveData<AuthResponse?> = _loginSuccess

    private val _signupSuccess = MutableLiveData<AuthResponse?>()
    val signupSuccess: LiveData<AuthResponse?> = _signupSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.signup(name, email, password)

            result.onSuccess { authResponse ->
                _signupSuccess.value = authResponse
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error en el registro"
            }

            _isLoading.value = false
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error inesperado"
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.login(email, password)

            result.onSuccess { authResponse ->
                _loginSuccess.value = authResponse
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error en el inicio de sesión"
            }

            _isLoading.value = false
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error inesperado"
            _isLoading.value = false
        }
    }

    fun isAuthenticated(): Boolean {
        return repository.isAuthenticated()
    }

    fun logout() {
        repository.logout()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessEvents() {
        _loginSuccess.value = null
        _signupSuccess.value = null
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}