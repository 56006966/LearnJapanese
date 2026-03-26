package com.example.learnjapanese.data

data class AppLevel(
    val number: Int,
    val title: String,
    val theme: String,
    val isUnlocked: Boolean,
    val cardCount: Int
)

object CurriculumRepository {
    private val seededCards = listOf(
        PhraseCard(
            id = "level-1-hello",
            english = "Hello",
            japanese = "\u3053\u3093\u306b\u3061\u306f",
            romaji = "konnichiwa",
            category = "Greetings",
            level = 1,
            unitTitle = "Meet and greet",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-1-morning",
            english = "Good morning",
            japanese = "\u304a\u306f\u3088\u3046",
            romaji = "ohayou",
            category = "Greetings",
            level = 1,
            unitTitle = "Meet and greet",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-1-thanks",
            english = "Thank you",
            japanese = "\u3042\u308a\u304c\u3068\u3046",
            romaji = "arigatou",
            category = "Politeness",
            level = 1,
            unitTitle = "Meet and greet",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-1-excuse-me",
            english = "Excuse me",
            japanese = "\u3059\u307f\u307e\u305b\u3093",
            romaji = "sumimasen",
            category = "Politeness",
            level = 1,
            unitTitle = "Meet and greet",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-2-yes",
            english = "Yes",
            japanese = "\u306f\u3044",
            romaji = "hai",
            category = "Basics",
            level = 2,
            unitTitle = "Simple answers",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-2-no",
            english = "No",
            japanese = "\u3044\u3044\u3048",
            romaji = "iie",
            category = "Basics",
            level = 2,
            unitTitle = "Simple answers",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-2-understand",
            english = "I understand",
            japanese = "\u308f\u304b\u308a\u307e\u3059",
            romaji = "wakarimasu",
            category = "Basics",
            level = 2,
            unitTitle = "Simple answers",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-2-dont-understand",
            english = "I don't understand",
            japanese = "\u308f\u304b\u308a\u307e\u305b\u3093",
            romaji = "wakarimasen",
            category = "Basics",
            level = 2,
            unitTitle = "Simple answers",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-3-water",
            english = "Water",
            japanese = "\u307f\u305a",
            romaji = "mizu",
            category = "Food",
            level = 3,
            unitTitle = "Cafe essentials",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-3-tea",
            english = "Tea",
            japanese = "\u304a\u3061\u3083",
            romaji = "ocha",
            category = "Food",
            level = 3,
            unitTitle = "Cafe essentials",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-3-please",
            english = "Please",
            japanese = "\u304a\u306d\u304c\u3044\u3057\u307e\u3059",
            romaji = "onegaishimasu",
            category = "Food",
            level = 3,
            unitTitle = "Cafe essentials",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-4-station",
            english = "Train station",
            japanese = "\u3048\u304d",
            romaji = "eki",
            category = "Travel",
            level = 4,
            unitTitle = "Getting around",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-4-bathroom",
            english = "Where is the bathroom?",
            japanese = "\u30c8\u30a4\u30ec\u306f\u3069\u3053\u3067\u3059\u304b",
            romaji = "toire wa doko desu ka",
            category = "Travel",
            level = 4,
            unitTitle = "Getting around",
            skills = listOf("flashcards", "listening", "typing")
        ),
        PhraseCard(
            id = "level-4-where-is-station",
            english = "Where is the station?",
            japanese = "\u3048\u304d\u306f\u3069\u3053\u3067\u3059\u304b",
            romaji = "eki wa doko desu ka",
            category = "Travel",
            level = 4,
            unitTitle = "Getting around",
            skills = listOf("flashcards", "listening", "typing")
        )
    )

    val levels: List<AppLevel> = (1..100).map { level ->
        val theme = when (level) {
            in 1..20 -> "Survival Japanese"
            in 21..40 -> "Daily life"
            in 41..60 -> "Conversation builder"
            in 61..80 -> "Intermediate situations"
            else -> "Refinement and review"
        }
        AppLevel(
            number = level,
            title = when (level) {
                1 -> "Greetings"
                2 -> "Simple answers"
                3 -> "Cafe essentials"
                4 -> "Getting around"
                else -> "Level $level"
            },
            theme = theme,
            isUnlocked = level <= 8,
            cardCount = seededCards.count { it.level == level }
        )
    }

    fun deckForLevel(level: Int): List<PhraseCard> {
        return seededCards.filter { it.level == level }
    }

    fun firstAvailableLevel(): Int {
        return levels.firstOrNull { it.cardCount > 0 }?.number ?: 1
    }
}
