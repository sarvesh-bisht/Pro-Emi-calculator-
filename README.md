# Pro EMI Calculator

A modern Android EMI (Equated Monthly Installment) calculator built with Kotlin and Jetpack Compose. Calculate loan EMIs, view full amortization schedules, plan part-payments, and optimize prepayments across multiple loans.

## Features

### Core (Free)
- **EMI Calculation** — Standard amortizing formula with adjustable principal, interest rate, and tenure (years or months)
- **Amortization Schedule** — Month-by-month breakdown with yearly accordion grouping
- **Part Payments** — Add lump-sum prepayments to specific months with outstanding-balance validation
- **Extra EMI** — Simulate the impact of paying extra every month
- **Savings Dashboard** — Interest saved, tenure saved, total interest, total payment
- **Quick Amount Chips** — One-tap loan amount selection (₹10L, ₹25L, ₹50L, ₹1Cr, ₹5Cr)
- **5 Color Themes** — Deep Blue, Emerald, Crimson, Slate, Purple — each with light and dark variants
- **Light / Dark / System** appearance mode
- **Theme Persistence** — Your theme and appearance preference survive app restarts

### Pro (Paid)
- **Multi-Loan Portfolio** — Store and manage multiple named loans
- **Portfolio Dashboard** — Total monthly EMI, outstanding principal, remaining interest, aggregate payoff
- **Prepayment Optimizer** — Ranks loans by interest saved per rupee for a given surplus
- **Scenario Simulator** — See the impact of adding extra monthly EMI to any loan

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVI (Model-View-Intent) over Clean Architecture layers |
| State | StateFlow + Compose `collectAsState` |
| Persistence | SharedPreferences (JSON for loan profiles) |
| Ads | Google AdMob |
| Build | Gradle 9.4.1, AGP 8.7.3 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

## Architecture

The project follows **Clean Architecture** principles with three distinct layers, and uses **MVI** for the presentation layer.

### Layers

```
┌─────────────────────────────────────────────┐
│  Presentation Layer (UI + ViewModel)        │
│  Compose Screens · MVI Contract · ViewModel │
├─────────────────────────────────────────────┤
│  Domain Layer (Use Cases + Models)          │
│  CalculateEmiUseCase · PortfolioAggregator  │
│  PrepaymentOptimizerUseCase · LoanInputs     │
├─────────────────────────────────────────────┤
│  Data Layer (Storage + Preferences)         │
│  LoanProfileStore · ThemePreferences        │
│  ProStatusStore · SharedPreferences          │
└─────────────────────────────────────────────┘
```

- **Domain layer** has zero Android dependencies — pure Kotlin. Use cases encapsulate business logic (EMI formula, amortization, optimization, aggregation). Models are immutable data classes.
- **Data layer** wraps `SharedPreferences` behind store classes (`LoanProfileStore`, `ThemePreferences`, `ProStatusStore`), keeping serialization and persistence details out of domain and UI.
- **Presentation layer** renders state via Jetpack Compose and dispatches user intents to the ViewModel.

### MVI Flow

```
User Action → Intent → ViewModel.handleIntent() → State update (StateFlow) → Compose recomposition
```

- **Contract** (`EmiContract.kt`): defines `State` (immutable data class), `Intent` (sealed class of user actions), and `Effect` (one-shot side effects).
- **ViewModel** (`EmiViewModel`): extends `AndroidViewModel`, holds `_state: MutableStateFlow<State>`, processes intents, calls use cases, persists preferences. Exposes `state: StateFlow<State>` consumed by Compose via `collectAsState()`.
- **Screens** (`EmiScreen`, `PortfolioScreen`, `ThemeScreen`): stateless-ish composables that read `state` and call `viewModel.handleIntent(...)`. No business logic in composables.

### Kotlin & Compose Conventions

- **Immutable state** — `State` is a data class; updates use `copy()` via `StateFlow.update {}`.
- **Coroutines/Flow** — `StateFlow` drives reactive UI; no LiveData.
- **Compose** — Material 3 components, `remember`/`mutableStateOf` for local UI state, `rememberScrollState` for scrolling, `AnimatedVisibility` for expand/collapse.
- **No DI framework** — dependencies are constructed in `ViewModel` (AndroidViewModel gives `Application` context). Stores are plain classes taking `Context`.
- **Testability** — Use cases are pure and unit-tested without Android. Parsing helpers (e.g., `ThemePreferences.parseTheme`) are extracted as companion functions for JVM testing. `org.json` is mocked via a test dependency for unit tests.

## Project Structure

```
app/src/main/java/com/sarworks/proemicalculator/
├── data/
│   ├── preferences/ThemePreferences.kt
│   └── storage/
│       ├── LoanProfileStore.kt
│       └── ProStatusStore.kt
├── domain/
│   ├── model/
│   │   ├── AmortizationResult.kt
│   │   ├── LoanInputs.kt
│   │   └── LoanProfile.kt
│   └── usecase/
│       ├── CalculateEmiUseCase.kt
│       ├── PortfolioAggregator.kt
│       └── PrepaymentOptimizerUseCase.kt
├── presentation/
│   ├── mvi/EmiContract.kt
│   ├── ui/
│   │   ├── EmiScreen.kt
│   │   ├── PortfolioScreen.kt
│   │   ├── ThemeScreen.kt
│   │   ├── AnimatedSplashScreen.kt
│   │   ├── components/
│   │   │   ├── DashboardCards.kt
│   │   │   ├── InputForm.kt
│   │   │   ├── PartPaymentsSection.kt
│   │   │   └── ScheduleTable.kt
│   │   └── theme/Theme.kt
│   └── viewmodel/EmiViewModel.kt
└── MainActivity.kt
```

## Getting Started

### Prerequisites
- Android Studio (Ladybug or newer)
- JDK 11+
- Android SDK (API 24+)

### Build & Run
```bash
# Clone the repository
git clone https://github.com/<your-username>/ProEmiCalculator.git
cd ProEmiCalculator

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (device required)
./gradlew connectedDebugAndroidTest
```

## Testing

- **Unit tests** (`app/src/test/`) — EMI calculation, part-payment validation, optimizer ranking, portfolio aggregation, theme parsing, loan profile serialization.
- **Instrumented tests** (`app/src/androidTest/`) — Compose UI tests covering app launch, navigation, input, chips, part payments, portfolio Pro gate, theme selection.

## OpenSpec (Spec-Driven Development)

This project uses [OpenSpec](https://github.com/Fission-AI/OpenSpec) for spec-driven development. Every feature is planned, specified, and tracked through OpenSpec artifacts before implementation:

- **Specs** (`openspec/specs/`) — durable capability specifications with requirements and scenarios (e.g., `emi-calculation`, `part-payments`, `theme`, `multi-loan-portfolio`).
- **Changes** (`openspec/changes/`) — proposed changes with proposal, design, delta specs, and task checklists.
- **Archived changes** (`openspec/changes/archive/`) — completed and verified changes.

The workflow: explore an idea → propose a change → write specs and design → implement tasks → verify (unit + UI tests) → archive. This ensures every feature has documented requirements, a technical design, and a tracked implementation checklist before any code is written.

## License

This project is provided as-is. Add your preferred license here.