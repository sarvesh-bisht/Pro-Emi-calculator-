---
name: deploy-and-test
description: Build, install, and run unit + instrumented tests on a connected Android device, then smoke-launch the app and check logcat for crashes. Use after implementing a change (when the user agrees) or when verifying a build. Do not run automatically; always confirm with the user first.
---

# deploy-and-test

End-to-end verification for the ProEmiCalculator Android app on a connected device.

## When to use
- After implementing an OpenSpec change, **only after asking the user** whether to run it.
- When the user explicitly asks to verify a build on a device.
- Never run automatically without confirmation.

## Prerequisites
- `adb` available (SDK platform-tools on PATH).
- Exactly one Android device/emulator connected (`adb devices` shows one device).
- Gradle wrapper present (`./gradlew`).

## Pipeline

Run each step in order. Stop and report on the first failure.

### 1. Pre-flight
```bash
adb devices
```
Confirm exactly one device in "device" state. If none, skip steps 4-6 and report "no device; unit tests only."

### 2. Unit tests (JVM)
```bash
./gradlew testDebugUnitTest
```
On failure: show the failing test name + stack trace, stop.

### 3. Build debug APK
```bash
./gradlew assembleDebug
```
Confirm `app/build/outputs/apk/debug/app-debug.apk` exists.

### 4. Deploy to device
```bash
./gradlew installDebug
```
On failure: try `adb install -r app/build/outputs/apk/debug/app-debug.apk`; report if still failing.

### 5. Instrumented (UI) tests on device
```bash
./gradlew connectedDebugAndroidTest
```
On failure: show failing test + device log snippet, stop.

### 6. Smoke launch + crash check
```bash
adb shell am start -n com.sarworks.proemicalculator/.MainActivity
sleep 3
adb logcat -d | grep -iE "AndroidRuntime|FATAL EXCEPTION|proemicalculator" | tail -20
```
If any "FATAL EXCEPTION" or "AndroidRuntime.*Exception" line appears -> FAIL and show it.
Otherwise -> PASS.

### 7. Report
Summary table:

| Step | Result |
|------|--------|
| Unit tests | ✓/✗ |
| Build | ✓/✗ |
| Deploy | ✓/✗ |
| Instrumented tests | ✓/✗ (or skipped) |
| Smoke launch | ✓/✗ |

Overall: PASS only if all executed steps passed.

## Notes
- Lint is intentionally excluded (run separately if needed).
- If multiple devices are connected, abort and ask the user to disconnect extras (ANDROID_SERIAL override not supported by this skill).
- APK path: `app/build/outputs/apk/debug/app-debug.apk`
- Package: `com.sarworks.proemicalculator`
- Main activity: `com.sarworks.proemicalculator/.MainActivity`
- Always ask the user before running this skill; never auto-invoke.
```