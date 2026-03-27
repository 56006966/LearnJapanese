package com.example.learnjapanese.data

data class StrokeGuide(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float
)

data class TraceLesson(
    val id: String,
    val character: String,
    val reading: String,
    val meaning: String,
    val type: String,
    val strokeHint: String,
    val guides: List<StrokeGuide>
)

object TraceLessonRepository {
    val starterLessons = listOf(
        TraceLesson(
            id = "h-a",
            character = "\u3042",
            reading = "a",
            meaning = "ah",
            type = "Hiragana",
            strokeHint = "3 strokes: top curve, vertical body, finishing curve.",
            guides = listOf(
                StrokeGuide(0.34f, 0.28f, 0.56f, 0.23f),
                StrokeGuide(0.47f, 0.18f, 0.46f, 0.72f),
                StrokeGuide(0.36f, 0.48f, 0.68f, 0.64f)
            )
        ),
        TraceLesson(
            id = "h-i",
            character = "\u3044",
            reading = "i",
            meaning = "ee",
            type = "Hiragana",
            strokeHint = "2 strokes: left short curve, then longer right curve.",
            guides = listOf(
                StrokeGuide(0.36f, 0.32f, 0.42f, 0.62f),
                StrokeGuide(0.56f, 0.25f, 0.62f, 0.72f)
            )
        ),
        TraceLesson(
            id = "h-u",
            character = "\u3046",
            reading = "u",
            meaning = "oo",
            type = "Hiragana",
            strokeHint = "2 strokes: small top mark, then the larger curve below.",
            guides = listOf(
                StrokeGuide(0.46f, 0.24f, 0.54f, 0.23f),
                StrokeGuide(0.39f, 0.42f, 0.63f, 0.67f)
            )
        ),
        TraceLesson(
            id = "h-e",
            character = "\u3048",
            reading = "e",
            meaning = "eh",
            type = "Hiragana",
            strokeHint = "2 strokes: top sweep, then broad crossing curve.",
            guides = listOf(
                StrokeGuide(0.34f, 0.28f, 0.60f, 0.24f),
                StrokeGuide(0.35f, 0.46f, 0.66f, 0.63f)
            )
        ),
        TraceLesson(
            id = "h-o",
            character = "\u304a",
            reading = "o",
            meaning = "oh",
            type = "Hiragana",
            strokeHint = "3 strokes: left sweep, center line, then right curve.",
            guides = listOf(
                StrokeGuide(0.32f, 0.30f, 0.47f, 0.24f),
                StrokeGuide(0.50f, 0.19f, 0.48f, 0.71f),
                StrokeGuide(0.46f, 0.43f, 0.67f, 0.62f)
            )
        ),
        TraceLesson(
            id = "k-sun",
            character = "\u65e5",
            reading = "nichi / hi",
            meaning = "sun, day",
            type = "Kanji",
            strokeHint = "4 strokes: top, left side, inner divider, close box on the right.",
            guides = listOf(
                StrokeGuide(0.34f, 0.22f, 0.66f, 0.22f),
                StrokeGuide(0.36f, 0.22f, 0.36f, 0.74f),
                StrokeGuide(0.40f, 0.47f, 0.62f, 0.47f),
                StrokeGuide(0.66f, 0.22f, 0.66f, 0.74f)
            )
        ),
        TraceLesson(
            id = "k-person",
            character = "\u4eba",
            reading = "hito / jin",
            meaning = "person",
            type = "Kanji",
            strokeHint = "2 strokes: left falling stroke, then longer right sweep.",
            guides = listOf(
                StrokeGuide(0.48f, 0.24f, 0.38f, 0.72f),
                StrokeGuide(0.50f, 0.24f, 0.65f, 0.74f)
            )
        ),
        TraceLesson(
            id = "k-tree",
            character = "\u6728",
            reading = "ki / moku",
            meaning = "tree",
            type = "Kanji",
            strokeHint = "4 strokes: top line, vertical trunk, left branch, right branch.",
            guides = listOf(
                StrokeGuide(0.34f, 0.30f, 0.66f, 0.30f),
                StrokeGuide(0.50f, 0.18f, 0.50f, 0.78f),
                StrokeGuide(0.48f, 0.49f, 0.35f, 0.67f),
                StrokeGuide(0.52f, 0.49f, 0.67f, 0.67f)
            )
        )
    )
}
