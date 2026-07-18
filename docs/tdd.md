Technical Design Document (TDD)

1. System Architecture

Clean Architecture layered with the MVVM presentation pattern, utilizing Dagger-Hilt for Dependency Injection. Data flows unidirectionally via Kotlin StateFlow.

2. Modules & Layers

UI/Presentation Layer: Jetpack Compose, material3-window-size-class.

Domain Layer: Pure Kotlin Use Cases.

Data Layer: Room DAOs and Repositories.

Security Layer: Android Keystore (Local encryption), PBKDF2 Key Derivation (Backup encryption), TOTP math, and QR Code generation.

Services & Workers: AutofillService, CredentialProviderService (Passkeys), SecurityAuditWorker.

3. The BYOC Backup Pipeline (Data in Transit)

Because we cannot use the hardware-bound Keystore key to encrypt a file meant for another device, the backup pipeline operates differently:

User requests a backup and provides a string password (e.g., "mysecret123").

The Security Layer uses PBKDF2 (Password-Based Key Derivation Function 2) with a random salt to derive an AES-256 key from that password.

The Room database is dumped to a JSON string in memory, encrypted using this derived key, and the salt + IV are prepended to the byte array.

The app fires ACTION_CREATE_DOCUMENT. The Android OS handles the UI for Google Drive/Local Storage.

Our app opens an OutputStream to the returned Uri and writes the encrypted bytes.




Technical Design Document (TDD)

1. System Architecture

Clean Architecture layered with MVVM, heavily utilizing Dagger-Hilt for Dependency Injection. Data flows unidirectionally via Kotlin StateFlow.

2. Modules & Layers

UI/Presentation Layer (presentation/): Jetpack Compose, Material 3 Adaptive Layouts, androidx.window.

Domain Layer (domain/): Pure Kotlin Use Cases and Domain Models.

Data Layer (data/): Room DAOs, Repositories, and CSV Parsing logic.

Security Layer (security/): Android Keystore, AES-256-GCM, TOTP math, CameraX/ZXing (QR).

Services & Workers (services/, workers/): AegisAutofillService, AegisCredentialService (Passkeys), SecurityAuditWorker.

3. Database Schema

A single polymorphic credentials table utilizing a VaultItemType Enum to store Logins, Notes, Wi-Fi, and Passkeys together efficiently.