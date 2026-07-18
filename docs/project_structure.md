com.mslabs.aegis
│
├── 📄 AegisApp.kt                        # Required Application class to initialize Dagger-Hilt
├── 📄 MainActivity.kt                    # The single-activity entry point (hosts Jetpack Compose)
│
├── 📁 billing                            # 💰 Google Play Billing & Monetization
│   ├── 📄 BillingManager.kt              # Handles IPC with Play Store for one-time purchase
│   └── 📄 PremiumPreferences.kt          # Encrypted SharedPreferences to store 'isPremium = true' offline
│
├── 📁 data                               # 🗄️ Data Layer (Room DB, DAOs, and Repositories)
│   ├── 📁 local
│   │   ├── 📄 AppDatabase.kt             # Room Database configuration
│   │   ├── 📁 dao
│   │   │   ├── 📄 CredentialDao.kt       # SQL queries (Insert, Get, Delete)
│   │   │   └── 📄 AuditDao.kt            # SQL queries for saving security audit results
│   │   └── 📁 entity
│   │       ├── 📄 CredentialEntity.kt    # The polymorphic Room table (Logins, Wi-Fi, Passkeys)
│   │       └── 📄 AuditResultEntity.kt   # Stores zxcvbn results and duplicate counts
│   └── 📁 repository
│       └── 📄 VaultRepositoryImpl.kt     # Implements domain interfaces, connects Room to Keystore
│
├── 📁 di                                 # 💉 Dependency Injection (Dagger-Hilt Modules)
│   ├── 📄 AppModule.kt                   # Provides global singletons (e.g., Context)
│   ├── 📄 DatabaseModule.kt              # Provides Room DB instances
│   └── 📄 SecurityModule.kt              # Provides KeystoreManager, BillingManager, etc.
│
├── 📁 domain                             # 🧠 Domain Layer (Business Logic & Use Cases)
│   ├── 📁 model
│   │   ├── 📄 DecryptedVaultItem.kt      # Plain-text data class sent to the UI
│   │   └── 📄 VaultItemType.kt           # Enum (LOGIN, WIFI, SECURE_NOTE, PASSKEY)
│   ├── 📁 repository
│   │   └── 📄 VaultRepository.kt         # Interface defining what the UI can request from data
│   └── 📁 usecase
│       ├── 📄 GenerateTotpUseCase.kt     # Connects TOTP math to the ViewModel countdown
│       ├── 📄 ImportCsvUseCase.kt        # Parses Chrome/1Password CSVs via Storage Access Framework
│       └── 📄 ExportJsonUseCase.kt       # Encrypts the vault to a user-defined password for backup
│
├── 📁 presentation                       # 🎨 UI Layer (Jetpack Compose, ViewModels)
│   ├── 📁 theme
│   │   ├── 📄 Color.kt, Theme.kt, Type.kt # Material 3 Design Tokens (Dark Obsidian/Cyan theme)
│   ├── 📁 navigation
│   │   └── 📄 AppNavGraph.kt             # Compose Navigation routes (Edge-to-Edge predictive back)
│   ├── 📁 components
│   │   ├── 📄 AegisButton.kt             # Reusable premium styled button
│   │   ├── 📄 AegisTextField.kt          # Reusable secure text field
│   │   └── 📄 TotpRing.kt                # The animated 30-second countdown circle
│   └── 📁 screens
│       ├── 📁 vault
│       │   ├── 📄 VaultListScreen.kt     # Adaptive layout (List for phone, Split-pane for Foldables)
│       │   └── 📄 VaultViewModel.kt      # Manages search, list state, and biometrics
│       ├── 📁 detail
│       │   ├── 📄 ItemDetailScreen.kt    # Views/Edits a specific password or note
│       │   └── 📄 DetailViewModel.kt     # Handles encrypting inputs before saving
│       ├── 📁 security
│       │   ├── 📄 SecurityAuditScreen.kt # Shows reused/weak passwords
│       │   └── 📄 SecurityViewModel.kt   
│       └── 📁 premium
│           └── 📄 PremiumScreen.kt       # The $4.99 one-time purchase UI
│
├── 📁 security                           # 🛡️ Cryptography & Local Security
│   ├── 📄 KeystoreManager.kt             # AES-256-GCM hardware encryption/decryption
│   ├── 📄 TotpGenerator.kt               # HMAC-SHA1 math for 6-digit offline codes
│   ├── 📄 PasswordAuditor.kt             # Wraps zxcvbn4j for entropy calculation
│   └── 📄 BiometricHelper.kt             # Triggers Face/Fingerprint prompt
│
├── 📁 services                           # ⚙️ Android OS Integrations
│   ├── 📁 autofill
│   │   ├── 📄 AegisAutofillService.kt    # Pops up over other apps for legacy passwords
│   │   └── 📄 AutofillParser.kt          # Traverses the screen to find username/password fields
│   └── 📁 passkey
│       └── 📄 AegisCredentialService.kt  # Android 14+ Credential Manager for Passkeys
│
└── 📁 workers                            # 🏗️ Background Tasks (WorkManager)
├── 📄 SecurityAuditWorker.kt         # Runs the in-memory duplicate scanner silently
└── 📄 TrashPurgeWorker.kt            # Permanently deletes items in "Recently Deleted" after 30 days
