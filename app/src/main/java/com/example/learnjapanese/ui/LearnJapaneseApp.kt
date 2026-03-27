package com.example.learnjapanese.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learnjapanese.data.AppLevel
import com.example.learnjapanese.data.CurriculumRepository
import com.example.learnjapanese.data.PhraseCard
import com.example.learnjapanese.data.StrokeGuide
import com.example.learnjapanese.data.TraceLesson
import com.example.learnjapanese.data.TraceLessonRepository
import com.example.learnjapanese.data.UserProfile
import com.example.learnjapanese.data.UserProfileRepository
import com.example.learnjapanese.data.WikiLookupResult
import com.example.learnjapanese.data.WikiLookupService
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class LearnTab(val title: String) {
    Flashcards("Flashcards"),
    Practice("Practice"),
    Tracing("Tracing")
}

private data class BuddySuggestion(
    val query: String,
    val label: String
)

@Composable
fun LearnJapaneseApp() {
    val context = LocalContext.current
    val tabs = LearnTab.entries
    val levels = CurriculumRepository.levels
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedLevel by rememberSaveable { mutableIntStateOf(CurriculumRepository.firstAvailableLevel()) }
    val currentLevel = levels.first { it.number == selectedLevel }
    val deck = CurriculumRepository.deckForLevel(selectedLevel)
    val traceLessons = TraceLessonRepository.starterLessons
    var buddyVisible by rememberSaveable { mutableStateOf(true) }
    var userProfile by remember { mutableStateOf(UserProfileRepository.load(context)) }
    var editingProfile by rememberSaveable { mutableStateOf(false) }
    
    val buddySuggestionState = rememberSaveable(
        selectedLevel,
        saver = listSaver<MutableState<BuddySuggestion>, String>(
            save = { listOf(it.value.query, it.value.label) },
            restore = { mutableStateOf(BuddySuggestion(it[0], it[1])) }
        )
    ) {
        mutableStateOf(
            BuddySuggestion(
                query = deck.firstOrNull()?.japanese.orEmpty(),
                label = deck.firstOrNull()?.english ?: "current card"
            )
        )
    }
    var buddySuggestion by buddySuggestionState

    LaunchedEffect(selectedLevel) {
        buddySuggestion = BuddySuggestion(
            query = deck.firstOrNull()?.japanese.orEmpty(),
            label = deck.firstOrNull()?.english ?: "current card"
        )
    }

    Scaffold { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        )
        {
            if (userProfile == null) {
                FirstLaunchOnboarding(
                    onComplete = { englishName ->
                        val profile = UserProfileRepository.generateKanjiProfile(englishName)
                        UserProfileRepository.save(context, profile)
                        userProfile = profile
                    }
                )
                return@BoxWithConstraints
            }

            val constraintsScope = this
            Column(modifier = Modifier.fillMaxSize()) {
                HeroHeader(
                    level = currentLevel,
                    profile = userProfile!!,
                    buddyVisible = buddyVisible,
                    onEditProfile = { editingProfile = true },
                    onToggleBuddy = { buddyVisible = !buddyVisible }
                )
                if (editingProfile) {
                    ProfileEditorCard(
                        profile = userProfile!!,
                        onSave = { englishName ->
                            val profile = UserProfileRepository.generateKanjiProfile(englishName)
                            UserProfileRepository.save(context, profile)
                            userProfile = profile
                            editingProfile = false
                        },
                        onCancel = { editingProfile = false }
                    )
                }
                LevelPicker(
                    levels = levels,
                    selectedLevel = selectedLevel,
                    onSelectLevel = { selectedLevel = it }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tab.title) }
                        )
                    }
                }

                when (tabs[selectedTab]) {
                    LearnTab.Flashcards -> FlashcardScreen(
                        deck = deck,
                        level = currentLevel,
                        onFocusCard = { buddySuggestion = BuddySuggestion(it.japanese, it.english) }
                    )
                    LearnTab.Practice -> DictationPracticeScreen(
                        deck = deck,
                        level = currentLevel,
                        onFocusCard = { buddySuggestion = BuddySuggestion(it.japanese, it.english) }
                    )
                    LearnTab.Tracing -> TracingLessonScreen(
                        profile = userProfile!!,
                        lessons = traceLessons,
                        onFocusLesson = { buddySuggestion = BuddySuggestion(it.character, it.meaning) }
                    )
                }
            }

            if (deck.isNotEmpty()) {
                if (buddyVisible) {
                    DraggableWikiBuddy(
                        profile = userProfile!!,
                        suggestedQuery = buddySuggestion.query,
                        suggestedLabel = buddySuggestion.label,
                        containerWidthDp = constraintsScope.maxWidth.value,
                        containerHeightDp = constraintsScope.maxHeight.value,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                } else {
                    MiniWikiBuddy(
                        kanjiName = userProfile!!.kanjiName,
                        onExpand = { buddyVisible = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(
    level: AppLevel,
    profile: UserProfile,
    buddyVisible: Boolean,
    onEditProfile: () -> Unit,
    onToggleBuddy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "English to Japanese",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Level ${level.number}: ${level.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = level.theme,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Welcome back, ${profile.kanjiName} (${profile.kanjiMeaning})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Row {
                IconButton(onClick = onEditProfile) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit kanji name",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = onToggleBuddy) {
                    Icon(
                        imageVector = if (buddyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (buddyVisible) "Hide helper" else "Show helper",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileEditorCard(
    profile: UserProfile,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var englishName by rememberSaveable(profile.englishName) { mutableStateOf(profile.englishName) }
    val preview = remember(englishName) {
        UserProfileRepository.generateKanjiProfile(englishName.ifBlank { "Friend" })
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Update your kanji name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = englishName,
                onValueChange = { englishName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("English name") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${preview.kanjiName}  •  ${preview.kanjiMeaning}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSave(englishName.ifBlank { "Friend" }) }) {
                    Text("Save")
                }
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun FirstLaunchOnboarding(
    onComplete: (String) -> Unit
) {
    var englishName by rememberSaveable { mutableStateOf("") }
    var previewProfile by remember(englishName) {
        mutableStateOf(UserProfileRepository.generateKanjiProfile(englishName.ifBlank { "Friend" }))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose your kanji name",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "On your first visit, we'll generate a kanji-style meaning name from your English name and use it around the app.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        OutlinedTextField(
            value = englishName,
            onValueChange = {
                englishName = it
                previewProfile = UserProfileRepository.generateKanjiProfile(it.ifBlank { "Friend" })
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your English name") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = previewProfile.kanjiName,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Meaning: ${previewProfile.kanjiMeaning}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This is a friendly kanji-style alias, not a strict real-world name translation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = { onComplete(englishName.ifBlank { "Friend" }) }) {
            Text("Start learning")
        }
    }
}

@Composable
private fun LevelPicker(
    levels: List<AppLevel>,
    selectedLevel: Int,
    onSelectLevel: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        levels.forEach { level ->
            val enabled = level.isUnlocked
            Surface(
                modifier = Modifier.clickable(enabled = enabled) { onSelectLevel(level.number) },
                shape = RoundedCornerShape(999.dp),
                color = if (selectedLevel == level.number) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = "L${level.number}",
                        color = if (selectedLevel == level.number) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            !level.isUnlocked -> "Locked"
                            level.cardCount == 0 -> "Soon"
                            else -> "${level.cardCount} cards"
                        },
                        color = if (selectedLevel == level.number) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardScreen(
    deck: List<PhraseCard>,
    level: AppLevel,
    onFocusCard: (PhraseCard) -> Unit
) {
    val revealedCards = remember(level.number) { mutableStateListOf<Int>() }
    val speaker = rememberJapaneseSpeaker()

    LaunchedEffect(level.number) {
        deck.firstOrNull()?.let(onFocusCard)
    }

    if (deck.isEmpty()) {
        EmptyLevelState(level = level)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(deck) { index, card ->
            val revealed = index in revealedCards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onFocusCard(card)
                        if (revealed) revealedCards.remove(index) else revealedCards.add(index)
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (revealed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = card.category.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (revealed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = card.unitTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (revealed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                onFocusCard(card)
                                speaker.speak(card.japanese)
                            },
                            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Japanese audio",
                                tint = if (revealed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = card.english,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (revealed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (revealed) "${card.japanese}\n${card.romaji}" else "Tap to reveal",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (revealed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DictationPracticeScreen(
    deck: List<PhraseCard>,
    level: AppLevel,
    onFocusCard: (PhraseCard) -> Unit
) {
    if (deck.isEmpty()) {
        EmptyLevelState(level = level)
        return
    }

    var currentIndex by rememberSaveable(level.number) { mutableIntStateOf(0) }
    var answer by rememberSaveable(level.number) { mutableStateOf("") }
    var feedback by rememberSaveable(level.number) { mutableStateOf("Listen and type the Japanese in kana or romaji.") }
    val currentCard = deck[currentIndex]
    val speaker = rememberJapaneseSpeaker()

    LaunchedEffect(level.number, currentIndex) {
        answer = ""
        feedback = "Listen and type the Japanese in kana or romaji."
        onFocusCard(currentCard)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PracticeStatPill(currentIndex = currentIndex + 1, total = deck.size) }
        item { SpeechBubbleTip(text = "I'm following this card now. Tap the helper later for a quick Wiktionary hint.") }
        item {
            Text(
                text = "Meaning: ${currentCard.english}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = "Unit: ${currentCard.unitTitle}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Button(onClick = { speaker.speak(currentCard.japanese) }) {
                Text("Play Japanese audio")
            }
        }
        item {
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Your answer") },
                placeholder = { Text("Example: konnichiwa or \u3053\u3093\u306b\u3061\u306f") },
                minLines = 2
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val normalized = answer.trim().lowercase()
                    val isCorrect = normalized == currentCard.japanese.lowercase() ||
                        normalized == currentCard.romaji.lowercase()
                    feedback = if (isCorrect) {
                        "Correct! ${currentCard.japanese} (${currentCard.romaji})"
                    } else {
                        "Not quite. Correct answer: ${currentCard.japanese} (${currentCard.romaji})"
                    }
                }) {
                    Text("Check")
                }
                Button(onClick = { currentIndex = (currentIndex + 1) % deck.size }) {
                    Text("Next card")
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = feedback,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        item { HorizontalDivider() }
        item {
            Text(
                text = "Level ${level.number} deck",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        itemsIndexed(deck) { index, card ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { currentIndex = index },
                shape = RoundedCornerShape(18.dp),
                color = if (index == currentIndex) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(card.english, fontWeight = FontWeight.Medium)
                        Text(
                            text = card.japanese,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TracingLessonScreen(
    profile: UserProfile,
    lessons: List<TraceLesson>,
    onFocusLesson: (TraceLesson) -> Unit
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val lesson = lessons[selectedIndex]

    LaunchedEffect(selectedIndex) {
        onFocusLesson(lesson)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SpeechBubbleTip(text = "${profile.kanjiName}, trace the shape a few times, say the reading out loud, then move to the next character.")
        }
        item {
            Text(
                text = "${lesson.type} tracing",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Text(
                text = lesson.character,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = "Reading: ${lesson.reading}  |  Meaning: ${lesson.meaning}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        item {
            SpeechBubbleTip(text = lesson.strokeHint)
        }
        item {
            TracingCanvas(
                character = lesson.character,
                guides = lesson.guides
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    selectedIndex = if (selectedIndex == 0) lessons.lastIndex else selectedIndex - 1
                }) {
                    Text("Previous")
                }
                Button(onClick = {
                    selectedIndex = (selectedIndex + 1) % lessons.size
                }) {
                    Text("Next")
                }
            }
        }
        item { HorizontalDivider() }
        item {
            Text(
                text = "Starter tracing lessons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        itemsIndexed(lessons) { index, traceLesson ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedIndex = index },
                shape = RoundedCornerShape(18.dp),
                color = if (index == selectedIndex) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = traceLesson.character,
                        fontSize = 30.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text("${traceLesson.type}: ${traceLesson.reading}", fontWeight = FontWeight.Medium)
                        Text(
                            text = traceLesson.meaning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TracingCanvas(
    character: String,
    guides: List<StrokeGuide>
) {
    val strokes = remember(character) { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember(character) { mutableStateListOf<Offset>() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val guideColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF7F2EB))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(character) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStroke.clear()
                                    currentStroke.add(offset)
                                },
                                onDragEnd = {
                                    if (currentStroke.isNotEmpty()) {
                                        strokes.add(currentStroke.toList())
                                        currentStroke.clear()
                                    }
                                }
                            ) { change, _ ->
                                change.consume()
                                val nextPoint = change.position
                                currentStroke.add(nextPoint)
                            }
                        }
                ) {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            character,
                            size.width / 2f,
                            size.height / 2f + 48f,
                            android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 180f
                                color = android.graphics.Color.argb(40, 80, 80, 80)
                            }
                        )
                    }

                    guides.forEachIndexed { index, guide ->
                        val start = Offset(size.width * guide.startX, size.height * guide.startY)
                        val end = Offset(size.width * guide.endX, size.height * guide.endY)
                        drawLine(
                            color = guideColor,
                            start = start,
                            end = end,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                "${index + 1}",
                                start.x + 8f,
                                start.y - 8f,
                                android.graphics.Paint().apply {
                                    textAlign = android.graphics.Paint.Align.LEFT
                                    textSize = 34f
                                    color = android.graphics.Color.argb(180, 175, 62, 45)
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }

                    strokes.forEach { points ->
                        if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 12f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    if (currentStroke.size > 1) {
                        val path = Path().apply {
                            moveTo(currentStroke.first().x, currentStroke.first().y)
                            currentStroke.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = secondaryColor,
                            style = Stroke(width = 12f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    strokes.clear()
                    currentStroke.clear()
                }) {
                    Text("Clear")
                }
                Text(
                    text = "Trace over the faded guide to build muscle memory.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DraggableWikiBuddy(
    profile: UserProfile,
    suggestedQuery: String,
    suggestedLabel: String,
    containerWidthDp: Float,
    containerHeightDp: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalConfiguration.current.densityDpi / 160f
    val bubbleWidthPx = 320f * density
    val bubbleHeightPx = 360f * density
    val marginPx = 16f * density
    val minX = -(containerWidthDp * density) + bubbleWidthPx + marginPx
    val maxX = -marginPx
    val minY = -(containerHeightDp * density) + bubbleHeightPx + marginPx + (72f * density)
    val maxY = -marginPx

    var offsetX by rememberSaveable { mutableFloatStateOf(-marginPx) }
    var offsetY by rememberSaveable { mutableFloatStateOf(-marginPx) }
    var minimized by rememberSaveable { mutableStateOf(false) }

    fun snapX(value: Float): Float = if (abs(value - minX) < abs(value - maxX)) minX else maxX

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(containerWidthDp, containerHeightDp) {
                detectDragGestures(
                    onDragEnd = {
                        offsetX = snapX(offsetX)
                        offsetY = offsetY.coerceIn(minY, maxY)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x).coerceIn(minX, maxX)
                    offsetY = (offsetY + dragAmount.y).coerceIn(minY, maxY)
                }
            }
            .padding(16.dp)
    ) {
        if (minimized) {
            MiniWikiBuddy(
                kanjiName = profile.kanjiName,
                onExpand = { minimized = false }
            )
        } else {
            WikiBuddyWidget(
                profile = profile,
                suggestedQuery = suggestedQuery,
                suggestedLabel = suggestedLabel,
                onMinimize = { minimized = true }
            )
        }
    }
}

@Composable
private fun MiniWikiBuddy(
    kanjiName: String,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animation = rememberInfiniteTransition(label = "mini-clip-bob")
    val floatOffset by animation.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mini-clip-offset"
    )

    Surface(
        modifier = modifier.clickable { onExpand() },
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u53cb",
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer { translationY = floatOffset },
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = kanjiName,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WikiBuddyWidget(
    profile: UserProfile,
    suggestedQuery: String,
    suggestedLabel: String,
    onMinimize: () -> Unit
) {
    val animation = rememberInfiniteTransition(label = "clip-bob")
    val floatOffset by animation.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clip-offset"
    )
    val tips = listOf(
        "Drag me to an edge if I'm in the way.",
        "I follow your current card now.",
        "Use the current card button for a quick lookup."
    )
    var tipIndex by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable(suggestedQuery) { mutableStateOf(suggestedQuery) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<WikiLookupResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(suggestedQuery) {
        if (query.isBlank() || query == suggestedQuery || result == null) {
            query = suggestedQuery
        }
        tipIndex = (tipIndex + 1) % tips.size
    }

    Surface(
        modifier = Modifier.width(320.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { translationY = floatOffset }
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u53cb",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WikiBuddy for ${profile.kanjiName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile.kanjiName}, need a quick hint for \"$suggestedLabel\"? I can ask Wiktionary.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onMinimize) {
                    Text(text = "_", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            SpeechBubbleTip(text = tips[tipIndex])
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Look up a Japanese word") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    loading = true
                    error = null
                    result = null
                }) {
                    Text("Ask WikiBuddy")
                }
                Button(onClick = { query = suggestedQuery }) {
                    Text("Use current card")
                }
            }
            if (loading) {
                LaunchedEffect(query) {
                    WikiLookupService.lookup(query)
                        .onSuccess { result = it }
                        .onFailure { error = it.message ?: "Could not reach Wiktionary." }
                    loading = false
                }
            }
            if (loading) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text("Looking it up...")
                }
            }
            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            result?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = it.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = it.extract.take(320).ifBlank { "No summary available." })
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = it.sourceUrl,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeechBubbleTip(text: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyLevelState(level: AppLevel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Level ${level.number} is ready for content",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "This slot is part of the 100-level curriculum and can be filled from free Tatoeba-backed phrases later.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PracticeStatPill(currentIndex: Int, total: Int) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    ) {
        Text(
            text = "Card $currentIndex of $total",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

private class JapaneseSpeaker(context: Context) : TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(context, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            textToSpeech.language = Locale.JAPANESE
            textToSpeech.setSpeechRate(0.9f)
        }
    }

    fun speak(text: String) {
        if (ready) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jp_practice")
        }
    }

    fun shutdown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}

@Composable
private fun rememberJapaneseSpeaker(): JapaneseSpeaker {
    val context = LocalContext.current
    val speaker = remember(context) { JapaneseSpeaker(context) }

    DisposableEffect(speaker) {
        onDispose { speaker.shutdown() }
    }

    return speaker
}
