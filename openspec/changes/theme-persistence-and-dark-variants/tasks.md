# Tasks

## 1. Theme preferences wrapper

- [x] 1.1 Create `data/preferences/ThemePreferences.kt` wrapping SharedPreferences (name `theme_prefs`) with `getTheme()/setTheme()` and `getMode()/setMode()` plus safe fallbacks, and verify it compiles
- [x] 1.2 Add unit-testable parsing helpers (enum from string with default) and verify unknown values fall back to defaults

## 2. Contract and ViewModel

- [x] 2.1 Add `ThemeMode` enum (LIGHT, DARK, SYSTEM) and `themeMode` field to `EmiContract.State` with default SYSTEM, and verify compilation
- [x] 2.2 Convert `EmiViewModel` to `AndroidViewModel`, load saved theme + mode in `init`, and persist on `UpdateTheme`/new `UpdateThemeMode` intent, and verify values round-trip
- [x] 2.3 Add `UpdateThemeMode` intent handling and verify mode changes update state

## 3. Per-theme dark color schemes

- [x] 3.1 Implement `getDarkColorScheme(theme)` for all five built-in themes and use it when `darkTheme` is true, and verify each theme renders a distinct primary in dark mode
- [x] 3.2 Keep shared neutral dark background/surface across themes and verify contrast of onPrimary text

## 4. Theme screen mode selector

- [x] 4.1 Add Light/Dark/System selector (segmented control or cards) to `ThemeScreen.kt` that dispatches `UpdateThemeMode`, and verify selecting a mode updates state and persists
- [x] 4.2 Keep theme cards auto-returning to main while mode selector stays on screen, and verify navigation behavior

## 5. MainActivity wiring

- [x] 5.1 Resolve effective `darkTheme` from `state.themeMode` + `isSystemInDarkTheme()` in `MainActivity` and pass to `ProEmiCalculatorTheme`, and verify light/dark switching
- [x] 5.2 Ensure splash screen path does not crash with the new theme wiring, and verify app launches

## 6. Verification

- [x] 6.1 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`, and verify both succeed
- [ ] 6.2 Manually verify persistence across process death and per-theme dark palettes, and document results

## Workflow follow-up

- Archive the change after review requirements are satisfied.
- Verify the archived result.