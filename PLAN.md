# TiviMate Clone — Kotlin/Compose for TV

## Project Overview

Build an IPTV player for Android TV with: M3U/Xtream playlist loading, XMLTV EPG parsing, synced EPG grid, video preview, fullscreen playback, favorites, and catchup.

**Tech Stack:**
- **Language:** Kotlin
- **UI:** Jetpack Compose for TV (Material 3 for TV)
- **Video:** Media3 (ExoPlayer)
- **Architecture:** MVVM + Repository pattern
- **Async:** Kotlin Coroutines + Flow
- **DI:** Koin
- **Database:** Room (SQLite) for EPG cache + playlist metadata
- **Networking:** Ktor Client (OkHttp engine)
- **Build:** Gradle (Kotlin DSL)
- **Testing:** JUnit 5, MockK, Compose Testing, Espresso, UI Automator

---

## High-Level Phases

| Phase | Name | Focus | Est. Effort |
|-------|------|-------|-------------|
| 0 | Project Setup | Skeleton, CI, tooling, device deployment | 1 day |
| 1 | Core Domain | M3U/Xtream parser, XMLTV parser, domain models | 3 days |
| 2 | Data Layer | Repositories, Room database, Ktor networking, caching | 3 days |
| 3 | Video Player | Media3 integration, preview, fullscreen, PiP | 3 days |
| 4 | EPG Grid UI | Synced channel/time grid, DPad navigation, focus management | 4 days |
| 5 | Playlist Management | Add/edit/remove playlists, Xtream auth, favorites | 3 days |
| 6 | Polish & Features | Settings, search, EPG auto-refresh, error states | 3 days |
| 7 | Testing & Hardening | Integration tests, DPad testing, performance profiling | 3 days |

**Total:** ~23 development days

---

## Phase 0: Project Setup

### Goals
- Android project scaffold with Compose for TV
- Koin DI setup
- Logging, crash reporting structure
- CI pipeline (GitHub Actions)
- Device deployment scripts
- Dev server for mock playlist/EPG data

### Detailed Tasks

#### 0.1 — Project Scaffold
```
app/
├── src/main/
│   ├── java/com/iptvplayer/
│   │   ├── IptvPlayerApp.kt          # Application class, Koin init
│   │   ├── di/                        # Koin modules
│   │   │   ├── AppModule.kt
│   │   │   ├── DataModule.kt
│   │   │   └── DomainModule.kt
│   │   ├── core/                      # Shared utilities
│   │   │   ├── Constants.kt
│   │   │   ├── DispatcherProvider.kt
│   │   │   └── Result.kt              # Sealed class: Success/Error/Loading
│   │   ├── domain/                    # Business logic
│   │   │   ├── model/                 # Pure Kotlin data classes
│   │   │   │   ├── Playlist.kt
│   │   │   │   ├── Channel.kt
│   │   │   │   ├── Programme.kt
│   │   │   │   ├── EpgChannel.kt
│   │   │   │   ├── EpgProgramme.kt
│   │   │   │   └── PlaybackState.kt
│   │   │   ├── repository/            # Interfaces
│   │   │   │   ├── PlaylistRepository.kt
│   │   │   │   ├── EpgRepository.kt
│   │   │   │   └── PlaybackRepository.kt
│   │   │   └── usecase/              # Use cases
│   │   │       ├── LoadPlaylistUseCase.kt
│   │   │       ├── FetchEpgUseCase.kt
│   │   │       └── PlayChannelUseCase.kt
│   │   ├── data/                      # Data layer implementations
│   │   │   ├── repository/
│   │   │   │   ├── PlaylistRepositoryImpl.kt
│   │   │   │   ├── EpgRepositoryImpl.kt
│   │   │   │   └── PlaybackRepositoryImpl.kt
│   │   │   ├── local/                 # Room
│   │   │   │   ├── database/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   └── Daos.kt
│   │   │   │   └── entities/
│   │   │   │       ├── PlaylistEntity.kt
│   │   │   │       ├── ChannelEntity.kt
│   │   │   │       └── EpgProgrammeEntity.kt
│   │   │   ├── remote/                # Ktor client
│   │   │   │   ├── KtorClient.kt
│   │   │   │   ├── PlaylistApi.kt
│   │   │   │   └── EpgApi.kt
│   │   │   └── parser/
│   │   │       ├── M3uParser.kt
│   │   │       ├── XtreamParser.kt
│   │   │       └── XmlTvParser.kt
│   │   ├── presentation/             # Compose UI
│   │   │   ├── navigation/
│   │   │   │   └── AppNavigation.kt
│   │   │   ├── theme/
│   │   │   │   └── Theme.kt
│   │   │   ├── components/
│   │   │   │   ├── VideoPreview.kt
│   │   │   │   ├── EpgGrid.kt
│   │   │   │   ├── ChannelRow.kt
│   │   │   │   ├── TimeHeader.kt
│   │   │   │   ├── ProgrammeCell.kt
│   │   │   │   └── PlaylistSelector.kt
│   │   │   ├── screens/
│   │   │   │   ├── home/HomeScreen.kt
│   │   │   │   ├── epg/EpgScreen.kt
│   │   │   │   ├── fullscreen/FullscreenPlayerScreen.kt
│   │   │   │   └── settings/SettingsScreen.kt
│   │   │   └── viewmodel/
│   │   │       ├── EpgViewModel.kt
│   │   │       ├── PlayerViewModel.kt
│   │   │       └── PlaylistViewModel.kt
│   │   └── MainActivity.kt
│   ├── res/
│   │   ├── values/strings.xml
│   │   └── xml/network_security_config.xml
│   └── AndroidManifest.xml
├── src/androidTest/                   # Instrumented tests
├── src/test/                          # Unit tests
├── build.gradle.kts
└── proguard-rules.pro

dev-server/                            # Mock data server for testing
└── src/
    └── main/
        ├── server.js                  # Express server with mock M3U/XMLTV
        ├── data/
        │   ├── sample.m3u
        │   └── sample.xml
        └── package.json

scripts/                               # Deployment & dev scripts
├── deploy.sh                          # Build + push + install to Android TV
├── run-dev-server.sh                  # Start mock data server
├── adb-connect.sh                     # ADB over WiFi connect
└── screen-capture.sh                  # Pull screenshots for testing
```

#### 0.2 — Build Configuration
**`build.gradle.kts` (project-level):**
```kotlin
plugins {
    id("com.android.application") version "8.3.0"
    id("org.jetbrains.kotlin.android") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

android {
    namespace = "com.iptvplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.iptvplayer"
        minSdk = 26  // Android 8.0 — covers 99% Android TV devices
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Compose for TV
    val tvCompose = "1.4.0"
    implementation("androidx.tv:tv-material:$tvCompose")
    implementation("androidx.tv:tv-foundation:$tvCompose")

    // Core Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")

    // Media3 (ExoPlayer)
    val media3 = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$media3")
    implementation("androidx.media3:media3-ui:$media3")
    implementation("androidx.media3:media3-session:$media3")
    implementation("androidx.media3:media3-exoplayer-hls:$media3")
    implementation("androidx.media3:media3-exoplayer-dash:$media3")

    // Ktor
    val ktor = "2.3.11"
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-okhttp:$ktor")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
    implementation("io.ktor:ktor-client-logging:$ktor")

    // Room
    val room = "2.6.1"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    // Koin
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

#### 0.3 — AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <uses-feature
        android:name="android.software.leanback"
        android:required="true" />
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:name=".IptvPlayerApp"
        android:allowBackup="true"
        android:banner="@mipmap/ic_launcher"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:networkSecurityConfig="@xml/network_security_config"
        android:theme="@style/Theme.IptvPlayer"
        android:usesCleartextTraffic="true"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:exported="true"
            android:launchMode="singleTask"
            android:resizeableActivity="true"
            android:screenOrientation="landscape">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

#### 0.4 — GitHub Actions CI
**`.github/workflows/ci.yml`:**
```yaml
name: CI
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v3
      - name: Lint
        run: ./gradlew lint
      - name: Unit Tests
        run: ./gradlew testDebugUnitTest
      - name: Build APK
        run: ./gradlew assembleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
```

#### 0.5 — Dev Scripts
See `scripts/` section below.

#### 0.6 — Mock Dev Server
Express server serving sample M3U and XMLTV files for local testing without real IPTV provider.

---

## Phase 1: Core Domain (TDD-First)

### Goals
- Pure Kotlin domain models (no Android deps)
- M3U parser (local file + URL)
- Xtream Codes API parser
- XMLTV/EPG parser
- All parsers test-driven

### Domain Models

```kotlin
data class Playlist(
    val id: String,                     // UUID
    val name: String,
    val type: PlaylistType,             // M3U_URL, M3U_FILE, XTREAM
    val url: String? = null,            // For M3U_URL / Xtream
    val username: String? = null,       // For Xtream
    val password: String? = null,       // For Xtream
    val serverUrl: String? = null,      // For Xtream
    val channels: List<Channel>,
    val addedAt: Instant,
    val lastUpdated: Instant,
)

enum class PlaylistType { M3U_URL, M3U_FILE, XTREAM }

data class Channel(
    val id: String,                     // Composite: playlistId + channelNumber or streamId
    val playlistId: String,
    val number: String,                 // tvg-chno
    val name: String,                   // display name
    val logo: String?,                  // tvg-logo URL
    val group: String?,                 // group-title
    val tvgId: String?,                 // tvg-id for EPG matching
    val streamUrl: String,              // actual stream URL
    val catchupDays: Int = 0,           // catchup source if available
    val catchupSource: String? = null,  // catchup URL template
    val epgChannel: EpgChannel? = null, // Linked after EPG fetch
)

data class EpgChannel(
    val id: String,                     // XMLTV channel id
    val displayName: String,
    val iconUrl: String?,
    val programmes: List<EpgProgramme>,
)

data class EpgProgramme(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String?,
    val category: String?,
    val startAt: Instant,
    val endAt: Instant,
    val iconUrl: String?,
    val season: String?,
    val episode: String?,
    val new: Boolean = false,
    val premiere: Boolean = false,
)
```

### TDD Plan — Parser Tests

#### Test 1: M3U Parser — Basic Channel
```kotlin
@Test
fun `parse basic M3U line extracts channel info`() {
    val input = """
        #EXTINF:-1 tvg-id="BBC1.uk" tvg-chno="1" tvg-name="BBC One" tvg-logo="https://..." group-title="UK",BBC One HD
        http://stream.example.com/bbc1.m3u8
    """.trimIndent()

    val channels = M3uParser.parse(input)

    assertEquals(1, channels.size)
    with(channels[0]) {
        assertEquals("BBC One", name)
        assertEquals("1", number)
        assertEquals("BBC1.uk", tvgId)
        assertEquals("UK", group)
        assertEquals("http://stream.example.com/bbc1.m3u8", streamUrl)
    }
}
```

#### Test 2: M3U Parser — Groups
```kotlin
@Test
fun `parse M3U groups channels by group-title`() {
    val input = """
        #EXTINF:-1 group-title="Sports",Sky Sports 1
        http://...
        #EXTINF:-1 group-title="Sports",Sky Sports 2
        http://...
        #EXTINF:-1 group-title="News",BBC News
        http://...
    """.trimIndent()

    val channels = M3uParser.parse(input)
    val groups = channels.groupBy { it.group }

    assertEquals(2, groups["Sports"]?.size)
    assertEquals(1, groups["News"]?.size)
}
```

#### Test 3: M3U Parser — Catchup
```kotlin
@Test
fun `parse catchup attributes`() {
    val input = """
        #EXTINF:-1 catchup="default" catchup-days="3" catchup-source="https://.../${'{utc}'}",Channel 4
        http://...
    """.trimIndent()

    val channels = M3uParser.parse(input)

    assertEquals(3, channels[0].catchupDays)
    assertEquals("https://.../${'{utc}'}", channels[0].catchupSource)
}
```

#### Test 4: Xtream Parser — Authentication
```kotlin
@Test
fun `xtream authenticate returns success`() = runTest {
    val api = XtreamApi("http://example.com", "user", "pass")
    val auth = api.authenticate()

    assertEquals(true, auth.success)
    assertEquals("Premium", auth.subscriptionType)
}
```

#### Test 5: XMLTV Parser — Basic Programme
```kotlin
@Test
fun `parse XMLTV channel and programme`() {
    val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <tv generator-info-name="test">
          <channel id="BBC1.uk">
            <display-name lang="en">BBC One</display-name>
            <icon src="https://..."/>
          </channel>
          <programme start="20240101120000 +0000" stop="20240101123000 +0000" channel="BBC1.uk">
            <title lang="en">News at Twelve</title>
            <desc lang="en">The latest news</desc>
            <category lang="en">News</category>
          </programme>
        </tv>
    """.trimIndent()

    val result = XmlTvParser.parse(xml)

    assertEquals(1, result.channels.size)
    assertEquals("BBC One", result.channels[0].displayName)
    assertEquals(1, result.channels[0].programmes.size)
    assertEquals("News at Twelve", result.channels[0].programmes[0].title)
}
```

#### Test 6: EPG Channel Matching
```kotlin
@Test
fun `match EPG channels to playlist channels by tvg-id`() {
    val playlistChannels = listOf(
        Channel(id = "1", tvgId = "BBC1.uk", name = "BBC One", streamUrl = "...")
    )
    val epgChannels = listOf(
        EpgChannel(id = "BBC1.uk", displayName = "BBC One", iconUrl = null, programmes = emptyList())
    )

    val matched = EpgMatcher.match(playlistChannels, epgChannels)

    assertEquals("BBC One", matched[0].epgChannel?.displayName)
}
```

### Implementation Order (TDD)
1. Write test → see fail → implement `M3uParser` → see pass
2. Write test → see fail → implement `M3uParser` group parsing → pass
3. Write test → see fail → implement `M3uParser` catchup → pass
4. Write test → see fail → implement `XtreamApi` + `XtreamParser` → pass
5. Write test → see fail → implement `XmlTvParser` → pass
6. Write test → see fail → implement `EpgMatcher` → pass

### Key Implementation Details

**M3U Parser:**
- State machine: parse line-by-line, track `#EXTINF` attributes, consume next line as URL
- Regex for attribute extraction: `(\w+(?:-\w+)*)="([^"]*)"` for key-value pairs
- Handle edge cases: empty lines, comments, missing attributes, multiline `#EXTINF`

**Xtream Parser:**
- REST API calls:
  - `GET /player_api.php?username=X&password=Y&action=get_live_categories`
  - `GET /player_api.php?username=X&password=Y&action=get_live_streams`
  - `GET /xmltv.php?username=X&password=Y` for EPG
- Response: JSON → map to Channel/EpgChannel
- Handle pagination, rate limiting

**XMLTV Parser:**
- XmlPullParser (Android built-in) for streaming parse (large files: 50MB+)
- Date format: `yyyyMMddHHmmss Z` → `Instant`
- Memory-efficient: don't load entire EPG into memory at once; chunk by date

---

## Phase 2: Data Layer

### Goals
- Room database for persistent storage
- Repository implementations
- Ktor HTTP client for remote fetches
- Caching strategy
- Error handling

### Room Schema

**Tables:**
```kotlin
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,  // enum serialized
    val url: String?,
    val username: String?,
    val password: String?,
    val serverUrl: String?,
    val addedAt: Long,
    val lastUpdated: Long,
)

@Entity(tableName = "channels",
    indices = [Index("playlistId"), Index("group")])
data class ChannelEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val number: String,
    val name: String,
    val logo: String?,
    val group: String?,
    val tvgId: String?,
    val streamUrl: String,
    val catchupDays: Int,
    val catchupSource: String?,
)

@Entity(tableName = "epg_channels",
    indices = [Index("channelId", unique = true)])
data class EpgChannelEntity(
    @PrimaryKey val id: String,
    val channelId: String,  // matches Channel.tvgId
    val displayName: String,
    val iconUrl: String?,
)

@Entity(tableName = "epg_programmes",
    indices = [Index("channelId"), Index("startAt"), Index("endAt")])
data class EpgProgrammeEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val title: String,
    val description: String?,
    val category: String?,
    val startAt: Long,
    val endAt: Long,
    val iconUrl: String?,
)
```

### Repository Interfaces

```kotlin
interface PlaylistRepository {
    suspend fun addPlaylist(playlist: Playlist): Result<Unit>
    suspend fun removePlaylist(id: String): Result<Unit>
    suspend fun getPlaylists(): Flow<List<Playlist>>
    suspend fun refreshPlaylist(id: String): Result<Unit>
    suspend fun getChannels(playlistId: String): Flow<List<Channel>>
}

interface EpgRepository {
    suspend fun fetchAndCacheEpg(playlistIds: List<String>): Result<Unit>
    fun getEpgForChannel(channelId: String, from: Instant, to: Instant): Flow<List<EpgProgramme>>
    fun getNowPlaying(channelId: String): Flow<EpgProgramme?>
    suspend fun clearStaleEpg(before: Instant): Result<Unit>
}

interface PlaybackRepository {
    fun createPlayer(): Player  // Returns Media3 ExoPlayer
    suspend fun prepareChannel(channel: Channel): Result<Unit>
    fun getPlaybackState(): Flow<PlaybackState>
}
```

### Caching Strategy

| Data | Source | Cache TTL | Strategy |
|------|--------|-----------|----------|
| Playlists | Room | Persistent until user deletes | Cache-first |
| Channels | Room | Refresh on user action / 24h | Stale-while-revalidate |
| EPG Programmes | Room | 24h (varies by provider) | Fetch once per day, incrementally update |
| Channel Logos | Coil (image cache) | 7 days | Disk cache 50MB |
| Stream URLs | In-memory | Duration of session | No persistence (may expire) |

### EPG Fetch Strategy
1. On playlist load → fetch XMLTV for all channels
2. Parse in chunks (by day or by 100 channels)
3. Insert into Room in transactions (batch 500)
4. Background refresh every 6 hours via WorkManager
5. Incremental update: only fetch new programmes (startAt > max existing)

### TDD Plan

```kotlin
// PlaylistRepository test
@Test
fun `add playlist saves to database and emits in flow`() = runTest {
    val repo = PlaylistRepositoryImpl(mockDatabase, mockApi)

    repo.addPlaylist(testPlaylist)

    val playlists = repo.getPlaylists().first()
    assertEquals(1, playlists.size)
    assertEquals("Test Playlist", playlists[0].name)
}

// EPG Repository test
@Test
fun `fetch EPG caches to database and returns programmes`() = runTest {
    val repo = EpgRepositoryImpl(mockDatabase, mockApi, mockParser)
    mockApi.enqueueSampleXmlTv()

    repo.fetchAndCacheEpg(listOf("playlist-1"))

    val programmes = repo.getEpgForChannel("BBC1.uk", now, now.plusHours(4)).first()
    assertEquals(8, programmes.size) // 2 per hour for 4 hours
}
```

### Ktor Client Setup
```kotlin
fun createKtorClient(): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        level = LogLevel.BODY
    }
    engine {
        config {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
        }
    }
}
```

---

## Phase 3: Video Player

### Goals
- Media3 ExoPlayer integration
- Preview player in EPG screen header
- Fullscreen player screen
- Playback controls (DPad-operable)
- Error handling (stream unavailable, buffer, retry)
- State management (play/pause, loading, error)

### Architecture

```
PlayerViewModel
    ├── Player: ExoPlayer (Media3)
    ├── currentChannel: StateFlow<Channel?>
    ├── playbackState: StateFlow<PlaybackState>
    ├── isFullscreen: StateFlow<Boolean>
    └── error: StateFlow<String?>
```

### Player States
```kotlin
sealed class PlaybackState {
    object Idle : PlaybackState()
    object Loading : PlaybackState()
    object Buffering : PlaybackState()
    data class Playing(val position: Long, val duration: Long) : PlaybackState()
    data class Error(val message: String, val recoverable: Boolean) : PlaybackState()
}
```

### Preview Player

In EPG screen header:
```kotlin
@Composable
fun VideoPreview(
    player: ExoPlayer,
    modifier: Modifier = Modifier,
    currentChannel: Channel?,
    onClick: () -> Unit,
) {
    AndroidView(
        factory = { ctx ->
            StyledPlayerView(ctx).apply {
                this.player = player
                useController = false      // No controls in preview
                setShowBuffering(StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = modifier.clickable(onClick = onClick),
        update = { view ->
            view.player = player
        }
    )

    // Channel overlay (name, logo, "now playing" title)
    if (currentChannel != null) {
        ChannelOverlay(currentChannel)
    }
}
```

### Fullscreen Player
```kotlin
@Composable
fun FullscreenPlayerScreen(
    player: ExoPlayer,
    channel: Channel,
    onBack: () -> Unit,
) {
    AndroidView(
        factory = { ctx ->
            StyledPlayerView(ctx).apply {
                this.player = player
                useController = true       // Full controls
                controllerShowTimeoutMs = 4000
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}
```

### Stream Type Detection
```kotlin
fun buildMediaItem(channel: Channel): MediaItem {
    val uri = Uri.parse(channel.streamUrl)
    val mimeType = when {
        uri.toString().endsWith(".m3u8") -> MimeTypes.APPLICATION_M3U8
        uri.toString().endsWith(".mpd") -> MimeTypes.APPLICATION_MPD
        else -> null // Let ExoPlayer sniff
    }
    return MediaItem.Builder()
        .setUri(uri)
        .setMimeType(mimeType)
        .build()
}
```

### TDD Plan

```kotlin
// PlayerViewModel test
@Test
fun `selecting channel prepares player and emits loading state`() = runTest {
    val viewModel = PlayerViewModel(mockRepository)
    val channel = Channel(name = "BBC One", streamUrl = "http://...")

    viewModel.selectChannel(channel)

    assertEquals(PlaybackState.Loading, viewModel.playbackState.value)
    assertEquals(channel, viewModel.currentChannel.value)
}

@Test
fun `player error emits error state with message`() = runTest {
    val viewModel = PlayerViewModel(mockRepository)
    mockRepository.simulatePlaybackError("Connection refused")

    viewModel.selectChannel(testChannel)

    val state = viewModel.playbackState.value as PlaybackState.Error
    assertEquals("Connection refused", state.message)
    assertEquals(true, state.recoverable)
}
```

### Key Implementation Details

- **Single ExoPlayer instance:** Reuse across preview and fullscreen. Don't create/destroy on screen changes.
- **Surface handling:** Preview uses a small `PlayerView`, fullscreen uses full-screen. Same player, different surface.
- **Buffer management:** `DefaultLoadControl` tuned for live TV (lower buffer = faster channel switching).
- **Channel switching:** `player.setMediaItem()` + `player.prepare()` + `player.play()`. Expect 1-3s delay.
- **Error recovery:** Auto-retry once on failure. Show "Stream unavailable" after 2 attempts.
- **Live vs VOD:** Set `MediaItem.liveConfiguration` for live streams.

---

## Phase 4: EPG Grid UI

### Goals
- Synced channel × time grid
- DPad navigation with focus management
- Current-time indicator
- Now-playing badge
- Smooth scrolling
- Group filter
- Preview video header

### Layout

```
┌─────────────────────────────────────────────────────────────┐
│  [Video Preview - 30% height]                               │
│                                                             │
│  [Now: "BBC One - News at Twelve"]              [Fullscreen]│
├─────────────────────────────────────────────────────────────┤
│  [◀ Groups: All ▼]  [🔍 Search]  [⚙ Settings]              │
├────────┬────────────────────────────────────────────────────┤
│        │  18:00    18:30    19:00    19:30    20:00    20:30│ ← Time header
│        │  │────────│────────│────────│────────│────────│    │
│  BBC 1 │  News     │  The Great British  Bake Off          │ │
│  One   │           │                                       │ │
│        │───────────│───────────────────────────────────────│ │
│  BBC 2 │  Antiques │  Strictly Come                        │ │
│        │  Roadshow │  Dancing                               │ │
│        │───────────│───────────────────────────────────────│ │
│  ITV   │  Coronation      │  Emmerdale │  News at          │ │
│        │  Street          │            │  Ten              │ │
│        │───────────────────│────────────│──────────────────│ │
│  Ch 4  │  The Simpsons  │  The Simpsons  │ 22             │ │
│        │                 │  (repeat)                        │ │
│        │───────────────────────────────────────────────────│ │
│  ┌─current time line─────────────────────────────────────┐ │ │
└─────────────────────────────────────────────────────────────┘
```

### Component Structure

```kotlin
@Composable
fun EpgGrid(
    channels: List<Channel>,
    programmes: Map<String, List<EpgProgramme>>,  // keyed by channelId
    currentTime: Instant,
    selectedChannel: Channel?,
    timeWindow: Duration = 4.hours,
    onChannelSelected: (Channel) -> Unit,
    onProgrammeSelected: (EpgProgramme) -> Unit,
    onFullscreen: () -> Unit,
) {
    // Two synced scroll states
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Column {
        // Time header (horizontal scroll)
        TimeHeader(
            currentTime = currentTime,
            timeWindow = timeWindow,
            scrollState = horizontalScrollState,
        )

        // Channel rows (vertical scroll)
        LazyColumn(
            state = rememberTvLazyListState(),  // TV-optimized
            modifier = Modifier.weight(1f),
        ) {
            items(channels) { channel ->
                ChannelRow(
                    channel = channel,
                    programmes = programmes[channel.id] ?: emptyList(),
                    currentTime = currentTime,
                    timeWindow = timeWindow,
                    horizontalScrollState = horizontalScrollState,
                    isSelected = channel.id == selectedChannel?.id,
                    onSelected = { onChannelSelected(channel) },
                )
            }
        }
    }
}
```

### DPad Navigation

Compose for TV handles this via focus system:
```kotlin
@Composable
fun ProgrammeCell(
    programme: EpgProgramme,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    TvMaterialSurface(
        onClick = onSelected,
        focused = isSelected,
        modifier = modifier
            .tvFocusable(focusRequester = focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    // Highlight programme
                }
            },
    ) {
        Text(programme.title)
    }
}
```

### Time Header
- Fixed-width time slots (30 min intervals)
- Pixel width per minute = configurable (default: 2px/min → 60px per 30min)
- Current-time red vertical line overlay
- Snap to interval on scroll

### Programme Cell
- Width = (endAt - startAt) in minutes × pixelPerMinute
- Overlapping programmes → stacked (rare in IPTV)
- Now-playing badge: "NOW" pill on current programme
- Progress bar on now-playing programme

### TDD Plan (UI Tests)

```kotlin
@Test
fun `epg grid displays channels and programmes`() {
    composeTestRule.setContent {
        EpgGrid(
            channels = testChannels,
            programmes = testProgrammes,
            currentTime = testNow,
            selectedChannel = null,
            onChannelSelected = {},
        )
    }

    composeTestRule.onNodeWithText("BBC One").assertIsDisplayed()
    composeTestRule.onNodeWithText("News at Twelve").assertIsDisplayed()
}

@Test
fun `programme cell width matches duration`() {
    // Programme: 18:00-19:00 (60 min), pixelPerMinute = 2 → 120px
    val cell = composeTestRule.onNodeWithTag("programme-cell-BBC1-1")
    cell.assertWidthIsDp(120.dp)
}

// Instrumented test for DPad
@Test
fun `dpad down navigates from BBC One to BBC Two`() {
    launchApp()
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.pressDPadCenter()  // Focus first channel
    device.pressDPadDown()    // Move to next channel

    device.findObject(By.text("BBC Two")).also {
        assertNotNull(it)
        assertTrue(it.isFocused)
    }
}
```

### Key Implementation Details

- **Synced scroll:** Time header uses same `ScrollState` as programme cells in horizontal axis
- **Visible window optimization:** Only compose programme cells within visible time window
- **Initial position:** Scroll to current time on load
- **Channel logo:** 40×40dp, loaded via Coil
- **Empty states:** "No EPG data" with skeleton loading
- **Performance:** Lazy loading, avoid recomposition of off-screen cells

---

## Phase 5: Playlist Management

### Goals
- Add playlist screen (M3U URL, local file, Xtream)
- Playlist selector (switch between loaded playlists)
- Edit/delete playlists
- Favorites
- Auto-refresh on boot/network change

### Add Playlist Screen

```kotlin
@Composable
fun AddPlaylistScreen(
    onAddM3uUrl: (String, String) -> Unit,   // name, url
    onAddXtream: (String, String, String, String) -> Unit,  // name, server, user, pass
    onBack: () -> Unit,
) {
    var selectedType by remember { mutableStateOf(PlaylistType.M3U_URL) }

    Column {
        // Type selector tabs
        Row {
            TvMaterialTab(selected = selectedType == M3U_URL) { Text("M3U URL") }
            TvMaterialTab(selected = selectedType == XTREAM) { Text("Xtream") }
        }

        when (selectedType) {
            M3U_URL -> M3uUrlForm(onSubmit = onAddM3uUrl)
            XTREAM -> XtreamForm(onSubmit = onAddXtream)
        }
    }
}
```

### Favorites
```kotlin
@Entity(tableName = "favorites")
data class FavoriteChannelEntity(
    @PrimaryKey val channelId: String,
    val addedAt: Long,
)

interface FavoritesRepository {
    suspend fun toggleFavorite(channelId: String): Result<Unit>
    fun isFavorite(channelId: String): Flow<Boolean>
    fun getFavorites(): Flow<List<String>>
}
```

### Auto-Refresh
```kotlin
class PlaylistRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = PlaylistRepository::class.java
            .getKoin().get<PlaylistRepository>()

        repo.getPlaylists().first().forEach { playlist ->
            if (shouldRefresh(playlist)) {
                repo.refreshPlaylist(playlist.id)
            }
        }
        return Result.success()
    }
}
```

### TDD Plan

```kotlin
@Test
fun `add M3U playlist fetches channels and saves to database`() = runTest {
    val viewModel = PlaylistViewModel(mockRepository)
    mockRepository.enqueueMockM3uResponse()

    viewModel.addPlaylist("My Playlist", PlaylistType.M3U_URL, "http://example.com/playlist.m3u")

    val playlists = viewModel.playlists.first()
    assertEquals(1, playlists.size)
    assertEquals(150, playlists[0].channels.size)
}

@Test
fun `xtream authentication failure shows error`() = runTest {
    val viewModel = PlaylistViewModel(mockRepository)
    mockRepository.enqueueAuthError()

    viewModel.addPlaylist("Bad Xtream", PlaylistType.XTREAM, ...)

    assertEquals("Authentication failed", viewModel.error.value)
}
```

---

## Phase 6: Polish & Features

### 6.1 — Settings Screen
```kotlin
data class AppSettings(
    val epgRefreshInterval: Duration = 6.hours,
    val pixelPerMinute: Int = 2,
    val channelSwitchDelay: Duration = 300.milliseconds,
    val startOnLastChannel: Boolean = true,
    val showChannelNumbers: Boolean = true,
    val bufferSize: BufferSize = BufferSize.MEDIUM,
)
```

### 6.2 — Search
- Search channels by name, number, group
- Debounced input (300ms)
- Filter results in real-time

### 6.3 — Error States
- Network unavailable → offline banner
- Playlist fetch failed → retry button
- EPG fetch failed → show channels without EPG
- Stream error → "Stream unavailable, retrying..." → fallback message

### 6.4 — Loading States
- Skeleton EPG grid while loading
- Spinner on playlist fetch
- Shimmer effect on channel logos

### 6.5 — EPG Auto-Refresh
- WorkManager: periodic refresh every 6 hours
- Only fetch programmes not yet cached
- Silent background refresh (no UI interruption)

### 6.6 — Quick Channel Switch
- Number pad input: user presses "1" "5" "0" → jump to channel 150
- DPad left/right: previous/next channel in group
- DPad up/down: previous/next channel in list

### TDD Plan

```kotlin
@Test
fun `search filters channels by name`() = runTest {
    val viewModel = EpgViewModel(channels = listOf(bbc, sky, channel4))

    viewModel.searchQuery.value = "bbc"

    val results = viewModel.filteredChannels.first()
    assertEquals(1, results.size)
    assertEquals("BBC One", results[0].name)
}

@Test
fun `number input switches to matching channel`() {
    val viewModel = PlayerViewModel(channels = testChannels)

    viewModel.onNumberInput("1")
    viewModel.onNumberInput("5")
    viewModel.onNumberInput("0")  // Channel 150

    assertEquals(150, viewModel.currentChannel.value?.number)
}
```

---

## Phase 7: Testing & Hardening

### Test Pyramid

```
           ┌─────────┐
          │   E2E     │  ← 5 tests (UI Automator on real device)
         │───────────│
        │ Integration │  ← 20 tests (ViewModel + Repository)
       │─────────────│
      │    Unit       │  ← 80 tests (Parsers, UseCases, Domain)
     └───────────────┘
```

### Unit Tests (Phase 1-2, JUnit 5 + MockK)
- **M3U Parser:** 8 tests (basic, groups, catchup, edge cases, empty, malformed)
- **XMLTV Parser:** 6 tests (basic, multiple channels, date parsing, missing fields)
- **Xtream Parser:** 4 tests (auth, live streams, categories, EPG)
- **EPG Matcher:** 3 tests (by tvg-id, by name, no match)
- **Repositories:** 10 tests (CRUD, flow emissions, error handling)
- **Use Cases:** 8 tests (happy path, error, edge cases)
- **ViewModels:** 12 tests (state transitions, side effects)

### Compose UI Tests (Phase 4, Compose Testing)
- EPG grid renders channels and programmes
- Programme cell widths are correct
- Focus moves correctly between cells
- Time header scrolls with programme cells
- Current-time line is positioned correctly
- Clicking channel selects it
- Empty state shows when no EPG data

### Instrumented Tests (Phase 7, UI Automator)
**Requires real Android TV device or emulator.**

```kotlin
@RunWith(AndroidJUnit4::class)
class EpgNavigationTest {

    @Test
    fun `full EPG navigation flow`() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // App launches to EPG screen
        device.wait(Until.hasObject(By.text("BBC One")), 5000)

        // DPad down navigates channels
        device.pressDPadDown()
        device.wait(Until.hasObject(By.focused(true)), 500)
        assertTrue(device.findObject(By.focused(true)).text.contains("BBC"))

        // DPad right moves through time
        device.pressDPadRight()
        device.pressDPadRight()

        // Center selects channel
        device.pressDPadCenter()

        // Preview video loads (check for PlayerView)
        device.wait(Until.hasObject(By.res("video-preview")), 5000)
    }

    @Test
    fun `fullscreen player opens and plays`() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Navigate to fullscreen button
        navigateTo(By.text("Fullscreen"))
        device.pressDPadCenter()

        // Fullscreen player appears
        device.wait(Until.hasObject(By.res("fullscreen-player")), 3000)

        // Video is playing (check controls visible)
        device.wait(Until.hasObject(By.text("00:00")), 5000)
    }
}
```

### Performance Targets
| Metric | Target |
|--------|--------|
| Cold start | < 2 seconds |
| EPG grid render (500 channels) | < 500ms |
| Channel switch time | < 3 seconds |
| Memory usage | < 200 MB |
| APK size (release) | < 30 MB |
| Scroll frame rate | 60 fps |

### Profiling
- **Android Studio Profiler:** Memory leaks, CPU spikes, frame drops
- **Layout Inspector:** Recomposition counts in Compose
- **Systrace:** EPG grid scroll performance
- **LeakCanary:** Memory leak detection (debug builds)

---

## Scripts

### `scripts/adb-connect.sh`
```bash
#!/usr/bin/env bash
# Connect to Android TV device over WiFi
# Usage: ./scripts/adb-connect.sh [IP_ADDRESS]
# If no IP provided, attempts to discover on local network

set -euo pipefail

TV_IP="${1:-}"

# Auto-discover if no IP provided
if [ -z "$TV_IP" ]; then
    echo "🔍 Scanning for Android TV devices on local network..."

    # Try common Android TV ports via mDNS/ARP
    SUBNET=$(ipconfig getifaddr en0 2>/dev/null || ip route get 1 | awk '{print $7}' | cut -d. -f1-3)
    if [ -z "$SUBNET" ]; then
        echo "❌ Could not determine local subnet. Please provide IP: $0 192.168.1.100"
        exit 1
    fi

    echo "   Subnet: ${SUBNET}.0/24"
    echo "   Probing ${SUBNET}.100-${SUBNET}.120..."

    for i in $(seq 100 120); do
        IP="${SUBNET}.${i}"
        if nc -z -w1 "$IP" 5555 2>/dev/null; then
            TV_IP="$IP"
            echo "✅ Found Android TV at ${TV_IP}:5555"
            break
        fi
    done

    if [ -z "$TV_IP" ]; then
        echo "❌ No Android TV found. Enable Developer Options on your TV and enable ADB debugging."
        echo "   Then provide IP manually: $0 192.168.1.XX"
        exit 1
    fi
fi

# Check if already connected
CONNECTED=$(adb devices | grep "$TV_IP:5555" | wc -l)
if [ "$CONNECTED" -gt 0 ]; then
    echo "✅ Already connected to ${TV_IP}:5555"
else
    echo "📺 Connecting to ${TV_IP}:5555..."
    adb connect "$TV_IP:5555" || {
        echo "❌ Connection failed. Check:"
        echo "   1. Developer Options enabled on TV"
        echo "   2. 'USB debugging' and 'Network debugging' enabled"
        echo "   3. Device IP: ${TV_IP}"
        echo "   4. ADB port 5555 not blocked by firewall"
        exit 1
    }
fi

adb devices
echo "✅ Connected. Ready for deployment."
```

### `scripts/deploy.sh`
```bash
#!/usr/bin/env bash
# Build debug APK and deploy to connected Android TV device
# Usage: ./scripts/deploy.sh [--release]

set -euo pipefail

cd "$(dirname "$0")/.."

BUILD_TYPE="debug"
if [[ "${1:-}" == "--release" ]]; then
    BUILD_TYPE="release"
fi

echo "🔨 Building ${BUILD_TYPE} APK..."
./gradlew "assemble${BUILD_TYPE^}" --quiet

APK_PATH=$(find app/build/outputs/apk/${BUILD_TYPE} -name "*.apk" -type f | head -1)
echo "📦 APK: ${APK_PATH}"

# Check device connection
DEVICE=$(adb devices | grep -E "device$" | head -1 | awk '{print $1}')
if [ -z "$DEVICE" ]; then
    echo "❌ No Android TV device connected. Run: ./scripts/adb-connect.sh"
    exit 1
fi

echo "📺 Deploying to ${DEVICE}..."
adb -s "$DEVICE" install -r "$APK_PATH"

# Launch app
echo "🚀 Launching app..."
adb -s "$DEVICE" shell am start -n com.iptvplayer/.MainActivity

echo "✅ Deployed and launched."
```

### `scripts/deploy-and-log.sh`
```bash
#!/usr/bin/env bash
# Deploy + stream logcat filtered to app
# Usage: ./scripts/deploy-and-log.sh

set -euo pipefail

cd "$(dirname "$0")/.."

./scripts/deploy.sh

echo ""
echo "📋 Streaming logs (Ctrl+C to stop)..."
echo "─────────────────────────────────────"

adb logcat -c  # Clear buffer
adb logcat | grep -E "com.iptvplayer|ExoPlayer|IptvPlayer"
```

### `scripts/screen-capture.sh`
```bash
#!/usr/bin/env bash
# Capture screenshot from Android TV
# Usage: ./scripts/screen-capture.sh [output_path]

set -euo pipefail

OUTPUT="${1:-screenshot-$(date +%Y%m%d-%H%M%S).png}"

echo "📸 Capturing screenshot..."
adb exec-out screencap -p > "$OUTPUT"
echo "✅ Saved to ${OUTPUT}"
open "$OUTPUT" 2>/dev/null || echo "   Open manually: ${OUTPUT}"
```

### `scripts/run-dev-server.sh`
```bash
#!/usr/bin/env bash
# Start mock IPTV dev server
# Usage: ./scripts/run-dev-server.sh [port]

set -euo pipefail

cd "$(dirname "$0")/../dev-server"

PORT="${1:-8080}"

if ! command -v node &>/dev/null; then
    echo "❌ Node.js required. Install: brew install node"
    exit 1
fi

if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

echo "🌐 Starting mock IPTV server on port ${PORT}..."
echo ""
echo "   M3U Playlist:  http://localhost:${PORT}/playlist.m3u"
echo "   Xtream API:    http://localhost:${PORT}/player_api.php"
echo "   XMLTV EPG:     http://localhost:${PORT}/epg.xml"
echo "   Channel Logos: http://localhost:${PORT}/logos/"
echo ""
echo "   Test: curl http://localhost:${PORT}/playlist.m3u | head -20"
echo ""

node src/server.js --port "$PORT"
```

### Dev Server (`dev-server/src/server.js`)
```javascript
const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 8080;

// Serve static files (logos, etc.)
app.use('/logos', express.static(path.join(__dirname, 'data/logos')));

// M3U Playlist endpoint
app.get('/playlist.m3u', (req, res) => {
    const m3u = fs.readFileSync(path.join(__dirname, 'data/sample.m3u'), 'utf8');
    res.set('Content-Type', 'audio/x-mpegurl');
    res.send(m3u);
});

// Xtream Codes API
app.get('/player_api.php', (req, res) => {
    const { username, password, action } = req.query;

    if (!username || !password) {
        return res.json({ user_info: { auth: false, message: 'Missing credentials' } });
    }

    if (username !== 'demo' || password !== 'demo') {
        return res.json({ user_info: { auth: false, message: 'Invalid credentials' } });
    }

    switch (action) {
        case 'get_live_categories':
            res.json([
                { category_id: '1', category_name: 'Entertainment' },
                { category_id: '2', category_name: 'Sports' },
                { category_id: '3', category_name: 'News' },
                { category_id: '4', category_name: 'Movies' },
            ]);
            break;

        case 'get_live_streams':
            const categoryId = req.query.category_id;
            const streams = JSON.parse(
                fs.readFileSync(path.join(__dirname, 'data/xtream-streams.json'), 'utf8')
            );
            res.json(
                categoryId
                    ? streams.filter(s => s.category_id === categoryId)
                    : streams
            );
            break;

        default:
            res.json({
                user_info: {
                    auth: true,
                    username: 'demo',
                    status: 'Active',
                    exp_date: '1893456000',
                    is_trial: '0',
                    max_connections: '1',
                },
                server_info: {
                    url: `http://localhost:${PORT}`,
                    port: PORT,
                    https_port: '',
                    server_protocol: 'http',
                    rtmp_port: '',
                },
            });
    }
});

// XMLTV EPG endpoint
app.get('/epg.xml', (req, res) => {
    res.set('Content-Type', 'text/xml');
    res.sendFile(path.join(__dirname, 'data/sample.xml'));
});

app.listen(PORT, () => {
    console.log(`Mock IPTV server running on http://localhost:${PORT}`);
});
```

### Dev Server Sample Data (`dev-server/data/sample.m3u`)
```
#EXTM3U
#EXTINF:-1 tvg-id="BBC1.uk" tvg-chno="1" tvg-name="BBC One" tvg-logo="http://localhost:8080/logos/bbc1.png" group-title="Entertainment",BBC One HD
http://test-streams.mux.dev/x30x/variant.m3u8
#EXTINF:-1 tvg-id="BBC2.uk" tvg-chno="2" tvg-name="BBC Two" tvg-logo="http://localhost:8080/logos/bbc2.png" group-title="Entertainment",BBC Two HD
http://test-streams.mux.dev/x30x/variant.m3u8
#EXTINF:-1 tvg-id="ITV1.uk" tvg-chno="3" tvg-name="ITV" tvg-logo="http://localhost:8080/logos/itv.png" group-title="Entertainment",ITV HD
https://devstreaming-cdn.apple.com/sample-code/streams/master.m3u8
#EXTINF:-1 tvg-id="CH4.uk" tvg-chno="4" tvg-name="Channel 4" tvg-logo="http://localhost:8080/logos/ch4.png" group-title="Entertainment",Channel 4 HD
https://devstreaming-cdn.apple.com/sample-code/streams/master.m3u8
#EXTINF:-1 tvg-id="SKYNEWS.uk" tvg-chno="501" tvg-name="Sky News" tvg-logo="http://localhost:8080/logos/skynews.png" group-title="News",Sky News
http://test-streams.mux.dev/x30x/variant.m3u8
#EXTINF:-1 tvg-id="CNN.us" tvg-chno="502" tvg-name="CNN" tvg-logo="http://localhost:8080/logos/cnn.png" group-title="News",CNN International
https://devstreaming-cdn.apple.com/sample-code/streams/master.m3u8
#EXTINF:-1 tvg-id="SKYSPORTS.uk" tvg-chno="601" tvg-name="Sky Sports Main Event" tvg-logo="http://localhost:8080/logos/skysports.png" group-title="Sports",Sky Sports Main Event HD
http://test-streams.mux.dev/x30x/variant.m3u8
```

### Dev Server Sample EPG (`dev-server/data/sample.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tv source-info-name="Mock EPG">
  <channel id="BBC1.uk">
    <display-name lang="en">BBC One</display-name>
    <icon src="http://localhost:8080/logos/bbc1.png"/>
  </channel>
  <channel id="BBC2.uk">
    <display-name lang="en">BBC Two</display-name>
    <icon src="http://localhost:8080/logos/bbc2.png"/>
  </channel>
  <channel id="ITV1.uk">
    <display-name lang="en">ITV</display-name>
    <icon src="http://localhost:8080/logos/itv.png"/>
  </channel>
  <channel id="CH4.uk">
    <display-name lang="en">Channel 4</display-name>
    <icon src="http://localhost:8080/logos/ch4.png"/>
  </channel>
  <channel id="SKYNEWS.uk">
    <display-name lang="en">Sky News</display-name>
    <icon src="http://localhost:8080/logos/skynews.png"/>
  </channel>
  <channel id="CNN.us">
    <display-name lang="en">CNN</display-name>
    <icon src="http://localhost:8080/logos/cnn.png"/>
  </channel>
  <channel id="SKYSPORTS.uk">
    <display-name lang="en">Sky Sports Main Event</display-name>
    <icon src="http://localhost:8080/logos/skysports.png"/>
  </channel>
</tv>
```

---

## Architecture Summary

```
┌─────────────────────────────────────────────────┐
│              Presentation (Compose for TV)       │
│                                                  │
│  ┌───────────┐  ┌──────────┐  ┌───────────────┐ │
│  │ EpgScreen │  │HomeScreen│  │FullscreenPlayer│ │
│  │           │  │          │  │    Screen      │ │
│  └─────┬─────┘  └────┬─────┘  └──────┬────────┘ │
│        │             │               │            │
│  ┌─────┴─────┐  ┌────┴─────┐  ┌──────┴────────┐ │
│  │EpgViewModel│ │PlaylistVM│  │PlayerViewModel│ │
│  └─────┬─────┘  └────┬─────┘  └──────┬────────┘ │
└────────┼─────────────┼───────────────┼───────────┘
         │             │               │
┌────────┼─────────────┼───────────────┼───────────┐
│        ▼             ▼               ▼           │
│              Domain (Use Cases)                  │
│                                                  │
│  ┌──────────────┐ ┌───────────┐ ┌─────────────┐ │
│  │FetchEpgUseCase│ │LoadPlaylist│ │PlayChannel  │ │
│  │              │ │UseCase     │ │UseCase      │ │
│  └──────┬───────┘ └─────┬─────┘ └──────┬──────┘ │
└─────────┼───────────────┼──────────────┼────────┘
          │               │              │
┌─────────┼───────────────┼──────────────┼────────┐
│         ▼               ▼              ▼        │
│              Data (Repositories)                │
│                                                  │
│  ┌──────────────┐ ┌───────────┐ ┌─────────────┐ │
│  │EpgRepositoryImpl│PlaylistRepo│ PlaybackRepo │ │
│  │              │ │Impl       │ │Impl         │ │
│  └──────┬───────┘ └─────┬─────┘ └──────┬──────┘ │
│         │               │              │         │
│  ┌──────┴───────┐ ┌─────┴─────┐ ┌──────┴──────┐ │
│  │ Room + Ktor  │ │ Room+Ktor │ │  Media3     │ │
│  │ Parser       │ │  Parser   │ │  ExoPlayer  │ │
│  └──────────────┘ └───────────┘ └─────────────┘ │
└──────────────────────────────────────────────────┘
```
