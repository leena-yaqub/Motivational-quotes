package com.example.motivationalquote

data class Quote(
    val id: String = "",          // for Firestore document ID
    val text: String = "",
    val author: String = "",
    val category: String = "",
    var isFavorite: Boolean = false   // 🔹 new property for favorites
)
