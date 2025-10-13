package com.example.kkarhua.utils

object ValidationUtils {
    fun validateEmail(email: String): ValidationResult {
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches())
            ValidationResult(true)
        else
            ValidationResult(false, "Correo inválido")
    }

    fun validatePassword(pass: String): ValidationResult {
        return if (pass.length < 6)
            ValidationResult(false, "Debe tener al menos 6 caracteres")
        else
            ValidationResult(true)
    }
}

data class ValidationResult(val valid: Boolean, val message: String? = null)
