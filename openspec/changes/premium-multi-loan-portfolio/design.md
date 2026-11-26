# Design

## Context

The app is single-loan: `EmiViewModel` holds one `LoanInputs` in `EmiContract.State`, and `CalculateEmiUseCase` computes one amortization. Storage today is limited to theme prefs. See `proposal.md` for motivation. Multi-loan needs persistent storage and a new screen, but must not disrupt the existing single-loan (free) flow.

## Goals / Non-Goals

**Goals:**
- Persist a list of named loans across app restarts using only Android built-ins (no new dependencies).
- Provide portfolio aggregation (total EMI, outstanding, interest remaining, aggregate payoff).
- Provide a prepayment optimizer that ranks loans by simulated interest saved for a given surplus.
- Gate portfolio behind a Pro flag with a placeholder unlock path (billing deferred).
- Reuse existing `CalculateEmiUseCase` per loan; no changes to EMI math.

**Non-Goals:**
- Google Play Billing integration (follow-up change).
- PDF/CSV export (follow-up change).
- Cloud sync / backup.
- Loan comparison UI (separate concern; can reuse portfolio data later).
- Editing part payments inside the portfolio editor (reuse single-loan screen for detail).

## Decisions

1. **JSON in SharedPreferences over Room.**
   - Rationale: zero new dependencies; realistic portfolio is 1-5 loans; org.json is built into Android.
   - Alternative: Room. Rejected for v1 to keep diff small; migrate if portfolio grows.

2. **`LoanProfile` wraps `LoanInputs` + metadata.**
   - Fields: `id (UUID)`, `name`, `lender`, `inputs: LoanInputs`, `createdAt`.
   - Stored as a JSON array under key `"loans"` in prefs file `"loan_portfolio"`.
   - Serialization via `org.json.JSONObject`/`JSONArray` with manual mapping; `LoanInputs` fields are primitives + enum names.

3. **`LoanProfileStore` as a plain class taking Context.**
   - Mirrors `ThemePreferences` pattern. Methods: `getAll(): List<LoanProfile>`, `save(profile)`, `delete(id)`, `getById(id)`.
   - No Flow; synchronous reads (small data). ViewModel loads on init.

4. **`PrepaymentOptimizerUseCase` reuses `CalculateEmiUseCase`.**
   - For each loan, compute baseline interest (no surplus) and interest with a lump-sum prepayment of `surplus` at the current month (or user-chosen month). Rank by `interestSaved` descending. Tie-break by higher rate.
   - Also compute "months saved" per loan.
   - Returns `List<OptimizationResult(loanId, loanName, interestSaved, monthsSaved, newPayoffMonth)>`.

5. **Pro gating via `isProUnlocked` boolean in `EmiContract.State`.**
   - For v1, a debug toggle in settings or a simple "Unlock Pro" button that sets the flag in prefs (placeholder). Real billing in follow-up.
   - Free users see portfolio teaser with lock icon; tapping shows "Pro feature" message.

6. **Navigation: add "Portfolio" button in `EmiScreen` TopAppBar actions.**
   - New screen route `"portfolio"` in MainActivity's `when` block, mirroring existing `"themes"` route.
   - `PortfolioScreen` lists loans, shows aggregate, FAB to add loan, tap to edit (opens single-loan screen seeded with that loan's data).

7. **Portfolio editing reuses single-loan flow.**
   - "Add loan" opens `EmiScreen` in a "editing loan profile" mode; on Calculate/Save, persist as `LoanProfile`. Avoids duplicating input UI.
   - This means `EmiViewModel` needs an optional `editingLoanId` state to know whether to persist on save.

## Risks / Trade-offs

- [JSON serialization fragility] -> Use explicit field mapping with fallbacks; version the JSON with a `schemaVersion` field for future migration.
- [No Room querying] -> Portfolio aggregation is in-memory over small list; fine for <20 loans.
- [Pro gating without real billing] -> Placeholder unlock only; must not ship to production as "paid" without billing. Clearly mark as dev-only.
- [Reusing single-loan screen for editing] -> State management complexity; `EmiViewModel` gains optional editing-loan-id. Mitigation: keep it optional and default to null (free single-loan mode).

## Migration Plan

No migration needed (new prefs file). Rollback: remove portfolio code; `loan_portfolio` prefs orphaned but harmless.