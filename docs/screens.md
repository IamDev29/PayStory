# Application Screens & User Interfaces

This document describes the design system, structural layout, and interaction specifications of **PayStory's** primary screens, built entirely in Jetpack Compose and following Material Design 3 guidelines.

---

## Screen Navigation Hierarchy

PayStory operates on a **Single-Activity layout**, hosting a persistent bottom bar for authorized tabs:

```
[Launcher / Main]
       │
       ├──► [Auth / Sign-In / Sign-Up Screens] (If unauthenticated)
       │
       ├──► [Onboarding screen Wizard] (If first-time launch)
       │
       └──► [Main Work Space Navigation]
                 ├──► [Home Tab]
                 ├──► [Transactions Tab]
                 ├──► [Budgets Tab]
                 ├──► [Analytics Tab]
                 └──► [Settings Tab]
                           └──► [Smart Mappings Dialog Editor]
```

---

## Core Screens Breakdown

### 1. Authentication Screens (`AuthScreens.kt`)
* **Purpose**: Local authentication checks protecting financial statements from unauthorized device access.
* **Layout**: Clean centralized dark/light cards with text fields for Username and Email. Includes full validators for email formats.
* **Aesthetic**: Utilizes high negative space, custom Material 3 elevated action buttons, and standard system keyboard integrations.

### 2. Onboarding Screen (`OnboardingScreen.kt`)
* **Purpose**: Welcomes new users and sets up database entities.
* **Steps**:
  1. Profile Registration.
  2. Baseline monthly budget limits.
  3. Interactive permission requests (SMS and Notification Listener toggles) with instructions.
* **Design**: Horizontal paginated view with responsive progress indicators.

### 3. Home Screen (`HomeScreen.kt`)
The primary executive dashboard displaying accounts status:
* **Metrics Header**: Visual summary of total balances and current monthly spending.
* **Budget Overview Rings**: Compact visual progress bars showing category limits.
* **Recent Transactions List**: Quick list of the last 5 transactions.
* **Pending PayStory Card (CRITICAL)**:
  * Automatically pops up when a transaction is recorded with `isReviewed = false`.
  * Displays auto-categorized classifications and custom context story suggestions based on local learning.
  * Shows a confidence badge (`HIGH MATCH`, `MEDIUM MATCH`, `LOW MATCH`) using dynamic primary/tertiary colors.
  * Features three main touch targets: **"Skip"** (dismisses transaction review), **"Edit"** (opens category/story override options), and **"Save"** (commits the classification and trains the classifier).

### 4. Transactions Screen (`TransactionsScreen.kt`)
The digital ledger showing all historical payments:
* **Search Header**: Supports real-time text searches (matching payee name, bank name, or story context).
* **Category Filters**: Horizontal scrolling list of category chips (`🍔 Food`, `🛒 Grocery`, `✈️ Travel`, etc.) supporting quick single-select filters.
* **Ledger Groupings**: Groups transactions by calendar dates for easy timeline navigation.
* **Visual Elements**: Expenses are formatted with `-₹Amount` in high-contrast error/red, and income credits are styled with `+₹Amount` in success/green.

### 5. Budgets Screen (`BudgetsScreen.kt`)
* **Purpose**: Budget limit controller.
* **Controls**: Vertical layout of active categories with an edit text field to adjust monthly spending limits.
* **Budget Health Bars**: Features Material 3 `LinearProgressIndicator` bars that shift from primary color (healthy budget) to tertiary (warning) and error colors (exceeded limits) as spending approaches capacity.
* **Alert Logs**: Historical list of system-generated `BudgetAlert` items indicating when a category exceeded 80% or 100% caps.

### 6. Analytics Screen (`AnalyticsScreen.kt`)
* **Purpose**: Visual ratio distribution analyzer.
* **Spend Ratios**: Calculates and highlights percentage contributions of each category to overall monthly expenses.
* **Visual Lists**: Clean metrics listing category spend, transaction counts, and average purchase amounts.

### 7. Settings Screen (`SettingsScreen.kt`)
System controls and custom mappings:
* **Notification Redirect Card**: Redirects to system permission settings to verify the listener status.
* **"PayStory Smart Mappings" Selector (🧠)**:
  * Opens a fully-featured Dialog Manager.
  * Displays a searchable list of learned rules.
  * Allows users to add, edit, or delete custom classification rules (`merchant_mappings` table) so the system can automate future transactions.
* **Battery Whitelist Prompt**: Redirects users to battery optimization settings.
* **Local Log Out Button**: Clears current session references securely.

---

## Styling & Accessibility Rules

To guarantee a modern, production-grade interface:

* **Dynamic Colors**: Leverages dynamic Material Theme schemes with deep neutral bases, spacious container shapes, and prominent high-contrast buttons.
* **Touch Targets**: All interactive controls (buttons, filters, checklist chips, input fields, close buttons) maintain a minimum size of **`48.dp x 48.dp`** to prevent miss-clicks.
* **Labels & Content Descriptions**: All icons and action vectors are declared with meaningful semantic descriptions (`contentDescription`) for accessibility screen readers (TalkBack).
* **TestTags**: Primary UI widgets declare distinct string tags (e.g., `testTag("manage_merchant_mappings_setting")`) to facilitate reliable Robolectric and screenshot automated testing.
