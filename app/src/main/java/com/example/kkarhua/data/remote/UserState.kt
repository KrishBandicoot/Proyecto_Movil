package com.example.kkarhua.data.remote

enum class UserState(val value: String) {
    ACTIVO("activo"),
    BLOQUEADO("bloqueado");

    companion object {
        fun fromString(value: String?): UserState {
            return when (value?.lowercase()) {
                "bloqueado" -> BLOQUEADO
                else -> ACTIVO // Default
            }
        }
    }
}