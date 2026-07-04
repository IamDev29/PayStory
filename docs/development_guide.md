# Development Guide

This guide is designed to help engineers set up, build, test, and debug **PayStory** locally.

---

## Developer Environment Setup

### 1. Requirements
* **Operating System**: macOS, Linux, or Windows 11.
* **JDK**: Java Development Kit 17 (or higher). Ensure your `JAVA_HOME` environment variable is set.
* **Android Studio**: Ladybug (2024.2.1) or Koala (2024.1.1) or higher.

### 2. Standard Workspace Setup
1. Clone the project code and enter the folder:
   ```bash
   git clone https://github.com/YOUR_ORGANIZATION/PayStory.git
   cd PayStory
   ```
2. Set up the local environment settings file:
   ```bash
   cp .env.example .env
   ```
3. Open **Android Studio** and choose **File > Open**, selecting the PayStory root folder.
4. Let Gradle sync and download required dependencies.

---

## Core Development Commands

All compilation, builds, and verification tasks are orchestrated via standard **Gradle** wrapper tasks.

> **CRITICAL**: Always invoke the Gradle task directly using `gradle` (e.g. `gradle :app:assembleDebug`) in this workspace. Do **NOT** use `./gradlew` or `gradlew` commands.

### Build and Compile
* **Full Application Compilation**:
  ```bash
  gradle :app:compileDebugKotlin
  ```
* **Assemble Debug APK**:
  ```bash
  gradle :app:assembleDebug
  ```

---

## Testing Frameworks

PayStory utilizes a robust local test framework to verify logical flows and screen visuals without requiring emulator hardware.

### 1. Local JVM Testing (Robolectric)
We use **Robolectric** to simulate Android runtime environments on the local JVM.
* **Location of Tests**: `/app/src/test/java/`
* **Execute All Unit Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

### 2. Visual Regression Testing (Roborazzi)
We use **Roborazzi** for screenshot testing to capture visual layouts of composables and compare them against pixel-perfect reference baselines.

* **Verify Visual Layouts** (compares against baselines in `/app/src/test/screenshots/`):
  ```bash
  gradle :app:verifyRoborazziDebug
  ```
* **Record New Screenshot Reference Baselines** (run this if you purposely modified layouts or styling):
  ```bash
  gradle :app:recordRoborazziDebug
  ```

---

## Debugging Android OS Services

Debugging system listeners (such as the SMS Receiver or Notification Listener) on virtual devices requires specific mock command inputs.

### 1. Emulating Incoming SMS Messages
You can mock an incoming debit SMS on an active Android Emulator using `adb` or standard telnet.

**Example Command (via telnet on port 5554)**:
1. Connect to emulator: `telnet localhost 5554`
2. Send SMS payload:
   ```text
   sms send AD-HDFCBK "Your HDFC Bank account xx12 has been debited with Rs. 1,500.00 for SWIGGY. Ref: 124589"
   ```
   Our `SmsReceiver` will trigger instantly, parse the bank, extract `₹1500`, and save the record with a pending review status.

### 2. Triggering Mock Notifications
To test the `TransactionNotificationListener`:
1. Use a notification generator tool (or write a quick test activity helper) that posts status notifications with titles like `"Google Pay"` or `"PhonePe"` and text containing `"Received ₹450 from John Doe"`.
2. Ensure you have granted **Notification Access** permissions under **Settings > Apps > Special App Access > Notification Access** inside the emulator first!
