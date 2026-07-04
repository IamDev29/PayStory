# Deployment Guide

This document describes the steps required to configure, sign, build, and deploy **PayStory** to the Google Play Store or distribute it as a stand-alone release package.

---

## App Identification Configuration

Before pushing a production release, customize the unique identification parameters inside `/app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.example" // Keep unchanged to maintain class mappings
    defaultConfig {
        applicationId = "com.aistudio.paystory.<random>" // Update to unique company domain
        minSdk = 24
        targetSdk = 36
        versionCode = 1 // Increment by 1 with each consecutive release
        versionName = "1.0.0" // Incremented Semantic Version
    }
}
```

---

## Keystore & Signing Configuration

PayStory's release signing configuration is fully externalized for security. Secrets are injected into the Gradle compiler process via environment variables.

### Required Environment Variables
To compile a signed release build, export the following variables in your pipeline or local terminal:

```bash
export KEYSTORE_PATH="/path/to/your/upload-key.jks"
export STORE_PASSWORD="your_keystore_password_here"
export KEY_ALIAS="upload"
export KEY_PASSWORD="your_alias_key_password_here"
```

The app's `build.gradle.kts` automatically loads these variables at compile-time:

```kotlin
signingConfigs {
    create("release") {
        val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
        storeFile = file(keystorePath)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

---

## Compiling Production Artifacts

### 1. Generating a Release APK
To compile a standalone APK file for ad-hoc sharing or internal testing distributions:
```bash
gradle :app:assembleRelease
```
* **Output Path**: `/app/build/outputs/apk/release/app-release.apk`

### 2. Generating an Android App Bundle (AAB)
To generate the required bundle file for submission to the Google Play Console:
```bash
gradle :app:bundleRelease
```
* **Output Path**: `/app/build/outputs/bundle/release/app-release.aab`

---

## Google Play Console Submission Checklist

### 1. ProGuard Obfuscation
Ensure code optimization is enabled inside `/app/build.gradle.kts` to protect intellectual property and parsing expressions:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

### 2. Permissions Declaration Form
Since PayStory requests high-access SMS and notification listeners, you **must** complete Google's **Permissions Declaration Form** when publishing:
* **Declared Core Functionality**: Financial / Personal Finance tracker.
* **Justification**: The core purpose of the application relies on reading passive transaction notifications and text messages offline to log expenditures.

### 3. Safety & Privacy Policy
Ensure you declare correct statements on Google Play's **Data Safety Form**:
* **Data Collected**: Financial info (optional and strictly local).
* **Data Shared**: None. State that zero transaction/personal data is shared or transmitted off-device.
* **Encryption**: Highlight that all database persistence is saved locally using encrypted/offline sandbox sqlite formats.
