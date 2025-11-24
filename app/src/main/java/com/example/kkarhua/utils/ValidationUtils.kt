package com.example.kkarhua.utils

import android.util.Patterns

object ValidationUtils {

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "El correo es requerido")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult(false, "Correo inválido")
            else -> ValidationResult(true)
        }
    }

    // ✅ VALIDACIÓN MEJORADA: Mínimo 8 caracteres, al menos una letra y un número
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "La contraseña es requerida")
            password.length < 8 ->
                ValidationResult(false, "Debe tener al menos 8 caracteres")
            !password.any { it.isLetter() } ->
                ValidationResult(false, "Debe contener al menos una letra")
            !password.any { it.isDigit() } ->
                ValidationResult(false, "Debe contener al menos un número")
            else -> ValidationResult(true)
        }
    }

    // ✅ NUEVA: Validación opcional de contraseña (para edición)
    fun validatePasswordOptional(password: String): ValidationResult {
        // Si está vacía, es válido (no se va a cambiar)
        if (password.isEmpty()) {
            return ValidationResult(true)
        }

        // Si tiene contenido, aplicar validación completa
        return validatePassword(password)
    }

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "El nombre es requerido")
            name.length < 3 ->
                ValidationResult(false, "El nombre debe tener al menos 3 caracteres")
            !name.all { it.isLetter() || it.isWhitespace() } ->
                ValidationResult(false, "El nombre solo debe contener letras")
            else -> ValidationResult(true)
        }
    }

    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() ->
                ValidationResult(false, "Confirma tu contraseña")
            password != confirmPassword ->
                ValidationResult(false, "Las contraseñas no coinciden")
            else -> ValidationResult(true)
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null
)