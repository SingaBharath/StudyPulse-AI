# StudyPulse AI

A production-grade, offline-first Android application designed for intelligent syllabus retention and revision. Built with modern Android standards: **Jetpack Compose (Material 3)**, **Room Database**, **Coroutines & Reactive StateFlow**, and structured **AI Flashcard Synthesis**.

---

## Architecture

StudyPulse AI follows **Clean Architecture** combined with the **MVVM** pattern, cleanly separated into four decoupled layers:

```
app/src/main/java/com/bharath/studypulseai/
├── data/
│   ├── local/
│   │   ├── StudyPulseDatabase.kt       # Room v3 Singleton + Automated Migrations (1→2, 2→3)
│   │   ├── dao/StudyNoteDao.kt         # Reactive Flow queries & CRUD operations
│   │   └── entity/StudyNoteEntity.kt   # Local SQLite persistence model
│   ├── mapper/StudyNoteMapper.kt       # Bidirectional Domain ↔ Entity data mapper
│   └── repository/StudyRepository.kt   # Single Source of Truth & AI Synthesis Engine
├── domain/
│   └── model/StudyNote.kt              # Domain Models (StudyNote, StudyStats, Difficulty, SortOrder)
├── presentation/
│   ├── viewmodel/StudyViewModel.kt     # Reactive StateFlow, Channel one-shot events, debounced search
│   └── ui/
│       ├── components/
│       │   ├── CommonComponents.kt      # Skeletons, RingProgress, Badges, EmptyStates
│       │   ├── NoteCard.kt              # Interactive Card with Accordion Flashcard view
│       │   └── AddNoteBottomSheet.kt    # Validated topic creation with live AI progress state
│       └── screen/
│           ├── HomeScreen.kt            # Command Center: Dashboard, search, filters, list
│           └── StatsBottomSheet.kt      # Deep-dive study analytics and mastery metrics
└── ui/theme/
    ├── Color.kt                        # Premium semantic color palette (Light & Dark)
    ├── Shape.kt                        # Unified corner curvature system
    ├── Theme.kt                        # Dynamic edge-to-edge Material 3 theme wrapper
    └── Type.kt                         # Typography scale
```

---

## Key Features

### 1. Multi-Perspective AI Flashcard Synthesis
- Automatically analyzes syllabus notes, lecture transcripts, or raw bullet points.
- Extracts core definitions without mangling acronyms (`ACID`, `FIFO`, `REST`, `O(1)`) or punctuation.
- **Dynamic Regeneration with 4 Perspectives**:
  1. **Core Concepts & Definitions**: Foundational rules, keywords, and primary mechanics.
  2. **Exam High-Yield & Common Pitfalls**: Edge cases, trade-offs, and Feynman technique prompts.
  3. **Deep-Dive & Architecture**: Structural breakdowns and blurting self-tests.
  4. **Rapid-Fire Flashcard Q&A**: Clean Front (Prompt) and Back (Answer) flashcard format.

### 2. Focused Accordion Study UX
- **Single-Card Focus**: Expanding one topic automatically keeps other topics collapsed, preventing visual clutter during intensive revision.
- Tap anywhere on the card header, the sparkle action button, or the expand chevron to toggle.
- Real-time animated spinner and `"Regenerating…"` visual state when refreshing cards.

### 3. Spaced Repetition (SM-2 Algorithm)
- Automatically schedules review intervals based on mastery level ($1 \rightarrow 3 \rightarrow 7 \rightarrow 14 \rightarrow 30$ days).
- **Due Today Banner**: Instantly flags topics whose review date has arrived.
- One-tap `"Log Review"` records study sessions and updates progress.

### 4. Interactive Analytics & Dashboard
- **Live Progress Ring & Linear Mastery Tracker**: Animated transitions reflecting completed topics.
- **Detailed Analytics Sheet**: Track Total Topics, Mastered, Pending, Total Reviews, and Completion Percentage.

### 5. Multi-Criteria Filtering & Live Search
- **Instant Debounced Search**: Searches both topic titles and note contents without freezing the UI.
- **Filter Chips**: All, Pending, Mastered, Pinned, and Due Today.
- **Multi-Sort Options**: Sort by Newest, Oldest, Alphabetical (A → Z), Difficulty (Hard → Easy), or Most Reviewed.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Kotlin** | Modern, concise, and type-safe language |
| **Jetpack Compose** | Declarative, reactive UI toolkit |
| **Material 3** | Latest design tokens, colors, elevation, and typography |
| **Room Database (v3)** | Offline-first SQLite persistence with explicit migrations |
| **Kotlin Coroutines & Flow** | Asynchronous execution and reactive state streams |
| **Android Lifecycle & ViewModel** | Architecture components managing UI lifecycle & configuration changes |
| **KSP (Kotlin Symbol Processing)** | High-performance Room annotation processing |

---

## Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17 or JDK 21
- Android SDK 35+ (Compile SDK: 37, Min SDK: 24)

### Clone & Build
```bash
git clone https://github.com/SingaBharath/StudyPulse-AI.git
cd StudyPulse-AI
```

1. Open the project in **Android Studio**.
2. Let Gradle sync project dependencies.
3. Select an emulator or connected Android device (Android 7.0+ / API 24+).
4. Click **Run (`Shift + F10`)**.

---

## Connecting a Live LLM (Gemini / OpenAI)

In [`StudyRepository.kt`](app/src/main/java/com/bharath/studypulseai/data/repository/StudyRepository.kt), the `generateAiSummary` function is designed as a drop-in integration point. To connect a live remote AI backend, replace the local synthesis engine with your API call:

```kotlin
suspend fun generateAiSummary(subject: String, rawNotes: String, styleIndex: Int = 0): Result<String> =
    runCatching {
        val response = geminiService.generateStudyCard(
            prompt = "Synthesize study note for $subject: $rawNotes (Style index: $styleIndex)"
        )
        response.body()?.text ?: fallbackSummary(subject, rawNotes)
    }
```

---

## Author
- **Bharath** - [@SingaBharath](https://github.com/SingaBharath)
