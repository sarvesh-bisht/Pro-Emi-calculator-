# Proposal

## Why

The app currently handles a single loan. Users with multiple loans (home, car, personal) have no way to see total monthly outflow, compare which loan to prepay first, or simulate surplus allocation. A multi-loan portfolio with a prepayment optimizer is durable, policy-independent value that justifies a paid "Pro" unlock and drives recurring use.

## What Changes

- Add a `LoanProfile` model (id, name, lender, inputs, part payments) persisted as JSON in SharedPreferences.
- Add a portfolio screen showing all loans, total monthly EMI, total outstanding, total interest remaining, and aggregate payoff date.
- Add a prepayment optimizer that ranks loans by interest saved per rupee for a given surplus amount, using full amortization simulation per loan.
- Add a scenario simulator: "if I add ₹X/month extra, which loan closes first and total interest saved."
- Gate the portfolio/optimizer behind a Pro unlock (Google Play Billing integration deferred to a follow-up; this change adds a `isProUnlocked` flag check with a placeholder unlock path).
- Keep single-loan EMI calculation (free tier) unchanged.

## Capabilities

### New Capabilities

- `multi-loan-portfolio`: Storing, listing, and viewing multiple named loans with aggregate portfolio metrics and prepayment optimization.

### Modified Capabilities

None. (Existing `emi-calculation` and `part-payments` specs are reused per-loan without requirement changes.)

## Impact

- New package: `data/storage/LoanProfileStore.kt` (JSON in SharedPreferences).
- New domain model: `LoanProfile` (id, name, lender, inputs, partPayments).
- New use case: `PrepaymentOptimizerUseCase` (ranks loans by simulated interest saved).
- New UI: `PortfolioScreen.kt`, `LoanProfileEditor.kt` (add/edit loan).
- `EmiViewModel` / navigation: add portfolio screen route; keep existing single-loan flow as free tier.
- Dependency: none new for v1 (JSON via org.json or kotlinx.serialization; prefer org.json built into Android). Google Play Billing added in a follow-up change.
- Tests: unit tests for optimizer ranking and portfolio aggregation.