package com.example.kkarhua.data.local

enum class ProductCategory(val displayName: String) {
    COLLARES("collares"),
    AROS("aros"),
    BROCHES("broches"),
    ANILLOS("anillos"),
    PULSERAS("pulseras"),
    TOBILLERAS("tobilleras"),
    OTROS("otros");

    companion object {
        fun getAllCategories(): List<String> {
            return values().map { it.displayName }
        }

        fun fromDisplayName(name: String): ProductCategory? {
            return values().find { it.displayName == name }
        }
    }
}