# Design

## Context

Theme state currently lives in `EmiContract.State.selectedTheme` (an enum of five built-ins) inside `EmiViewModel`, with `ProEmiCalculatorTheme` choosing between a light per-theme scheme and one shared dark scheme. Dark mode already follows the system via `isSystemInDarkTheme()` in the composable default, but the dark palette is theme-agnostic. See `proposal.md` for motivation.

Constraints: minSdk 24, no DI framework, no persistence layer today, ViewModel created via `viewModel()` in `MainActivity`. SharedPreferences is available through core-ktx without new dependencies.

## Goals / Non-Goals

**Goals:**
- Persist selected theme + theme mode across process death and restart.
- Give each built-in theme a distinct, readable dark palette.
- Let the user override system dark mode (Light/Dark/System) from the Theme screen.
- Keep the change localized to presentation + a small data-preferences wrapper; no domain changes.

**Non-Goals:**
- Custom theme creation/editing (deferred follow-up).
- Theme persistence across device backup/restore tuning.
- Dynamic color (Material You) integration.
- Accessibility/contrast enforcement beyond reasonable palette choices.

## Decisions

1. **AndroidViewModel + SharedPreferences over a repository interface.**
   - Rationale: no DI exists; `viewModel()` supports `AndroidViewModel` with an `Application` constructor out of the box, avoiding a custom factory. SharedPreferences reads are fast after first load.
   - Alternative: constructor-injected `ThemePreferences` interface + factory for testability. Rejected for this stage to keep diff small; revisit when ViewModel tests are added. The prefs wrapper itself (`ThemePreferences`) is a plain class taking `Context`, so it can be faked later.

2. **`ThemeMode` enum (LIGHT/DARK/SYSTEM) added to `EmiContract`.**
   - Rationale: mirrors `AppTheme`; keeps state serializable and Compose-friendly. Default SYSTEM preserves current behavior.

3. **Per-theme dark schemes via `getDarkColorScheme(theme)`.**
   - Rationale: replaces the single `DarkColorScheme`. Background/surface stay a shared neutral dark slate for consistency; primary becomes a lighter tint of the light theme's primary so each theme stays recognizable in dark mode. onPrimary is dark text.
   - Alternative: full per-theme dark backgrounds. Rejected to keep visual consistency and reduce palette work; can revisit.

4. **Mode resolution in `MainActivity`, not in the composable.**
   - Rationale: `ProEmiCalculatorTheme` already takes `darkTheme: Boolean`. Computing it in `MainActivity` from `state.themeMode` + `isSystemInDarkTheme()` keeps the theme composable pure and testable. `isSystemInDarkTheme()` is called at the `MainActivity` composition level.

5. **SharedPreferences key/values as strings.**
   - Keys: `selected_theme` (default `"DEEP_BLUE"`), `theme_mode` (default `"SYSTEM"`). Stored as enum names; unknown values fall back to defaults.

6. **Mode selector on Theme screen stays on screen (no auto-navigate).**
   - Rationale: mode is a toggle, not a selection that warrants leaving. Theme cards keep auto-return.

## Risks / Trade-offs

- [SharedPreferences disk read on main thread at first load] → Prefs file is tiny; acceptable. Could move to a coroutine later if StrictMode flags it.
- [AndroidViewModel reduces testability] → Mitigation: prefs logic isolated in `ThemePreferences`; ViewModel tests deferred.
- [Enum-name persistence breaks if enums renamed] → Mitigation: store/restore with safe fallbacks; a future migration can add versioning.
- [Dark palettes may need tuning] → Iterative; values chosen to meet contrast for white/dark text on primary.

## Migration Plan

No data migration needed (new prefs keys with defaults). Rollback: revert code; prefs keys simply become unused.

## Open Questions

None blocking. Palette tuning is iterative and does not change specs/tasks.