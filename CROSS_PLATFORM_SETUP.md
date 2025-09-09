# MoveIn Cross-Platform Setup Guide

This project has been converted to support both Android and iOS using Kotlin Multiplatform Mobile (KMM).

## Project Structure

```
MoveIn/
├── app/                    # Android app
├── shared/                 # Shared Kotlin Multiplatform module
│   ├── src/
│   │   ├── commonMain/     # Shared business logic
│   │   ├── androidMain/    # Android-specific implementations
│   │   └── iosMain/        # iOS-specific implementations
│   └── build.gradle.kts
├── iosApp/                 # iOS app
│   ├── iosApp/
│   │   ├── iosAppApp.swift
│   │   ├── ContentView.swift
│   │   └── Assets.xcassets/
│   └── iosApp.xcodeproj/
└── build.gradle.kts
```

## Prerequisites

### For Android Development
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK 24+

### For iOS Development
- Xcode 14.0 or later
- macOS (required for iOS development)
- iOS 13.0+ deployment target

## Setup Instructions

### 1. Android Setup

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. The shared module will be automatically built and linked

### 2. iOS Setup

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Build the shared framework first:
   ```bash
   cd shared
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```
3. In Xcode, add the shared framework to your project:
   - Go to Project Settings → Build Phases
   - Add the shared.framework to "Embed Frameworks"

### 3. Building the Shared Framework

To build the shared framework for iOS:

```bash
# Build for iOS Simulator
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Build for iOS Device
./gradlew :shared:linkDebugFrameworkIosArm64

# Build for both (Universal Framework)
./gradlew :shared:linkDebugFrameworkIosX64 :shared:linkDebugFrameworkIosArm64
```

## Shared Module Features

### Data Models
- `UserData`: User apartment information
- `Defect`: Defect management
- `ChecklistItem`: Task management
- `SubTask`: Sub-task management
- All models support Kotlinx Serialization for cross-platform data persistence

### Repositories
- `DefectRepository`: Manages defect data with Flow-based reactive updates
- `ChecklistRepository`: Manages checklist and task data

### Platform-Specific Storage
- **Android**: Uses SharedPreferences with Gson serialization
- **iOS**: Uses NSUserDefaults with Kotlinx Serialization

## Development Workflow

### 1. Adding New Features

1. **Shared Logic**: Add business logic to `shared/src/commonMain/`
2. **Android UI**: Update Android Compose UI in `app/src/main/`
3. **iOS UI**: Update SwiftUI in `iosApp/iosApp/`

### 2. Platform-Specific Code

Use `expect/actual` declarations for platform-specific implementations:

```kotlin
// In commonMain
expect class PlatformSpecificClass {
    fun doSomething()
}

// In androidMain
actual class PlatformSpecificClass {
    actual fun doSomething() {
        // Android implementation
    }
}

// In iosMain
actual class PlatformSpecificClass {
    actual fun doSomething() {
        // iOS implementation
    }
}
```

### 3. Data Persistence

The shared module provides platform-agnostic data persistence:

```kotlin
// Android
val storage = DefectStorage(context)
val defects = storage.loadDefects()

// iOS
val storage = DefectStorage()
val defects = storage.loadDefects()
```

## Building and Running

### Android
```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

### iOS
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select your target device or simulator
3. Build and run (⌘+R)

## Testing

### Shared Module Tests
```bash
./gradlew :shared:testDebugUnitTest
```

### Android Tests
```bash
./gradlew :app:testDebugUnitTest
```

### iOS Tests
Run tests in Xcode (⌘+U)

## Troubleshooting

### Common Issues

1. **iOS Framework Not Found**
   - Ensure the shared framework is built before running iOS app
   - Check that the framework is added to "Embed Frameworks" in Xcode

2. **Serialization Issues**
   - Ensure all data classes are marked with `@Serializable`
   - Check that kotlinx-serialization plugin is applied

3. **Build Failures**
   - Clean and rebuild: `./gradlew clean`
   - In Xcode: Product → Clean Build Folder

### Platform-Specific Considerations

#### Android
- Uses Jetpack Compose for UI
- SharedPreferences for data persistence
- Material Design 3 components

#### iOS
- Uses SwiftUI for UI
- NSUserDefaults for data persistence
- Native iOS design patterns

## Next Steps

1. **Complete iOS UI**: Implement all screens from Android app
2. **Add Navigation**: Implement proper navigation for iOS
3. **Platform Features**: Add camera, notifications, etc.
4. **Testing**: Add comprehensive tests for both platforms
5. **CI/CD**: Set up automated builds for both platforms

## Resources

- [Kotlin Multiplatform Mobile Documentation](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [SwiftUI](https://developer.apple.com/xcode/swiftui/)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)

