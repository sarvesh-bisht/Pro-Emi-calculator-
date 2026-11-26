# part-payments Specification

## Purpose

Manages lump-sum prepayments applied to specific months of an amortizing loan, including aggregation, allocation, outstanding-balance validation, invalid-row retention, and user feedback.

## Requirements

### Requirement: Add part-payment rows
The system SHALL allow adding a new part-payment row with an empty month and zero amount.

#### Scenario: New row added
- **WHEN** the user requests a new part payment
- **THEN** a new editable row is created with an empty month and zero amount

### Requirement: Remove part-payment rows
The system SHALL allow removing an existing part-payment row so it no longer affects calculations.

#### Scenario: Row removed
- **WHEN** the user removes a part-payment row
- **THEN** that row is deleted and excluded from all calculations

### Requirement: Multiple part payments in the same month are summed
The system SHALL aggregate multiple part payments targeting the same month by summing their amounts.

#### Scenario: Duplicate month entries
- **WHEN** two or more part payments target the same month
- **THEN** their amounts are summed and applied once to that month

### Requirement: Part payment allocation never exceeds opening balance
The system SHALL reduce part payment first, then extra EMI, when a month's payments exceed the opening balance.

#### Scenario: Overpayment in a month
- **WHEN** principalPaid + extraEmi + partPayment exceeds the opening balance
- **THEN** partPayment is reduced first and extraEmi second so the total equals the opening balance

### Requirement: Final-month part payment contributes zero
The system SHALL apply zero part payment in the final payoff month when the EMI already covers the balance.

#### Scenario: Part payment in payoff month
- **WHEN** a part payment is scheduled in the month where the EMI clears the remaining balance
- **THEN** the applied part payment amount is zero

### Requirement: Outstanding-balance validation
The system SHALL validate a part-payment amount against the outstanding principal immediately before the selected month, accounting for scheduled EMI principal reduction, extra EMI, and earlier part payments, and excluding the selected month's own part payment. Same-month payments SHALL be aggregated before validating the combined amount.

#### Scenario: Amount within outstanding balance
- **WHEN** a part-payment amount is greater than zero and less than or equal to the outstanding balance before its month
- **THEN** the payment is valid and included in calculations

#### Scenario: Amount exceeds outstanding balance
- **WHEN** a part-payment amount exceeds the outstanding balance before its month
- **THEN** the payment is invalid, excluded from calculations, and shows an inline error

#### Scenario: Invalid month
- **WHEN** a part payment has a month less than 1 or greater than totalMonths
- **THEN** it does not affect the amortization schedule

#### Scenario: Non-positive amount
- **WHEN** a part payment has an amount less than or equal to zero
- **THEN** it does not affect the amortization schedule

### Requirement: Invalid part payments are retained
The system SHALL retain invalid part-payment rows in state for correction rather than deleting them.

#### Scenario: Invalid row retained
- **WHEN** a part-payment row becomes invalid
- **THEN** the row remains in the UI and state and can be corrected by the user

### Requirement: Inline validation feedback
The system SHALL display inline validation feedback for invalid part-payment entries.

#### Scenario: Invalid entry feedback
- **WHEN** a part-payment row contains an invalid month or amount
- **THEN** the row shows an inline error message

### Requirement: New rows start empty
The system SHALL create new part-payment rows with an empty month so they are not applied until valid.

#### Scenario: Empty month on creation
- **WHEN** a part-payment row is added
- **THEN** its month field is empty and it is not applied to any calculation

### Requirement: Decimal precision preserved
The system SHALL preserve and display decimal values entered in part-payment amount fields.

#### Scenario: Decimal amount entry
- **WHEN** a user enters a decimal amount such as 5000.50
- **THEN** the field retains and displays the decimal value

### Requirement: Local text state reflects model
The system SHALL keep displayed month and amount text synchronized with the underlying PartPayment model.

#### Scenario: Model change updates text
- **WHEN** the underlying PartPayment month or amount changes
- **THEN** the displayed text updates to match