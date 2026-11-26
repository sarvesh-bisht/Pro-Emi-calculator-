# Spec Delta

## Purpose

Lets users choose and persist a visual theme and light/dark appearance preference, applying a per-theme color scheme in both light and dark modes across app restarts.

## ADDED Requirements

### Requirement: Theme selection is persisted
The system SHALL save the user's selected theme and restore it on the next app launch.

#### Scenario: Selection survives restart
- **WHEN** the user selects a theme and the app is restarted
- **THEN** the previously selected theme is applied

### Requirement: Theme mode is persisted
The system SHALL save the user's theme mode (Light, Dark, or System) and restore it on the next app launch.

#### Scenario: Mode survives restart
- **WHEN** the user selects a theme mode and the app is restarted
- **THEN** the previously selected mode is applied

### Requirement: Defaults on first launch
The system SHALL default to the Deep Blue theme and System mode when no preference has been saved.

#### Scenario: First launch defaults
- **WHEN** the app is launched with no saved theme preference
- **THEN** the Deep Blue theme is applied in System mode

### Requirement: Dark mode uses per-theme palette
The system SHALL apply a dark color scheme specific to the selected theme when dark mode is active, rather than a single shared dark palette.

#### Scenario: Dark palette matches theme
- **WHEN** dark mode is active for a non-default theme
- **THEN** the applied dark palette's primary color corresponds to that theme

### Requirement: Theme mode overrides system dark state
The system SHALL use Light mode when mode is Light, Dark mode when mode is Dark, and follow the system dark setting when mode is System.

#### Scenario: Light override
- **WHEN** theme mode is Light
- **THEN** the app uses a light color scheme regardless of system setting

#### Scenario: Dark override
- **WHEN** theme mode is Dark
- **THEN** the app uses a dark color scheme regardless of system setting

#### Scenario: System follow
- **WHEN** theme mode is System
- **THEN** the app follows the system dark setting

### Requirement: Invalid stored values fall back to defaults
The system SHALL fall back to the Deep Blue theme and System mode when a stored value is unrecognized.

#### Scenario: Unknown stored theme
- **WHEN** the stored theme value is not a recognized theme
- **THEN** the Deep Blue theme is applied

#### Scenario: Unknown stored mode
- **WHEN** the stored mode value is not a recognized mode
- **THEN** System mode is applied