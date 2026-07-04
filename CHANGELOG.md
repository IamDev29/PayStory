# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-07-04

This is the initial release of **PayStory**, an offline-first, highly private Android application that helps users track, categorize, and capture the real human "story" behind their daily transactions by listening to system-level payment notifications and SMS records in real time.

### Added

#### Core Background Transaction Detection Engine
* **`TransactionNotificationListener` (Foreground Service)**: Actively listens to system-level status bar notifications from UPI and payment apps (PhonePe, GPay, Paytm, CRED, Amazon Pay, etc.). Detects incoming payments and credits, extracts transaction details (sender name, amount), and triggers interactive feedback.
* **`SmsReceiver` (Broadcast Receiver)**: Passive SMS listener that parses bank transactional SMS strings (debited, spent, transferred, etc.), extracting the transaction amount, originating bank/institution name, and reference/UTR ID numbers.
* **Deduplication Safeguards**: Built-in 30-second window checks and reference ID checks inside the database repository to ensure identical transaction records are never duplicated.

#### Smart Classifier & Recommendation System (PayStory V2)
* **Intelligent Mapping Rule Engine**: Core analyzer matching raw merchant titles to standard categories with dynamic confidence markers (`HIGH`, `MEDIUM`, `LOW` match ratings).
* **Story Suggestion Generator**: Generates contextually relevant, human-readable explanations (e.g. "Dining out / Food order", "Weekly grocery shopping", "Refueling vehicle") based on merchant keywords.
* **Local Continuous Learning**: Automatically creates and updates a local database table (`merchant_mappings`) when a user corrects/reviews a transaction, allowing the classifier to auto-pilot future categorizations with `HIGH` confidence.

#### Interactive User Interfaces (Material Design 3 & Jetpack Compose)
* **On-Boarding Flow**: First-launch onboarding wizard to register local account credentials, set default budget values, and request necessary system permission consents.
* **Main Screen Workspace**: Custom Material 3 layout hosting a clean, bottom-navigation menu structure with type-safe routing.
* **Home Screen Dashboard**: Features total balances, recent payment cards, active budget progress rings, and a highlighted **"NEW PAYSTORY" Pending Review Card** featuring confidence tags and one-tap action buttons (Save, Edit, Skip).
* **Modal Review Prompt**: A high-priority `ModalBottomSheet` that pops up dynamically when an unreviewed transaction is captured in the background. Shows suggestions and provides inline editing controls.
* **Transactions Log**: Fully searchable ledger supporting real-time text filters, date groupings, and category-badge filtering.
* **Budget Center**: Allows users to set monthly expense limits per category. Displays visual progress bars, remaining capacities, and tracks historical system-generated **Budget Alerts** (triggered at 80% and 100% thresholds).
* **Financial Analytics**: Displays spending ratios by category, transaction counts, and visual progress charts.
* **Settings & Smart Rule Editor**: Configures notification permissions and features the **"PayStory Smart Mappings" Dialog Manager** — supporting full CRUD operations (add, edit, delete, search) for custom classification rules.

#### Robust Local Data Persistence
* **Room SQLite Engine**: Completely offline, localized database storing `User`, `Transaction`, `Budget`, `BudgetAlert`, and `MerchantMapping` tables securely.

#### Quality Assurance & Verification
* **Robolectric JVM Tests**: High-performance local JVM testing suite checking unit business logic and MVVM states without needing an emulator.
* **Roborazzi Screens**: Configured for pixel-perfect visual regression and visual screenshot testing.
