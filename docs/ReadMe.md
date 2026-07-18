Offline Privacy Vault 🛡️

A production-grade, completely offline, privacy-first Android password manager and authenticator. Built with modern Android architecture, this app guarantees your sensitive data never leaves your device by strictly omitting internet permissions.

Features

Zero-Internet Guarantee: No cloud, no tracking APIs.

Bring Your Own Cloud (BYOC) Backups: Export your encrypted vault directly to your own Google Drive or local storage via the Android file picker.

System-Wide Autofill & Passkeys: Natively fills passwords and authenticates FIDO2 Passkeys.

Foldable & Tablet Ready: Adaptive two-pane layouts for premium folding devices.

Built-in 2FA (TOTP): Generates time-based authenticator codes offline.

Local Security Auditor: Scans for weak and reused passwords on-device.

Tech Stack

Kotlin & Jetpack Compose

Dagger-Hilt

Room Database

Android Keystore (AES-256-GCM) & PBKDF2

Google Play Billing (IPC / Offline-capable)

Project Setup & Build Instructions

Clone the repository.

Open the project in Android Studio Koala (or newer).

Ensure you have the Android SDK (API 35/36) installed for Android 17 support.

Sync Gradle to download dependencies.

Build and run on a physical device or emulator.

Note: Android Auto Backup is explicitly disabled in this project. Users must migrate devices using the BYOC Export/Import feature.