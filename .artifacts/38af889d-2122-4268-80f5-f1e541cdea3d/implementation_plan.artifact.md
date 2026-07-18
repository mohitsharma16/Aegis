# Fix for Failed to resolve: androidx.compose.ui:ui-test-junit4

The error occurs because `androidx.compose.ui:ui-test-junit4` is declared without a version in the version catalog (`libs.versions.toml`) and the Compose BOM (Bill of Materials) is only applied to the `implementation` configuration. Since `androidTestImplementation` and `debugImplementation` do not inherit from `implementation`, they cannot resolve versionless Compose dependencies.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/mohit/AndroidStudioProjects/Aegis/app/build.gradle.kts)

Add the Compose BOM to `androidTestImplementation` and `debugImplementation` configurations to ensure all Compose-related dependencies (including test and tooling) use consistent versions managed by the BOM.

## Verification Plan

### Automated Tests
- Run Gradle sync to verify that the dependency resolution error is resolved.
- Run `app:assembleDebug` and `app:assembleDebugAndroidTest` to ensure the project builds correctly.
