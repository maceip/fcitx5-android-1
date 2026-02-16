# Implementation Plan: English-Only Keyboard with Translate & ML Integration

**Project:** fcitx5-android fork
**Date:** 2026-02-15
**Status:** Active

---

## Overview

Transform fcitx5-android from a multilingual IME platform into a streamlined English-only keyboard with two new features: an embedded translate panel and on-device ML integration (press-secretary pipeline). Work is divided into four phases, each with a verification gate that must pass before proceeding.

---

## Phase 1: Strip Chinese Input Method Dependencies

**Goal:** Remove `libime`, `fcitx5-chinese-addons`, and all Chinese-specific Kotlin code. The app must build, install, and function as an English-only keyboard.

### 1.1 Remove Gradle module dependencies
- **`settings.gradle.kts`**: Remove `include(":lib:libime")` and `include(":lib:fcitx5-chinese-addons")`
- **`app/build.gradle.kts`**:
  - Remove `implementation(project(":lib:libime"))` and `implementation(project(":lib:fcitx5-chinese-addons"))`
  - In `fcitxComponent.includeLibs`: remove `"libime"` and `"fcitx5-chinese-addons"`
  - Remove `excludeFiles` block (references Chinese table input methods)

### 1.2 Update CMakeLists.txt (`app/src/main/cpp/CMakeLists.txt`)
- Remove `find_package(libime ...)` and all `LibIME*` find_package calls
- Remove `find_package(fcitx5-chinese-addons ...)`
- Remove `pinyin-customphrase` static library target
- Remove `LibIME::Pinyin`, `LibIME::Table`, `pinyin-customphrase` from `native-lib` link targets
- Remove `copy-fcitx5-modules` entries for chinese-addons libs: `pinyin`, `table`, `scel2org5`, `chttrans`, `fullwidth`, `pinyinhelper`, `punctuation`
- Remove `install()` commands for chinese-addons-data, pinyinhelper, libime table/data, opencc
- Keep: `fcitx5::clipboard`, `fcitx5::spell`, `fcitx5::quickphrase`, `fcitx5::unicode`, `fcitx5::imselector`, `fcitx5-lua::luaaddonloader`
- Keep: Boost (still needed by fcitx5 core), libuv, spell-dict install

### 1.3 Delete Chinese-specific Kotlin code
- Delete `app/src/main/java/org/fcitx/fcitx5/android/data/pinyin/` (entire directory, 8 files)
- Delete `app/src/main/java/org/fcitx/fcitx5/android/data/table/` (entire directory, 5 files)
- Delete `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/PinyinDictionaryFragment.kt`
- Delete `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/PinyinCustomPhraseFragment.kt`
- Delete `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/TableInputMethodFragment.kt`
- Delete `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/TableFilesSelectionUi.kt`

### 1.4 Update navigation/routes
- **`SettingsRoute.kt`**: Remove `PinyinDict`, `TableInputMethods`, `PinyinCustomPhrase` routes and their `createGraph` entries
- **`PreferenceScreenFactory.kt`**: Remove Chinese-specific preference entries
- **`MainActivity.kt` / `MainViewModel.kt`**: Remove references to deleted fragments/routes

### 1.5 Fix compilation errors
- Grep for all imports referencing deleted packages (`data.pinyin`, `data.table`, `TableManager`, `PinyinDict`, `CustomPhrase`)
- Fix or remove each reference

### 1.6 Remove plugin modules from settings.gradle.kts (optional, low priority)
- Remove all `include(":plugin:*")` lines except `":plugin:clipboard-filter"` if desired
- These don't affect the app build, but reduces IDE indexing time

### Gate 1: Phase 1 Verification
```
[ ] `./gradlew :app:assembleDebug` succeeds with zero errors
[ ] APK installs on device/emulator
[ ] Keyboard activates and English text input works (type "hello world")
[ ] Spell check suggestions appear for misspelled words
[ ] Clipboard history panel opens and functions (copy text, open panel, tap to paste)
[ ] No Chinese-specific settings appear in the settings UI
[ ] No runtime crashes in logcat during 2 minutes of normal use
```

---

## Phase 2: Add Translate Panel

**Goal:** Add a `TranslateWindow` as a new `ExtendedInputWindow` panel accessible from the toolbar, with an input field, language selector, and "send to app" action.

### 2.1 Create TranslateWindow
- New file: `app/src/main/java/org/fcitx/fcitx5/android/input/translate/TranslateWindow.kt`
  - Extends `InputWindow.ExtendedInputWindow<TranslateWindow>()`
  - `onCreateView()`: Returns `TranslateUi` layout
  - `onAttached()`: Focus the input field, set up listeners
  - `onDetached()`: Clean up, cancel pending translations
  - `onCreateBarExtension()`: Language swap button + current language pair indicator
  - `title`: "Translate"

### 2.2 Create TranslateUi
- New file: `app/src/main/java/org/fcitx/fcitx5/android/input/translate/TranslateUi.kt`
  - Built programmatically using Splitties DSL (consistent with existing UI)
  - Layout:
    - Source language chip / dropdown (top-left)
    - Target language chip / dropdown (top-right)
    - Swap button between them
    - Input `EditText` (source text)
    - Output `TextView` (translated text, read-only)
    - "Send" button — commits translated text via `service.commitText()`
  - Theme-aware: uses `theme.barColor`, `theme.keyTextColor`, etc.

### 2.3 Translation backend (initial: stub, then real)
- New file: `app/src/main/java/org/fcitx/fcitx5/android/input/translate/TranslationProvider.kt`
  - Interface: `suspend fun translate(text: String, from: String, to: String): String`
  - Initial implementation: `StubTranslationProvider` that returns `"[translated] $text"` for testing
  - Real implementation deferred to Phase 4 (on-device ML)

### 2.4 Add toolbar button
- **`KawaiiBarComponent.kt`**: Add translate button to `IdleUi.buttonsUi`
  - `translateButton.setOnClickListener { windowManager.attachWindow(TranslateWindow()) }`
- **`IdleUi.kt`** (or `ToolbarUi.kt`): Add translate icon button
- Add translate icon drawable (`ic_translate_24.xml`) to resources

### 2.5 Add string resources
- `R.string.translate` = "Translate"
- `R.string.translate_source_hint` = "Type text to translate"
- `R.string.translate_send` = "Send"

### 2.6 Persist language preference
- Add `translateSourceLang` and `translateTargetLang` to `AppPrefs`

### Gate 2: Phase 2 Verification
```
[ ] Translate button visible in toolbar
[ ] Tapping translate button opens TranslateWindow panel
[ ] Back button returns to keyboard
[ ] Input field accepts text and is focusable within the keyboard view
[ ] Stub provider returns "[translated] <input>" in output field
[ ] "Send" button commits translated text into the active app's text field
[ ] Language selector persists across keyboard open/close cycles
[ ] Panel respects current theme (dark/light)
[ ] No crashes when switching rapidly between translate panel and keyboard
[ ] Translate panel works in both portrait and landscape
```

---

## Phase 3: Enhance Clipboard History

**Goal:** Verify and refine the existing clipboard panel to match the "cards" UX specification. This phase is lightweight since clipboard history is already implemented.

### 3.1 Audit existing clipboard against requirements
- Verify: cards display previous clipboard entries (already 2-column staggered grid)
- Verify: tapping a card commits the text (already `service.commitText(entry.text)`)
- Verify: history persists across keyboard sessions (Room database)

### 3.2 UX refinements (if needed)
- Ensure clipboard button is prominently accessible in toolbar (already in `buttonsUi`)
- Confirm swipe-to-delete + undo works smoothly
- Test with 50+ clipboard entries for performance

### Gate 3: Phase 3 Verification
```
[ ] Copy 10 different texts from various apps
[ ] Open clipboard panel — all 10 appear as cards
[ ] Tap any card — text is committed to active input field
[ ] Swipe to delete — entry removed with undo snackbar
[ ] Pin an entry — it persists after "delete all unpinned"
[ ] Close and reopen keyboard — clipboard history persists
[ ] Clipboard suggestion appears in toolbar bar after copying
```

---

## Phase 4: On-Device ML Integration (press-secretary)

**Goal:** Integrate on-device ML serving for real translation and suggested responses. This phase extracts modules from `maceip/press-secretary`.

### 4.1 Add ML module
- New Gradle module: `:ml` (or integrate press-secretary's `:llm` module)
- Dependencies: LiteRT, LiteRT-LM for Gemma 3 1B inference
- Interface: `LlmProvider` with `suspend fun generate(prompt: String): Flow<String>`

### 4.2 Wire translation to on-device model
- Replace `StubTranslationProvider` with `OnDeviceLlmTranslationProvider`
- Prompt engineering: `"Translate the following from {source} to {target}: {text}"`
- Stream tokens into the output TextView for responsive UX

### 4.3 Add suggested response panel
- New `SuggestedResponseWindow` extending `ExtendedInputWindow`
- Integrates press-secretary's notification capture → context → generation pipeline
- Shows draft response in the keyboard area with send/edit/regenerate actions

### 4.4 Model delivery
- Gemma 3 1B model (~1.4GB) via Play Asset Delivery or sideload
- Model loading on first use with progress indicator
- GPU acceleration via LiteRT GPU delegate where available

### Gate 4: Phase 4 Verification
```
[ ] On-device model loads successfully (first launch)
[ ] Translation produces real output (not stub) in <3 seconds
[ ] Suggested response appears when replying to a notification
[ ] App remains responsive during inference (no UI jank)
[ ] Memory usage stays under 2GB during inference
[ ] Model unloads cleanly when keyboard is dismissed
[ ] Works in airplane mode (fully offline)
```

---

## Execution Order

```
Phase 1 (Strip Chinese) ──> Gate 1 ──> Phase 2 (Translate Panel) ──> Gate 2
                                            │
                                            v
                              Phase 3 (Clipboard Audit) ──> Gate 3
                                            │
                                            v
                              Phase 4 (ML Integration) ──> Gate 4
```

Phases 2 and 3 can run in parallel after Gate 1 passes. Phase 4 depends on Phase 2 (translate panel must exist before wiring real ML backend).

---

## Files Modified (Phase 1 & 2 Summary)

**Modified:**
- `settings.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/cpp/CMakeLists.txt`
- `app/.../ui/main/settings/SettingsRoute.kt`
- `app/.../ui/main/settings/PreferenceScreenFactory.kt`
- `app/.../input/bar/KawaiiBarComponent.kt`
- `app/.../input/bar/ui/IdleUi.kt` (or ToolbarUi)
- Various files with Chinese import references

**Deleted:**
- `app/.../data/pinyin/` (8 files)
- `app/.../data/table/` (5 files)
- `app/.../ui/main/settings/PinyinDictionaryFragment.kt`
- `app/.../ui/main/settings/PinyinCustomPhraseFragment.kt`
- `app/.../ui/main/settings/TableInputMethodFragment.kt`
- `app/.../ui/main/settings/TableFilesSelectionUi.kt`

**Created:**
- `app/.../input/translate/TranslateWindow.kt`
- `app/.../input/translate/TranslateUi.kt`
- `app/.../input/translate/TranslationProvider.kt`
- `res/drawable/ic_translate_24.xml`
