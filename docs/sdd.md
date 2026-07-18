Security Design Document

1. Threat Model & Zero-Internet Guarantee

Primary threat vector is Physical Device Access. The INTERNET permission is omitted. No third-party tracking APIs.

2. Encryption Strategy (Dual-Key System)

At Rest (Local Device): AES-256-GCM. Master key generated and strictly bound inside the Android Hardware Keystore (TEE/StrongBox).

In Transit (BYOC Backups): AES-256-GCM. Key derived via PBKDF2 using a user-supplied backup password. This ensures the backup file can be decrypted on a different hardware device during migration.

3. Disabling Auto Backup

Standard Android Auto Backup is disabled (android:allowBackup="false"). If the Room database were backed up by the OS, it would be permanently locked on a new device because the hardware Keystore key does not migrate.

4. Foldable & Multi-Window Security

Screen Capture: App enforces FLAG_SECURE.

Multi-Window Leakage: In split-screen, the app will blur plain-text passwords when the lifecycle hits onPause (loses top focus).

5. Memory Management

Clipboard: Utilizes Android 13+ sensitive clipboard flags (EXTRA_IS_SENSITIVE).

R8 Obfuscation: Active on release builds to protect encryption logic.



Security Design Document

1. Threat Model & Zero-Internet Guarantee

The primary threat vector is Physical Device Access. The INTERNET permission is omitted. No third-party tracking APIs or cloud sync SDKs are permitted.

2. Encryption Strategy & Backup

Local Rest: AES-256-GCM bounded to the Android Hardware Keystore (TEE/StrongBox).

The Auto-Backup Trap: Standard Android Auto Backup is disabled (android:allowBackup="false"). Migration is handled only via the manual "Bring Your Own Cloud" Encrypted JSON Export feature utilizing the Storage Access Framework (SAF).

3. Memory & UI Security

Foldable Multi-Window: The app enforces FLAG_SECURE and obscures plain-text passwords when onPause is triggered during split-screen multitasking.

Clipboard: Utilizes Android 13+ sensitive clipboard flags (EXTRA_IS_SENSITIVE).

Memory Sweeps: The background security auditor hashes passwords in volatile memory and immediately flags them for garbage collection.