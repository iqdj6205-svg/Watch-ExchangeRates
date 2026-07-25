# Money — Wear OS Currency Exchange Rate App

**Package**: `com.serhio.money`
**API**: `exchangerate.fun` (keyless, HTTPS only)
**Status**: Development Complete ✅ (polish phase)
**Last Updated**: 2026-07-23

## Architecture

### Stack
- **Language**: Kotlin 2.4.0
- **UI**: Jetpack Compose for Wear OS (Material 3, BOM 2025.02.00)
- **Architecture**: MVVM + Clean Architecture (data/domain/presentation)
- **DI**: Hilt 2.60.1 with KSP 2.3.10
- **DB**: Room 2.7.0 (4 schema versions)
- **Network**: Retrofit + Moshi + OkHttp
- **Background**: WorkManager (periodic updates)
- **Preferences**: DataStore
- **Logging**: Timber
- **Build Tools**: AGP 9.2.1, Gradle 9.6.1, KSP

### Data Flow
```
[API: exchangerate.fun] → Retrofit → Repository → Room (cache)
                                          ↓
DataStore (favorites order) → SettingsViewModel
                                          ↓
                     CurrencyRepository → MainViewModel
                                          ↓
                              [Compose UI / Tiles / Complications]
```

**Key Design Decision**: All rates stored relative to USD (`base=USD`, `baseCurrency="USD"` in DB). Cross-rates calculated in `CurrencyRepository` using USD as intermediary. This ensures consistency when switching base currencies.

### Modules
Single `app` module with package-by-layer:
- `com.serhio.money.data.*` — API, DB, Repository, Preferences, Worker
- `com.serhio.money.domain.*` — Models, Repository interface
- `com.serhio.money.presentation.*` — Screens, ViewModels, Components
- `com.serhio.money.tiles.*` — 5 Tile variants
- `com.serhio.money.complications.*` — 4 Complication types

## Implemented Features

### Core
- [x] Real-time exchange rates from exchangerate.fun
- [x] Offline-first caching via Room (with network state detection)
- [x] Cross-rate calculation (any currency → any currency via USD)
- [x] Search by currency code or name
- [x] Favorites system (pin/unpin, drag-and-drop reorder)
- [x] Sparkline charts on all currency cards
- [x] Interactive chart with min/max labels and gradient fill
- [x] Adaptive sampling: 24h→24pts, 7d→28pts, 30d/90d→30pts, 1y→52pts, All→60pts
- [x] Rate alerts (push notifications when rate crosses threshold)

### UI Variants
- [x] 5 Tile variants (ExchangeRate, ExchangeRateMultiFavorites, ExchangeRateMultiAny, ChartRate, ChartRateMulti)
- [x] 4 Complication types (RateBigText, RateSmallText, RateIconRate, RateDirectionIcon)
- [x] 7 update intervals (15m, 30m, 45m, 1h, 2h, 4h, 6h)
- [x] 6 chart periods (24h, 7d, 30d, 90d, 1y, All)

### UI/UX
- [x] Card-based layout with rate direction indicators
- [x] Popup menu (⋯) replaced bottom action bar
- [x] Algorithmic currency flags (from first 2 letters of code)
- [x] German flag special case (DE → special rendering)
- [x] Drag-and-drop reorder on Favorites page (`sh.calvin.reorderable`)
- [x] ScalingLazyColumn on All Currencies page (no reorder)
- [x] Pull-to-refresh
- [x] Network status indicator
- [x] Rate alerts (push notifications when threshold crossed)
- [x] Skeleton loading with shimmer effect
- [x] Staggered card animations

### Architecture Details
- [x] Hilt modules: `AppModule`, `DatabaseModule`, `NetworkModule`, `WorkerModule`
- [x] 7 update intervals configurable via DataStore
- [x] DB migration: v1→v2→v3→v4 with proper data migration
- [x] ProGuard rules for Release builds
- [x] ARM-only build (arm64-v8a, armeabi-v7a — excluding x86/x86_64)

### Testing
- [x] 17 unit tests: Mapper (6) + Repository (4) + ViewModel (4) + Worker (3)
- [x] 17/17 tests passing

## Internationalization
- [x] English (default) — 38 strings
- [x] Russian — 38 strings
- [x] Ukrainian — 38 strings
- [x] Polish — 38 strings
- [x] German — 38 strings
- [x] French — 38 strings

## Remaining Work / Polish Items

### Cleanup
- [ ] Remove `REORDER_DBG` debug logs from `CurrencyCard` and `SettingsViewModel`
- [ ] Remove temporary `semantics` blocks from reorderable items
- [ ] Verify release APK builds (ARM-only config)
- [ ] Run full lint pass

### Known Issues
- `ScalingLazyColumn` does not support `animateItem()` — Favorites use `LazyColumn` (no scaling effect)
- Wear Material3 ColorScheme lacks `surface`/`surfaceVariant`/`onSurfaceVariant` — uses `Color(0xFF2C2C2C)` fallback
- `Card` composable replaced with `Box` + `combinedClickable` for Wear compatibility

## Key Files
| File | Purpose |
|------|---------|
| `app/.../data/repository/CurrencyRepository.kt` | All data logic, cross-rate calc |
| `app/.../data/repository/AlertRepository.kt` | Alert CRUD operations |
| `app/.../data/api/ExchangeRateApi.kt` | Retrofit API interface |
| `app/.../data/local/AppDatabase.kt` | Room DB with migrations |
| `app/.../data/database/AlertDao.kt` | Alert DAO |
| `app/.../presentation/MainActivity.kt` | Entry point, Tile/Complication wiring |
| `app/.../presentation/components/MainScreen.kt` | FavoritesPage + AllCurrenciesPage |
| `app/.../presentation/alerts/AlertsScreen.kt` | Alert management UI |
| `app/.../presentation/settings/SettingsViewModel.kt` | Favorites reorder logic |
| `app/.../domain/model/Currency.kt` | Domain models |
| `app/.../domain/model/Alert.kt` | Alert domain model |
| `app/.../worker/ExchangeRateWorker.kt` | Periodic background sync |
| `app/.../worker/AlertWorker.kt` | Alert threshold checking |
