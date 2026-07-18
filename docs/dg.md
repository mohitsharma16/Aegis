Development Guidelines & Coding Standards
1. Architecture & DI Rules

Strict Layering: The UI layer MUST NEVER reference DAOs directly. Inject via Hilt.

ViewModels: Annotated with @HiltViewModel.

2. Jetpack Compose & Foldable Guidelines

Responsive Layouts: NEVER hardcode fixed heights/widths. Use Modifier.weight() for foldable compatibility.

Window Insets: Wrap top-level screens in Scaffold and use windowInsetsPadding to respect the Android 17 transparent navigation pill.

3. Streams & Memory Management

File I/O (Backups): When writing to the SAF Uri, always use use { } blocks in Kotlin to guarantee OutputStream closure. Never read the entire backup file into memory at once if it exceeds safe limits; utilize streams.

No Plaintext Logs: Log.d or Timber must NEVER output decrypted passwords.