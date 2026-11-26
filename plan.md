# Pro EMI Calculator - UX Fix Plan

## Issues to Fix

1. **Remove Filter Button** (ScheduleTable.kt)
   - Remove "[ Filter ]" text from line 50-54
   - Keep only "AMORTIZATION SCHEDULE" header

2. **Currency Chips Should Add Up** (InputForm.kt)
   - Change chip logic from `UpdateAmount(value)` to `UpdateAmount(currentAmount + value)`
   - This allows cumulative selection: clicking ₹10L then ₹5L = ₹15L

3. **Part Payment & Prepayment Not Functional** 
   - Verify AddPartPaymentRow intent is properly handled (ViewModel looks correct)
   - Issue may be with LazyColumn rendering or state update
   - Part payments section might not be showing in UI or button not clickable

4. **Blank White Screen on App Launch**
   - LazyColumn may not be rendering default values properly
   - Need to ensure calculation happens on init
   - May need to add initial calculation trigger or use `rememberCoroutineScope`

## Implementation Steps

1. Edit ScheduleTable.kt - Remove "[ Filter ]" button
2. Edit InputForm.kt - Update chip onClick to add amounts instead of replace
3. Verify PartPaymentsSection rendering in EmiScreen
4. Debug LazyColumn rendering issue for blank screen
5. Test all changes

## Files to Modify

- ScheduleTable.kt (Line 50-54)
- InputForm.kt (Line 95-100)
- Possibly EmiScreen.kt (LazyColumn rendering)
- Verify ViewModel and PartPaymentsSection
