package com.example.learnjapanese.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.learnjapanese.data.WikiLookupResult
import com.example.learnjapanese.data.WikiLookupService
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class LearnTab(val title: String) {
    Flashcards("Flashcards"),
    Practice("Practice")
}

private data class BuddySuggestion(
    val query: String,
    val label: String
)

@Composable
fun LearnJapaneseApp() {
    val tabs = LearnTab.entries
    val levels = CurriculumRepository.levels
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedLevel by rememberSaveable { mutableIntStateOf(CurriculumRepository.firstAvailableLevel()) }
    val currentLevel = levels.first { it.number == selectedLevel }
    val deck = CurriculumRepository.deckForLevel(selectedLevel)
    var buddyVisible by rememberSaveable { mutableStateOf(true) }
    
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
            val constraintsScope = this
            Column(modifier = Modifier.fillMaxSize()) {
                HeroHeader(
                    level = currentLevel,
                    buddyVisible = buddyVisible,
                    onToggleBuddy = { buddyVisible = !buddyVisible }
                )
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
                }
            }

            if (deck.isNotEmpty()) {
                if (buddyVisible) {
                    DraggableWikiBuddy(
                        suggestedQuery = buddySuggestion.query,
                        suggestedLabel = buddySuggestion.label,
                        containerWidthDp = constraintsScope.maxWidth.value,
                        containerHeightDp = constraintsScope.maxHeight.value,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                } else {
                    MiniWikiBuddy(
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
    buddyVisible: Boolean,
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
private fun DraggableWikiBuddy(
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
            MiniWikiBuddy(onExpand = { minimized = false })
        } else {
            WikiBuddyWidget(
                suggestedQuery = suggestedQuery,
                suggestedLabel = suggestedLabel,
                onMinimize = { minimized = true }
            )
        }
    }
}

@Composable
private fun MiniWikiBuddy(
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
                text = "\uD83D\uDCCE",
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer { translationY = floatOffset }
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "WikiBuddy",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WikiBuddyWidget(
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
                    Text(text = "\uD83D\uDCCE", fontSize = 24.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WikiBuddy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Need a quick hint for \"$suggestedLabel\"? I can ask Wiktionary.",
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
