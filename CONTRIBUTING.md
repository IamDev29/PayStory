# Contributing to PayStory

Thank you for your interest in contributing to **PayStory**! We welcome developers, designers, writers, and bug hunters to help us build a highly secure, private, offline-first transaction story tracking application.

This document guides you through our workflow, development practices, and coding standards.

---

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please report any violations to **conduct@paystory.org**.

---

## Getting Started

### 1. Prerequisites
Ensure you have the following installed on your developer machine:
* **JDK 17** or higher
* **Android Studio Ladybug (2024.2.1)** or higher
* **Gradle 8.5+** (the project uses Gradle Kotlin DSL)
* **Android SDK 36 (compileSdk)**

### 2. Forking and Cloning
1. Fork the repository on GitHub.
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/PayStory.git
   ```
3. Copy `.env.example` to `.env` in the root and configure placeholders:
   ```bash
   cp .env.example .env
   ```

### 3. Running the App
* Open the root folder in **Android Studio**.
* Gradle will sync automatically.
* Run the `app` configuration on an Android Device or Emulator running Android 7.0 (API level 24) or higher.

---

## Project Architecture Overview

PayStory is built as a modular single-module Android App following the **MVVM (Model-View-ViewModel)** and **Clean Architecture** patterns:

```
[UI Layer (Jetpack Compose Screens)]
                │
                ▼
[State Management (ExpenseViewModel)]
                │
                ▼
[Domain / Data Access Layer (ExpenseRepository)]
         /               \
        ▼                 ▼
[Local DB (Room SQLite)]  [Android OS Services (SmsReceiver / NotificationListener)]
```

* **`/app/src/main/java/com/example/services/`**: Houses low-level system integrations like `TransactionNotificationListener` (foreground listener) and `SmsReceiver` (passive broadcast receiver).
* **`/app/src/main/java/com/example/data/`**: Data Models, Room Entity files, DAOs, and the unified repository `ExpenseRepository`.
* **`/app/src/main/java/com/example/ui/screens/`**: Visual interfaces written in Jetpack Compose.
* **`/app/src/main/java/com/example/ui/viewmodel/`**: Houses `ExpenseViewModel`, controlling all business logic, local mapping predictions, and screen states.

---

## Coding Style & Standards

### Kotlin Guidelines
* Follow the [Official Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html).
* Use explicit, expressive variable and function names.
* Leverage Kotlin Coroutines and Flows for all asynchronous database or IO work. Never run network/DB queries on the Main thread.

### Jetpack Compose Layouts
* **Material Design 3**: Do not hardcode Hex color strings in composables. Utilize the current color schemes dynamically (`MaterialTheme.colorScheme`).
* **Modifier Isolation**: Pass a default `modifier: Modifier = Modifier` as the first optional parameter on custom UI component declarations to support sizing constraints.
* **Touch Targets**: All interactive buttons, icon buttons, and list elements **must** maintain a minimum clickable bounds of `48.dp x 48.dp` (Material 3 standard).
* **TestTags**: Always add `testTag` modifiers on interactive nodes (e.g. `Modifier.testTag("submit_button")`) to ensure automated UI testers can target them.

---

## Git Workflow

### Branch Naming Conventions
Always create a descriptive branch for your changes:
* `feature/your-feature-name` (for new features or improvements)
* `bugfix/issue-description` (for bug fixes)
* `docs/updated-docs-section` (for documentation improvements)
* `refactor/clean-up-code` (for restructuring code without changing functionality)

### Commit Message Conventions
We adhere to **Conventional Commits**:
* `feat: add merchant smart mapping dialog`
* `fix: prevent SMS receiver duplication on rapid messages`
* `docs: complete architecture section`
* `style: re-align transaction list padding`
* `test: add tests for expense viewmodel`

### Pull Request Process
1. Run local JVM tests to make sure everything compiles and compiles green:
   ```bash
   gradle :app:testDebugUnitTest
   ```
2. Push your branch to your forked repository.
3. Open a Pull Request (PR) against our `main` branch.
4. Fill out the **Pull Request Template** completely.
5. Wait for automated CI checks (linter, unit tests) to pass.
6. A project maintainer will review your code. Address any comments or revision requests promptly. Once approved, your code will be merged!
