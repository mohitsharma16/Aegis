Product Requirements Document (PRD)

Project Name: Offline Privacy Vault
Target Platform: Android (API 26+), fully optimized for Android 17 and Foldable/Tablet form factors.

1. Objective

To build a highly secure, privacy-first, completely offline password and credential manager for Android. The app will rival the native iOS 18 Passwords app, monetized via a one-time Google Play purchase, and adapts to Android 17 system behaviors.

2. Target Audience

Privacy-conscious users who distrust cloud-based password managers, subscription fatigue sufferers, and users who want ultimate control over their data backups.

3. Scope

In-Scope:

Main Vault (Credentials, Wi-Fi, Secure Notes, and Passkeys).

System-wide Autofill & Android Credential Manager API integration.

Offline TOTP Authenticator (RFC 6238).

Local Security Auditor (zxcvbn entropy & duplicate checking).

Freemium monetization (Google Play Billing One-Time Purchase).

Biometric authentication (Face/Fingerprint).

Foldable Optimization (App continuity and split-pane layouts).

Modern OS Support (Edge-to-edge UI, Predictive Back).

iOS Parity Features:

"Recently Deleted" 30-day trash bin.

Secure QR Code generation for in-person sharing.

Bulk CSV Import.

Bring Your Own Cloud (BYOC) Backups:

User-encrypted JSON exports via Storage Access Framework (SAF) allowing users to save backups to Google Drive, Dropbox, or local storage.

Out-of-Scope:

App-managed cloud syncing (No AWS/Firebase servers).

Phishing/Live Breach detection.

4. User Stories

US1: As a user, I want to store my passwords locally so they cannot be hacked from a cloud server.

US2: As a user, I want to back up my vault to my own Google Drive so I don't lose my data if I break my phone.

US3: As a user, I want to create and sign in with Passkeys using my biometrics.

US4: As a user, I want to generate 6-digit 2FA codes offline.

US5: As a user, I want to import my existing passwords from a CSV file.

US6: As a foldable user, I want the UI to transition into a dual-pane layout when I unfold my device.

5. Acceptance Criteria

App manifest MUST NOT contain android.permission.INTERNET.

App manifest MUST explicitly declare android:allowBackup="false".

Backups exported via SAF MUST be encrypted with a user-provided password using PBKDF2/AES before the OS uploads them.