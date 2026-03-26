package com.example.learnjapanese.data

data class PhraseCard(
    val id: String,
    val english: String,
    val japanese: String,
    val romaji: String,
    val category: String,
    val level: Int,
    val unitTitle: String,
    val skills: List<String>
)
