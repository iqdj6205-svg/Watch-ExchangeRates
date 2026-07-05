# Money — Wear OS Currency Exchange Rate Tracker

Modern Android application for Wear OS smartwatches that displays real-time exchange rates from [ExchangeRate.fun](https://exchangerate.fun/).

## Architecture

The project follows **Clean Architecture** with **MVVM** pattern, separated into three layers:

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  Jetpack Compose (Wear OS) + Hilt   │
│  ViewModels / Screens / Navigation  │
├─────────────────────────────────────┤
│          Domain Layer               │
│  Models / Repository Interfaces     │
│  Business Logic                     │
├─────────────────────────────────────┤
│           Data Layer                │
│  Retrofit (API) / Room (DB) /       │
│  DataStore (Settings) / Repository  │
│  Implementation / Mapper / Workers  │
└─────────────────────────────────────┘
```

## Project Structure

```
com.serhio.money/
├── data/
│   ├── database/          # Room database, DAO, entities
│   ├── network/           # Retrofit API interface, DTOs
│   ├── repository/        # Repository implementation
│   │   └── mapper/        # DTO ↔ Domain mappers
│   └── settings/          # DataStore preferences
├── domain/
│   ├── model/             # Domain models (ExchangeRate)
│   └── repository/        # Repository interfaces
├── presentation/
│   ├── components/        # Reusable Compose components
│   ├── config/            # Tile & Complication config screen
│   ├── graphs/            # Chart screen with time periods
│   ├── navigation/        # Navigation routes (Screen)
│   └── settings/          # Settings screen & ViewModel
├── di/                    # Hilt dependency injection modules
├── tiles/                 # Wear OS Tile services (5 variants)
├── complications/         # Wear OS Complication service
├── utils/                 # Network monitor, Worker utilities
└── worker/                # WorkManager periodic worker
```

## Tech Stack

| Library | Purpose |
|---------|---------|
| **Kotlin 2.1** | Language |
| **Jetpack Compose for Wear OS** | UI (Material 3) |
| **Hilt 2.55** | Dependency Injection |
| **Room 2.6** | Local database for history |
| **Retrofit 2.11 + OkHttp 4.12** | Network requests |
| **Moshi 1.15** | JSON serialization |
| **WorkManager 2.10** | Background periodic updates |
| **DataStore** | Settings persistence |
| **Timber** | Logging |
| **Horologist** | Wear OS helper libraries |
| **MockK** | Unit testing mocking |

## Features

- Real-time exchange rates for 160+ currencies
- Offline-first caching (Room + DataStore)
- 7 background update intervals (15min – 24h) via WorkManager
- 5 Wear OS Tile variants (Big number, Pair+rate, Change%, Multi-currency, Compact)
- 6 Complication types (SHORT_TEXT, LONG_TEXT, RANGED_VALUE, MONOCHROMATIC_IMAGE, SMALL_IMAGE, ICON)
- Detailed chart screen with 6 time periods (24h, 7d, 30d, 90d, 1y, All)
- Configurable Tiles & Complications display settings
- Network state monitoring with offline warnings
- Dark/Light theme support
- Optimized for round & square Wear OS screens

## Setup

### Prerequisites

- Android Studio Ladybug (2024.3) or newer
- JDK 17
- Wear OS device or emulator (API 30+)

### API Key

The app uses [ExchangeRate.fun](https://exchangerate.fun/) — a **free API that does not require a key** by default. However, the infrastructure for API key management is in place for future use.

To set an API key (optional):

1. Open (or create) `local.properties` in the project root:
```
EXCHANGE_RATE_API_KEY=your_key_here
```

2. Alternatively, set it in `gradle.properties`:
```
EXCHANGE_RATE_API_KEY=your_key_here
```

The key is read from `local.properties` first, then `gradle.properties` as fallback, and is injected via `BuildConfig.API_KEY`. Both files are excluded from version control.

### Build

**Debug:**
```bash
./gradlew assembleDebug
```

**Release:**
```bash
./gradlew assembleRelease
```

### Run Tests

```bash
./gradlew testDebugUnitTest
```

## Security

- **HTTPS only**: Network Security Config enforces TLS for all connections
- **API Key**: Never stored in source code; loaded from local.properties / BuildConfig
- **Interceptors**: OkHttp interceptor adds API key to requests
- **Logging**: Retrofit body logging is enabled only in Debug builds, disabled in Release
- **ProGuard**: Minification enabled in Release with proguard-rules.pro
- **Timeouts**: 30s connect/read/write timeouts with retry-on-failure

## Performance

- All network and DB operations use Kotlin Coroutines
- WorkManager respects Doze mode and battery optimization
- Periodic work constraints: requires network connectivity
- Exponential backoff for retry attempts
- No unnecessary network calls — cached data shown offline
- Sparkline charts render in Compose Canvas (no WebView)

## Testing

| Test | Coverage |
|------|----------|
| ExchangeRateMapperTest | DTO → Domain mapping, Entity conversion |
| CurrencyRepositoryImplTest | API success/failure, offline cache fallback |
| MainViewModelTest | UI state transitions |
| ExchangeRateWorkerTest | Work logic, rate filtering |

## License

This project is open source and available for personal and educational use.
