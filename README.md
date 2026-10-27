# Kora Messenger 🌿

**Kora** is a clean, modern Android messaging application built 100% with **Kotlin** and **Jetpack Compose**. It features an emerald green, crisp white, and deep dark theme, original Kora branding, real-time message sending, automated simulated replies, and full integration with **Firebase Authentication** and **Cloud Firestore** linked to the **SAT Limited** organization.

---

## 📱 App Highlights

- **Original Branding**: Minimalist "K" leaf emblem intertwining green and white accents.
- **Organization-Linked Registration**: Powered by Google Sign-In via Jetpack `CredentialManager`, registering users under **SAT Limited**.
- **100% Kotlin & Jetpack Compose**: Zero XML layouts, zero Java files; built using modern Material 3 guidelines.
- **Adaptive Theme**: Smooth, real-time switching between **Light**, **Dark**, and **System Default** modes.
- **Local Simulation**: Contacts realistically type and reply to messages, allowing full end-to-end prototype testing on Android phones.
- **Zero-Trust Cloud Database**: Automated security rules deployed on Cloud Firestore with user isolation.

---

## 🚀 Screens & Features

### 1. Registration & Authentication (`RegistrationScreen.kt`)
- **SAT Limited Affiliation**: Highlights corporate affiliation with a verified **SAT Limited** organization badge.
- **Google Sign-In**: Interactive button utilizing Jetpack `CredentialManager` (`GetSignInWithGoogleOption`).
- **Silent Auto Sign-In**: Automatically signs in returning users using `GetGoogleIdOption`.
- **Cancellation Safety**: Gracefully handles dismissed credential sheets without crashing or freezing.
- **Automatic Firestore Registration**: Creates a `/users/{userId}` record in Firestore with the user's name, email, handle, and organization affiliation.

### 2. Chats Screen (`ChatsScreen.kt`)
- **Conversations List**: Displays active chats with contact avatars, online indicators, last message snippets, and timestamps.
- **Delivery Statuses**: Sent messages display double checkmarks (`✓✓`) with read receipt colors.
- **Unread Badges**: Vibrant green pill badges showing unread message counts.
- **Filters**: Quickly filter chats by **All**, **Unread**, or **Pinned**.
- **Search Bar**: Live filtering by contact name or message keywords.
- **Floating Action Button (FAB)**: Quick-start button to launch a new chat from the contact book.

### 3. Chat Detail Screen (`ChatDetailScreen.kt`)
- **Chat Bubbles**: Tailored Material 3 bubbles (sent messages in fresh mint green; received messages in surface slate/white).
- **Date Separators**: Clean date chips ("Today", "Yesterday").
- **Real-Time Input**: Multi-line text field with emoji insertion shortcut.
- **Quick Attachment Sheet**: Modal sheet for sharing Photos, Documents, Locations, and Audio.
- **Dynamic Action Button**: Shows a Mic icon when empty, and dynamically animates to an emerald Send button when text is entered.
- **Contextual Auto-Reply Simulation**: After sending a message, the contact displays an active `typing...` indicator and replies with contextual banter.
- **Call Simulation**: Interactive voice and video call dialog with mute, speaker, and end call controls.
- **Back Navigation**: Android system gesture and top-bar back arrow support (`BackHandler`).

### 4. Contacts Screen (`ContactsScreen.kt`)
- **Alphabetical Directory**: Categorized sections (A–Z) with contact avatars, statuses, and phone numbers.
- **Direct Messaging**: Tapping any contact opens or creates a direct conversation thread.
- **Add Contact Dialog**: Add new contacts directly with custom name, phone number, and status.
- **Quick Actions**: Prominent cards for "New Contact" and "New Group".

### 5. Settings Screen (`SettingsScreen.kt`)
- **Profile Card**: Displays user avatar, display name, username handle, email, phone, and **🏢 SAT Limited (Verified)** organization badge.
- **Edit Profile Dialog**: Update display name, username, status/bio, and phone number (synchronized with Cloud Firestore).
- **Theme Selector**: Interactive radio tiles to switch between **Light Mode**, **Dark Mode**, and **System Default**.
- **Chat Settings**: Toggles for "Enter is Send", read receipts (blue ticks), and push notifications.
- **Privacy & Encryption**: Overview of zero-trust security architecture.
- **Sign Out**: Clears `CredentialManager` state and signs out of Firebase, returning to the registration screen.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.2.10 |
| **UI Framework** | Jetpack Compose (Compose BOM 2024.09.00) |
| **Design System** | Material Design 3 (M3) |
| **State Management** | Android ViewModel + Kotlin Coroutines `StateFlow` |
| **Authentication** | Firebase Auth + Jetpack Credential Manager (`GetSignInWithGoogleOption`) |
| **Database** | Cloud Firestore Enterprise (`ai-studio-android-koramess-...`) |
| **Build System** | Gradle with Kotlin DSL (`.gradle.kts`) |
| **Testing** | Robolectric 4.16.1, JUnit 4, Node.js `@firebase/rules-unit-testing` |

---

## 📂 Project Directory Structure

```text
/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Root Activity, Auth gating, Bottom Navigation
│   │   │   │   ├── model/
│   │   │   │   │   ├── ChatModels.kt             # Message, Chat, Contact, UserProfile data models
│   │   │   │   │   └── UserFirestoreModels.kt    # Firestore UserProfileDocument model
│   │   │   │   ├── data/
│   │   │   │   │   ├── SampleData.kt             # Starter contacts, message history, auto-replies
│   │   │   │   │   ├── FirestoreErrorHelper.kt   # JSON error logging helper
│   │   │   │   │   └── repository/
│   │   │   │   │       └── UserRepository.kt     # Firestore user data access layer
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── KoraViewModel.kt          # App state, messaging logic, Firestore sync
│   │   │   │   └── ui/
│   │   │   │       ├── auth/
│   │   │   │       │   └── AuthManager.kt        # Credential Manager Google Sign-In & Sign-Out
│   │   │   │       ├── components/
│   │   │   │       │   └── KoraComponents.kt     # Avatars, Logo, Message bubbles, Search bar, Dialogs
│   │   │   │       ├── screens/
│   │   │   │       │   ├── RegistrationScreen.kt # Google Auth registration linked to SAT Limited
│   │   │   │       │   ├── ChatsScreen.kt        # Conversation list, search, filters, and FAB
│   │   │   │       │   ├── ChatDetailScreen.kt   # Conversation bubbles, typing indicators, input bar
│   │   │   │       │   ├── ContactsScreen.kt     # Alphabetical contact book & Add Contact dialog
│   │   │   │       │   └── SettingsScreen.kt     # Profile editing, theme selector, Sign Out
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt              # Emerald green, white, and obsidian dark palette
│   │   │   │           ├── Theme.kt              # KoraTheme & LocalKoraChatColors composition
│   │   │   │           └── Type.kt               # Typography scale
│   │   │   ├── res/
│   │   │   │   ├── drawable/                     # Launcher foreground, background, and brand logo
│   │   │   │   ├── mipmap-*/                     # Adaptive and non-adaptive launcher icons
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml               # App label ("Kora")
│   │   │   │       └── firebase_applet_config.xml# Cloud Firestore database ID
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── java/com/example/
│   │           ├── base/
│   │           │   └── FirestoreEmulatorTestBase.kt # Robolectric emulator test harness
│   │           ├── UserRepositoryRuleTest.kt        # Kotlin Firestore rules integration tests
│   │           ├── ExampleRobolectricTest.kt        # Local context tests
│   │           └── ExampleUnitTest.kt               # Unit tests
│   ├── build.gradle.kts                             # Module build configuration
│   └── google-services.json                         # Firebase credentials & OAuth client configuration
├── firebase-blueprint.json                          # Intermediate representation schema
├── firestore.rules                                  # Zero-trust security rules deployed to backend
├── firestore.test.js                                # Tier 1 JavaScript rules unit test suite
├── firebase.json                                    # Emulator configuration binding to named DB
├── settings.gradle.kts
└── metadata.json
```

---

## 🔒 Security & Firestore Rules

Kora enforces **Zero-Trust Security Rules** (`firestore.rules`):
1. **Default-Deny Catch-All**: All unlisted paths reject read and write by default (`match /{document=**} { allow read, write: if false; }`).
2. **User Isolation**: Every `/users/{userId}` profile can only be read, created, updated, or deleted by the authenticated user whose `request.auth.uid == userId`.
3. **Immutability Protection**: Users cannot modify their immutable `userId`, account creation date (`createdAt`), or the affiliated `organization` (`SAT Limited`).
4. **Schema Validation**: Writes are validated against strict structural constraints using `isValidUserProfile()` and `isValidChatSummary()`.

### Rules Verification Tiers:
- **Tier 1 (JavaScript)**: Fast syntax and boundary test suite via `@firebase/rules-unit-testing` (`node --test firestore.test.js`).
- **Tier 2 (Kotlin Robolectric)**: Native Android integration test suite (`UserRepositoryRuleTest.kt`) executing repository CRUD against local emulators.

---

## 🧪 Testing Instructions

To run all unit tests and Robolectric rules tests in the build container:
```bash
gradle :app:testDebugUnitTest
```

To run rules tests specifically:
```bash
gradle :app:testDebugUnitTest --tests '*Rule*'
```

---

## 💡 Beginner Developer Tips

- **Modifying Theme Colors**: Open `app/src/main/java/com/example/ui/theme/Color.kt` to tweak Kora's signature emerald green accents.
- **Adding Starter Contacts**: Open `app/src/main/java/com/example/data/SampleData.kt` to add or edit starter contacts and automated responses.
- **Editing Organization Name**: The organization defaults to **SAT Limited**; you can adjust organization logic in `RegistrationScreen.kt`, `UserRepository.kt`, and `KoraViewModel.kt`.
