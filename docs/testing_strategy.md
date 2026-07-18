Testing Strategy

1. Unit Testing (JUnit 5 + MockK)

Domain Layer: Test all Use Cases.

Security Layer:

Test TotpGenerator using known Base32 secrets.

Test BackupCryptoManager by encrypting a string with a password, and ensuring it decrypts properly with the same password (and fails with a wrong one).

2. Integration Testing

Keystore + Room: Write instrumentation tests for the encryption pipeline to ensure plaintext never touches the SQLite disk.

3. UI & Foldable Testing (Compose)

Configuration Changes: Use UI tests to simulate screen folding/unfolding states to ensure Hilt ViewModel state does not drop.

Multi-Window: Verify sensitive data is obscured when the app loses top focus.

4. Security & QA Testing

APK Analysis: Decompile release APK to verify android.permission.INTERNET is absent.

Backup Attack: Attempt to backup the app via adb backup to verify the OS rejects it.