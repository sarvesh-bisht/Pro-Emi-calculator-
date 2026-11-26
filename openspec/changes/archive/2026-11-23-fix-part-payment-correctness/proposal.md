# Proposal

## Why

The part-payment feature in the EMI calculator is unreliable: `CalculateEmiUseCase.runSimulation()` allocates overpayments asymmetrically (so `principalPaid + extraEmi + partPayment` can exceed the opening balance), collapses multiple part payments in the same month instead of summing them, and silently drops invalid entries. The UI compounds this by truncating decimal amounts (`toLong()`), letting local text state drift from the `PartPayment` model, and seeding new rows at month 1. Validation currently checks only against the original loan principal rather than the outstanding balance before the selected month. There are no domain tests, so none of these regressions are caught. Fixing this restores trust in amortization output and makes the feature testable.

## What Changes

- Fix overpayment allocation in `CalculateEmiUseCase.runSimulation()` so `principalPaid + extraEmi + partPayment` never exceeds the opening balance, reducing `partPayment` first then `extraEmi`.
- Aggregate multiple part payments sharing a month by summing their amounts (duplicate-month SUM).
- Ensure the final amortization schedule reconciles: per-row component sums, total principal, totals, and payoff month.
- Preserve decimal precision in part-payment amount fields (stop `toLong()` truncation).
- Synchronize local Compose text state with the underlying `PartPayment` model.
- Seed new part-payment rows with an empty month instead of silently defaulting to month 1.
- Validate month within `1..totalMonths` and amount `> 0` and not exceeding the outstanding principal immediately before the selected month (accounting for scheduled EMI principal reduction, extra EMI, and earlier part payments; excluding the current month's payment and aggregating same-month payments before validation); show inline feedback and exclude invalid entries from calculations while retaining the rows for correction (invalid-row RETAIN).
- Use tolerance-based comparisons for floating-point values; no residual-balance snapping or rounding is introduced.
- Add unit tests: standard EMI, duplicate-month aggregation, overpayment allocation, final-month part payments, outstanding-balance validation, and reconciliation between principal components and total principal.

## Capabilities

### New Capabilities

- `emi-calculation`: Standard EMI computation, amortization schedule generation, per-row and aggregate totals, and reconciliation of principal components.
- `part-payments`: Lump-sum prepayments: add/remove rows, month aggregation, allocation into the schedule, outstanding-balance validation, invalid-row retention, and inline UI feedback.

### Modified Capabilities

None.

## Impact

- `app/src/main/java/com/sarworks/proemicalculator/domain/usecase/CalculateEmiUseCase.kt`
- `app/src/main/java/com/sarworks/proemicalculator/domain/model/LoanInputs.kt` (and `PartPayment`)
- `app/src/main/java/com/sarworks/proemicalculator/presentation/ui/components/PartPaymentsSection.kt`
- `app/src/main/java/com/sarworks/proemicalculator/presentation/viewmodel/EmiViewModel.kt`
- `app/src/main/java/com/sarworks/proemicalculator/presentation/mvi/EmiContract.kt` (if validation state is added)
- `app/src/main/java/com/sarworks/proemicalculator/presentation/ui/components/InputForm.kt` (validation context / totalMonths)
- `app/src/test/java/com/sarworks/proemicalculator/` (new tests)

No new dependencies; no public API changes.

## Out of Scope

- Changing the default expanded/collapsed state of the prepayment accordion.
- Theme persistence, dark-mode variants, and schedule export.
- Splash screen and navigation changes.