# Design

## Context

See proposal.md - Why. The relevant code lives in `domain/usecase/CalculateEmiUseCase.kt` (calculation), `presentation/ui/components/PartPaymentsSection.kt` (row UI and local text state), `presentation/viewmodel/EmiViewModel.kt` (intent handling), and `presentation/mvi/EmiContract.kt` (state). There are no domain tests today. Calculation must remain in the domain layer; validation and feedback remain in the presentation layer.

## Goals / Non-Goals

**Goals:**
- Make part-payment allocation deterministic and internally consistent.
- Aggregate same-month payments instead of dropping all but the last.
- Keep Compose text state in sync with the `PartPayment` model and preserve decimals.
- Validate amounts against the outstanding balance immediately before the selected month.
- Retain invalid rows in state for correction while excluding them from calculations.
- Add validation and inline feedback without changing the MVI contract shape more than necessary.
- Establish a unit-test baseline for the calculation engine.

**Non-Goals:**
- Changing the default expanded/collapsed state of the prepayment accordion.
- Theme persistence, dark-mode variants, and schedule export.
- Splash screen and navigation changes.
- Residual-balance snapping or rounding behavior.
- Changing the EMI formula or baseline-savings semantics.

## Decisions

### D1: Overpayment allocation order
Reduce `partPayment` first, then `extraEmi`, clamping both at zero, and recompute `totalPrincipal = principalComp + currentExtra + currentPart` (then clamp to `opening` as a guard). Rationale: a lump-sum prepayment is the user's explicit, one-off intent and should be honored before the recurring extra EMI; the existing code already reduced `currentPart` first in the `currentPart > overpay` branch, so this makes the else branch consistent. Alternative considered: reduce `extraEmi` first — rejected because it would silently discard the recurring payment the user configured while still showing the part payment as applied.

### D2: Same-month aggregation
Replace `associateBy` (last-wins) with `groupBy { month }.mapValues { entries -> entries.sumOf { it.amount } }`. Rationale: two lump sums in the same month are economically additive; dropping one is a silent data loss. Alternative: reject duplicate months in the UI — rejected because it complicates entry and the economic result is the same as summing.

### D3: Decimal preservation
Render `amountText` from `partPayment.amount.toString()` instead of `toLong().toString()`, and parse with `toDoubleOrNull()`. Rationale: `toLong()` truncates fractional rupees. The field already parses doubles; only display was lossy.

### D4: Text-state synchronization
Key local `remember` on `partPayment` (id + month + amount) and reinitialize text when the model changes, while still allowing free typing. Concretely, use `remember(partPayment.id)` for the mutable state but seed it from `partPayment` via a `LaunchedEffect(partPayment.month, partPayment.amount)` that updates the text when the model changes externally (e.g., reset). Alternative considered: fully hoist text into the ViewModel — rejected to avoid expanding the MVI contract and adding per-keystroke intents; the chosen approach keeps keystroke handling local.

### D5: New rows start with empty month
Seed `PartPayment(month = 0, amount = 0.0)` instead of `month = 1`, and treat `month == 0` as "not yet valid" (excluded from calculation and shown as empty). Rationale: a row with only an amount entered should not silently apply at month 1.

### D6: Outstanding-balance validation
Validate a part-payment amount against the outstanding principal immediately before the selected month, not merely the original loan principal. The outstanding balance is computed by replaying the amortization from month 1 through `month - 1` using the same rules as `runSimulation` (regular EMI principal reduction, extra EMI, and earlier part payments). The selected month's own part payment is excluded from this balance. Same-month payments are aggregated before validating the combined amount.

Validity is determined by a single forward pass over payments sorted by month: a payment is valid only if `month` is in `1..totalMonths`, `amount > 0`, and `amount <= outstandingBalanceBefore(month)` computed from all prior valid payments. Invalid payments are retained in state, excluded from calculation, and surfaced with inline feedback. This avoids circular validation: the balance never depends on the payment being validated.

The domain exposes a helper `outstandingBalanceBefore(month, inputs)` that both validation (presentation) and calculation (domain) use, ensuring consistent rules.

### D7: Invalid-row retention
Keep invalid part-payment rows in state so users can correct them; exclude them from calculations. Rationale: deleting input on validation failure loses user intent and is surprising; filtering at the use-case boundary preserves the MVI contract. Alternative considered: removing invalid rows from state — rejected.

### D8: Tolerance-based floating point
Use tolerance-based assertions (1e-6) in tests and comparisons. No residual-balance snapping or rounding is introduced. Rationale: avoids changing payoff semantics and keeps behavior predictable; floating-point residuals remain but do not affect correctness within tolerance.

### D9: Test baseline
Add JUnit tests under `app/src/test` for `CalculateEmiUseCase`, using tolerance-based assertions (1e-6) for floating-point results and exact reconciliation checks. Rationale: no tests exist today; the calculation is pure Kotlin and needs no Android runtime.

## Risks / Trade-offs

- [Floating-point payoff] → Use tolerance-based assertions and a payoff-month helper rather than exact row counts; no snapping is introduced.
- [Outstanding-balance validation complexity] → A single forward pass with a domain helper keeps validation deterministic and avoids circularity; the helper is shared by validation and calculation.
- [Validation upper bound uses pre-month outstanding balance, not per-row available-after-EMI] → The domain clamps the applied amount to the opening balance, so over-count cannot occur; validation rejects amounts exceeding the pre-month balance, which is stricter than the clamp and prevents partial application.
- [LaunchedEffect resync may fight fast typing] → Only resync when the model value differs from the parsed local value, to avoid clobbering in-progress input.
- [Duplicate-month aggregation changes displayed per-row amount] → The schedule cell shows the summed amount; per-row provenance of which user entry contributed is lost (acceptable).
- [Same-month validation aggregates before checking] → Individual row feedback may lag the combined check; UI should show the aggregate error on the affected rows.

## Migration Plan

No data migration required. Existing in-memory state is ephemeral. Deploy as a normal app update; no rollback concerns beyond reinstalling the prior build.