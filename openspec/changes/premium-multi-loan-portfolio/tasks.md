# Tasks

## 1. Loan profile model and storage

- [x] 1.1 Create `LoanProfile` data class (id, name, lender, inputs, partPayments, createdAt) and JSON serialization helpers, and verify round-trip serialization
- [x] 1.2 Create `LoanProfileStore` (JSON in SharedPreferences) with getAll/save/delete/getById, and verify persistence across restart
- [x] 1.3 Add unit tests for serialization and store CRUD operations

## 2. Prepayment optimizer use case

- [x] 2.1 Create `PrepaymentOptimizerUseCase` that ranks loans by simulated interest saved for a given surplus amount, and verify ranking with test data
- [x] 2.2 Add scenario simulation (extra monthly amount -> new payoff month + interest saved), and verify against manual calculation
- [x] 2.3 Add unit tests for optimizer ranking and edge cases (single loan, zero surplus, fully paid loan)

## 3. Portfolio aggregation

- [x] 3.1 Add portfolio aggregation logic (total EMI, outstanding, interest remaining, aggregate payoff), and verify with test data
- [x] 3.2 Add unit tests for aggregation with multiple loans

## 4. Pro gating

- [x] 4.1 Add `isProUnlocked` flag to state and prefs with placeholder unlock path, and verify flag persists
- [x] 4.2 Add Pro gate UI (lock indicator + unlock prompt) on portfolio screen, and verify free users see lock

## 5. UI and navigation

- [x] 5.1 Add Portfolio button to EmiScreen TopAppBar and portfolio screen route in MainActivity, and verify navigation
- [x] 5.2 Create `PortfolioScreen` listing loans with aggregate metrics and FAB to add, and verify rendering
- [x] 5.3 Wire "add loan" to open EmiScreen in editing mode and persist on save, and verify end-to-end flow
- [x] 5.4 Add loan detail view showing optimizer ranking and scenario simulator, and verify interaction

## 6. Verification

- [x] 6.1 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`, and verify both succeed
- [x] 6.2 Ask user whether to run deploy-and-test skill, and run if approved

## Workflow follow-up

- Archive the change after review requirements are satisfied.
- Verify the archived result.