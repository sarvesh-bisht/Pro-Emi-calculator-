# Tasks

## 1. Calculation engine correctness

- [x] 1.1 Fix overpayment allocation in `CalculateEmiUseCase.runSimulation()` so `partPayment` is reduced first, then `extraEmi`, both clamped at zero, and `totalPrincipal` is recomputed; verify no row has `principalPaid + extraEmi + partPayment > openingBalance + 1e-9`.
- [x] 1.2 Replace `associateBy` with same-month aggregation (sum of amounts) in `runSimulation()`; verify a duplicate-month test applies the summed amount.
- [x] 1.3 Add a domain helper `outstandingBalanceBefore(month, inputs)` that replays months `1..month-1` (regular EMI principal, extra EMI, and earlier part payments) and returns the closing balance; verify it matches the schedule's opening balance for that month.
- [x] 1.4 Implement validity determination as a single forward pass over payments sorted by month (valid iff month in `1..totalMonths`, amount > 0, and amount <= `outstandingBalanceBefore(month)` computed from prior valid payments); verify invalid rows are excluded from calculation.
- [x] 1.5 Add JUnit tests for `CalculateEmiUseCase`: standard EMI golden value, invalid inputs return empty result, final-month EMI adjustment, per-row and aggregate reconciliation, duplicate-month aggregation, overpayment allocation, final-month part payment contributing zero, outstanding-balance validation, and invalid-row exclusion; verify `./gradlew test` passes using tolerance-based assertions (1e-6).

## 2. UI state and decimal precision

- [x] 2.1 Replace `partPayment.amount.toLong().toString()` with a decimal-preserving conversion in `PartPaymentRow`; verify a decimal entry such as `5000.50` round-trips through recomposition.
- [x] 2.2 Synchronize local `monthText`/`amountText` state with the underlying `PartPayment` via a `LaunchedEffect` that only resyncs when the model value differs from parsed local input; verify Reset and external updates refresh the fields.
- [x] 2.3 Seed new part-payment rows with `month = 0` instead of `1` in `EmiViewModel.AddPartPaymentRow`; verify a newly added row is not applied until a valid month and amount are entered.

## 3. Validation and feedback

- [x] 3.1 Pass `totalMonths` into `PartPaymentsSection` and validate month in `1..totalMonths`, showing inline error text; verify out-of-range months show feedback and are excluded.
- [x] 3.2 Validate amount is greater than zero and not greater than `outstandingBalanceBefore(month)` (aggregating same-month payments), showing inline error text; verify invalid amounts show feedback and are excluded while the row is retained.
- [x] 3.3 Confirm invalid rows remain in state and UI after validation failure and can be corrected; verify corrected rows become valid and affect calculations.
- [x] 3.4 Confirm the use-case filter still excludes invalid entries as the final gate and add a test asserting invalid rows do not affect the schedule; verify the test passes.

## 4. Integration verification

- [x] 4.1 Run `./gradlew test` and a manual smoke test (add/edit/delete rows, duplicate month, final month, decimal amount, outstanding-balance rejection) and verify schedule reconciliation and totals; verify no regressions in existing screens.

## Workflow follow-up

- Archive the change after review requirements are satisfied.
- Verify the archived result.