package com.example.learnjapanese.data

import android.content.Context

data class UserProfile(
    val englishName: String,
    val kanjiName: String,
    val kanjiMeaning: String
)

object UserProfileRepository {
    private const val PREFS_NAME = "learn_japanese_profile"
    private const val KEY_ENGLISH_NAME = "english_name"
    private const val KEY_KANJI_NAME = "kanji_name"
    private const val KEY_KANJI_MEANING = "kanji_meaning"

    fun load(context: Context): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val englishName = prefs.getString(KEY_ENGLISH_NAME, null) ?: return null
        val kanjiName = prefs.getString(KEY_KANJI_NAME, null) ?: return null
        val kanjiMeaning = prefs.getString(KEY_KANJI_MEANING, null) ?: return null
        return UserProfile(
            englishName = englishName,
            kanjiName = kanjiName,
            kanjiMeaning = kanjiMeaning
        )
    }

    fun save(context: Context, profile: UserProfile) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENGLISH_NAME, profile.englishName)
            .putString(KEY_KANJI_NAME, profile.kanjiName)
            .putString(KEY_KANJI_MEANING, profile.kanjiMeaning)
            .apply()
    }

    fun generateKanjiProfile(englishName: String): UserProfile {
        val normalized = englishName.trim().ifBlank { "Friend" }
        val firstLetter = normalized.first().uppercaseChar()
        val secondKanji = when (normalized.length % 5) {
            0 -> KanjiPart("\u7fd4", "soaring")
            1 -> KanjiPart("\u5149", "light")
            2 -> KanjiPart("\u82b1", "flower")
            3 -> KanjiPart("\u661f", "star")
            else -> KanjiPart("\u6d77", "sea")
        }
        val firstKanji = initialMap[firstLetter] ?: KanjiPart("\u53cb", "friend")

        return UserProfile(
            englishName = normalized,
            kanjiName = firstKanji.kanji + secondKanji.kanji,
            kanjiMeaning = "${firstKanji.meaning} + ${secondKanji.meaning}"
        )
    }

    private data class KanjiPart(
        val kanji: String,
        val meaning: String
    )

    private val initialMap = mapOf(
        'A' to KanjiPart("\u611b", "love"),
        'B' to KanjiPart("\u7f8e", "beauty"),
        'C' to KanjiPart("\u667a", "wisdom"),
        'D' to KanjiPart("\u5927", "greatness"),
        'E' to KanjiPart("\u6075", "blessing"),
        'F' to KanjiPart("\u98a8", "wind"),
        'G' to KanjiPart("\u5149", "radiance"),
        'H' to KanjiPart("\u967d", "sunshine"),
        'I' to KanjiPart("\u6cc9", "spring"),
        'J' to KanjiPart("\u7d14", "purity"),
        'K' to KanjiPart("\u5e0c", "hope"),
        'L' to KanjiPart("\u83ef", "splendor"),
        'M' to KanjiPart("\u771f", "truth"),
        'N' to KanjiPart("\u548c", "harmony"),
        'O' to KanjiPart("\u685c", "cherry blossom"),
        'P' to KanjiPart("\u5e73", "peace"),
        'Q' to KanjiPart("\u7434", "harp"),
        'R' to KanjiPart("\u96f7", "thunder"),
        'S' to KanjiPart("\u661f", "star"),
        'T' to KanjiPart("\u5929", "sky"),
        'U' to KanjiPart("\u512a", "gentleness"),
        'V' to KanjiPart("\u52dd", "victory"),
        'W' to KanjiPart("\u671b", "dream"),
        'X' to KanjiPart("\u971e", "glow"),
        'Y' to KanjiPart("\u96ea", "snow"),
        'Z' to KanjiPart("\u5584", "goodness")
    )
}
