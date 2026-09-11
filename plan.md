# Money App — Development Plan

## Phase 1: Foundation [DONE ✅]
- [x] Project scaffolding (AGP, Hilt, Room, Retrofit, Compose)
- [x] Dependency injection modules
- [x] API integration with exchangerate.fun
- [x] Room database (3 migrations)

## Phase 2: Core Features [DONE ✅]
- [x] Rate display with cards
- [x] Search/filter
- [x] Favorites system (pin/unpin)
- [x] Interactive chart with gradients

## Phase 3: Advanced Features [DONE ✅]
- [x] Drag-and-drop reorder (sh.calvin.reorderable)
- [x] Tiles (5 variants)
- [x] Complications (4 types)
- [x] 7 update intervals via WorkManager

## Phase 4: Architecture Refactor [DONE ✅]
- [x] USD-base storage with cross-rate calculation
- [x] DB migration v1→v2→v3
- [x] Library upgrades (AGP 9, Kotlin 2.4, Hilt 2.60, Room 2.7)

## Phase 5: i18n [DONE ✅]
- [x] 6 locale files (en, ru, uk, pl, de, fr)
- [x] Gradle locale filters include all shipped locales

## Phase 6: Testing [DONE ✅]
- [x] 17 unit tests (Mapper, Repository, ViewModel, Worker)

## Phase 7: Polish [IN PROGRESS]
- [x] Remove debug logs (`REORDER_DBG` in CurrencyCard, SettingsViewModel)
- [x] Remove temporary `semantics` blocks
- [x] Premium dark/gold finance visual language
- [x] Smooth sparkline polish
- [x] Round-screen settings/config layout pass
- [ ] Verify release APK builds
- [ ] Full lint pass
- [ ] Replace debug signing config before publishing

## Phase 8: Future [COMPLETE]
- [x] Rate alerts (push notifications when rate crosses threshold)
- [x] Multiple base currencies (not just USD)
