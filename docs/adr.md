Architecture Decision Records (ADRs)

ADR 1: Zero-Internet Architecture

Decision: Omit INTERNET permission. Rely solely on Play Console Vitals for crash reporting.

ADR 2: Bring Your Own Cloud (BYOC) via SAF

Context: Users will lose data if their phone breaks in a strictly local app, but hosting servers ruins our privacy guarantee and one-time purchase model.

Decision: Implement encrypted file exports using Android's Storage Access Framework (SAF).

Consequence: Users can save their encrypted vault to their own Google Drive, Dropbox, or SD Card. We pay $0 for hosting, and the user gets cloud safety without compromising privacy.

ADR 3: Disabling Android Auto Backup

Context: Hardware Keystore keys do not migrate across devices.

Decision: Set android:allowBackup="false".

ADR 4: Dependency Injection Framework

Decision: Use Dagger-Hilt for compile-time safe injection.

ADR 5: Adaptive UI for Foldables

Decision: Use Material 3's WindowSizeClass and ListDetailPaneScaffold to natively support Foldables and Android 17 Edge-to-Edge UI.