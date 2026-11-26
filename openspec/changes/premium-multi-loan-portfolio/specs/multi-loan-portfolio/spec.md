# Spec Delta

## Purpose

Manages multiple named loans with persistent storage, portfolio-level aggregation of payments and outstanding balances, and prepayment optimization that ranks loans by interest saved.

## ADDED Requirements

### Requirement: Add and persist a loan profile
The system SHALL allow creating a named loan profile with inputs and part payments, and persist it across app restarts.

#### Scenario: New loan saved
- **WHEN** the user creates a loan profile with a name and valid inputs
- **THEN** the profile is stored and appears in the portfolio list after restart

### Requirement: Edit and remove loan profiles
The system SHALL allow editing an existing loan profile's inputs and removing a profile so it no longer affects the portfolio.

#### Scenario: Profile removed
- **WHEN** the user removes a loan profile
- **THEN** it is deleted from storage and excluded from all portfolio calculations

### Requirement: Portfolio aggregates loan metrics
The system SHALL display total monthly EMI, total outstanding principal, total remaining interest, and aggregate payoff month across all stored loans.

#### Scenario: Aggregate totals
- **WHEN** two or more loans are stored
- **THEN** the portfolio shows the sum of their monthly EMIs and outstanding balances

### Requirement: Prepayment optimizer ranks loans
The system SHALL rank stored loans by estimated interest saved for a given surplus amount, using full amortization simulation.

#### Scenario: Ranking by interest saved
- **WHEN** the user enters a surplus amount
- **THEN** loans are ordered by interest saved descending, with the best option highlighted

### Requirement: Scenario simulator
The system SHALL simulate adding a fixed extra monthly amount to a selected loan and show the resulting payoff month and total interest saved.

#### Scenario: Extra EMI simulation
- **WHEN** the user specifies an extra monthly amount for a loan
- **THEN** the system shows the new payoff month and interest saved compared to baseline

### Requirement: Pro gating
The system SHALL restrict portfolio and optimizer features to Pro users, showing a lock indicator for free users.

#### Scenario: Free user sees lock
- **WHEN** a free user opens the portfolio screen
- **THEN** loan data is not accessible and a Pro unlock prompt is shown