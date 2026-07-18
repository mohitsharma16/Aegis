Master Implementation Plan: Aegis

This plan builds the app from the "inside out," ensuring data security is bulletproof before any UI is attached.

Phase 1: Core Foundation (Security & Local Data)

Goal: Establish Hilt, hardware encryption, and the Room database.

[x] Step 1.1: Setup App & Gradle configuration (Kotlin DSL, AGP 9.0).

[x] Step 1.2: Configure AndroidManifest.xml (No Internet, Backup disabled).

[x] Step 1.3: Implement KeystoreManager.kt (AES-256-GCM).

[x] Step 1.4: Implement Room Entities, DAOs, and AppDatabase.kt.

[x] Step 1.5: Create di/AppModule.kt and di/DatabaseModule.kt (Hilt Setup).

[x] Step 1.6: Define domain/repository/VaultRepository.kt (Interface).

[x] Step 1.7: Implement data/repository/VaultRepositoryImpl.kt (Connecting Room to Keystore).

Phase 2: Domain Logic & Cryptography

Goal: Add the math and business logic for our premium features.

[x] Step 2.1: Implement security/TotpGenerator.kt (HMAC-SHA1 math).

[x] Step 2.2: Implement domain/usecase/GenerateTotpUseCase.kt.

[x] Step 2.3: Implement domain/model/DecryptedVaultItem.kt.

[x] Step 2.4: Implement security/PasswordAuditor.kt (zxcvbn4j engine).

[x] Step 2.5: Implement domain/usecase/ImportCsvUseCase.kt & ExportJsonUseCase.kt (BYOC Backups via SAF).

Phase 3: Presentation - Core UI (Jetpack Compose)

Goal: Build the foldable-ready, edge-to-edge UI.

[x] Step 3.1: Setup presentation/theme (Color, Theme, Type - Obsidian/Cyan aesthetic).

[x] Step 3.2: Configure presentation/navigation/AppNavGraph.kt.

[ ] Step 3.3: Build presentation/screens/vault/VaultViewModel.kt (Search & StateFlow).

[ ] Step 3.4: Build presentation/screens/vault/VaultListScreen.kt (Adaptive Multi-Pane).

[ ] Step 3.5: Build presentation/screens/detail/DetailViewModel.kt & ItemDetailScreen.kt.

Phase 4: OS Integrations (The "Premium" Magic)

Goal: Natively integrate with Android for a frictionless experience.

[ ] Step 4.1: Implement security/BiometricHelper.kt.

[ ] Step 4.2: Build services/autofill/AutofillParser.kt & AegisAutofillService.kt.

[ ] Step 4.3: Build services/passkey/AegisCredentialService.kt (Android 14+ Credential Manager).

[ ] Step 4.4: Build QR Code Sharing UI (CameraX + ZXing).

Phase 5: Background Workers & Automation

Goal: Silent, in-memory vault maintenance.

[ ] Step 5.1: Implement workers/SecurityAuditWorker.kt (In-memory duplicate scanner).

[ ] Step 5.2: Build presentation/screens/security/SecurityAuditScreen.kt.

[ ] Step 5.3: Implement workers/TrashPurgeWorker.kt (30-day "Recently Deleted" auto-wipe).

Phase 6: Monetization & Launch

Goal: Google Play Billing for the one-time $4.99 unlock.

[ ] Step 6.1: Implement billing/PremiumPreferences.kt (Encrypted local premium state).

[ ] Step 6.2: Implement billing/BillingManager.kt (Play Billing IPC).

[ ] Step 6.3: Build presentation/screens/premium/PremiumScreen.kt.
