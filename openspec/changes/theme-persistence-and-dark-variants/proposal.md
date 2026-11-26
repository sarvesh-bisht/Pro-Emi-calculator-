# Proposal

## Why

Theme selection currently lives only in `EmiViewModel` in-memory state: it resets to Deep Blue on every process death and app restart, and dark mode uses a single hard-coded `DarkColorScheme` regardless of the selected color scheme. Users who pick a theme lose it immediately, and dark-mode users never see their chosen palette. Persisting the selection and giving each of the five built-in themes a real dark variant (plus a Light/Dark/System mode override) makes theme choice durable and correct.

## What Changes

- Persist the selected theme and theme mode (Light/Dark/System) in SharedPreferences, loaded on startup and saved on change.
- Add a `ThemeMode` preference (Light, Dark, System) defaulting to System, with a selector in the Theme screen.
- Replace the single theme-agnostic `DarkColorScheme` with per-theme dark palettes for all five built-in themes.
- Resolve the effective dark flag in `MainActivity` from `themeMode` + system dark state and pass it to `ProEmiCalculatorTheme`.
- Keep the existing five-theme enum and Theme screen selection flow; add the mode selector alongside it.

This change covers built-in themes only. Custom theme creation is explicitly deferred to a follow-up change.

## Capabilities

### New Capabilities

- `theme`: Durable theme preference and rendering — persisted selected theme, Light/Dark/System mode override, and per-theme light/dark color schemes.

### Modified Capabilities

None. (No existing spec-level behavior changes; `emi-calculation` and `part-payments` are untouched.)

## Impact

- `presentation/mvi/EmiContract.kt` — add `ThemeMode` enum and `themeMode` to `State`.
- `presentation/viewmodel/EmiViewModel.kt` — load/save theme + mode via preferences (AndroidViewModel).
- `presentation/ui/theme/Theme.kt` — per-theme dark color schemes; accept resolved dark flag.
- `presentation/ui/ThemeScreen.kt` — add Light/Dark/System mode selector.
- `MainActivity.kt` — resolve dark flag from mode + system, pass to theme.
- New: `data/preferences/ThemePreferences.kt` (SharedPreferences wrapper).
- New spec: `openspec/specs/theme/spec.md`.
- Tests: extend `CalculateEmiUseCase`-style coverage only where theme logic is pure; ViewModel persistence tested via Robolectric or faked prefs in a later pass.