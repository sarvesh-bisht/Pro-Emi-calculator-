# Spec Delta

## Purpose

Computes the standard monthly EMI and generates the full amortization schedule for a fixed-rate loan, including per-row and aggregate totals and reconciliation of principal components.

## ADDED Requirements

### Requirement: Standard EMI calculation
The system SHALL compute the monthly EMI from principal, annual interest rate, and term using the standard amortizing formula.

#### Scenario: Valid inputs produce EMI
- **WHEN** principal is greater than zero, annual rate is greater than zero, and term is greater than zero
- **THEN** monthly EMI equals P * r * (1 + r)^n / ((1 + r)^n - 1), where r = annualRate / 12 / 100 and n = term in months

#### Scenario: Zero or negative inputs
- **WHEN** principal, annual rate, or term is less than or equal to zero
- **THEN** the result has zero EMI, zero totals, and an empty schedule

### Requirement: Amortization schedule covers every month until payoff
The system SHALL generate one row per month until the balance reaches zero, capped at 1200 months.

#### Scenario: Schedule length matches payoff
- **WHEN** valid inputs are provided
- **THEN** the schedule contains one row for each month until the outstanding balance reaches zero

### Requirement: Final month EMI adjusts to remaining balance
The system SHALL reduce the EMI component in the final month to the remaining balance plus interest.

#### Scenario: Final month adjustment
- **WHEN** the remaining balance plus interest is less than the base EMI
- **THEN** the EMI component equals the remaining balance plus interest

### Requirement: Per-row principal does not exceed opening balance
The system SHALL ensure that principalPaid + extraEmi + partPayment does not exceed the opening balance in any row.

#### Scenario: Prepayments in a month
- **WHEN** principal, extra EMI, and part payment together exceed the opening balance
- **THEN** the applied amounts are reduced so their sum equals the opening balance

### Requirement: Schedule totals reconcile to principal
The system SHALL ensure the sum of principalPaid + extraEmi + partPayment across all rows equals the original principal within floating-point tolerance.

#### Scenario: Reconciliation
- **WHEN** a valid amortization schedule is generated
- **THEN** the aggregate principal components equal the original principal within floating-point tolerance

### Requirement: Total payment reconciles
The system SHALL ensure totalPayment equals totalPrincipal + totalInterest and each row's totalPayment equals its interest plus its applied principal.

#### Scenario: Aggregate totals
- **WHEN** a valid result is produced
- **THEN** totalPayment equals totalPrincipal + totalInterest

#### Scenario: Per-row totals
- **WHEN** a schedule row is produced
- **THEN** its totalPayment equals its interestPaid plus its applied principal total

### Requirement: Baseline comparison for savings
The system SHALL compute a baseline schedule without prepayments or extra EMI to derive interestSaved and monthsSaved.

#### Scenario: Savings derived from baseline
- **WHEN** prepayments or extra EMI reduce cost
- **THEN** interestSaved equals baseline interest minus actual interest, coerced to at least zero, and monthsSaved equals original months minus actual months, coerced to at least zero

### Requirement: Outstanding balance before a month
The system SHALL compute the outstanding principal immediately before a given month by replaying the amortization through the prior month, including scheduled EMI principal reduction, extra EMI, and earlier part payments.

#### Scenario: Balance before selected month
- **WHEN** the outstanding balance before month M is requested
- **THEN** it equals the closing balance after month M-1, including all prior principal reductions