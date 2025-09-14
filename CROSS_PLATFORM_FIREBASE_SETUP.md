# Cross-Platform Firebase Setup for MoveIn App

## 🎯 Overview

Your MoveIn app is a **Kotlin Multiplatform** project supporting both **Android** and **iOS**. This guide covers the complete Firebase setup for both platforms.

## 📱 Project Structure

```
MoveIn/
├── app/                    # Android app
│   ├── google-services.json    # Android Firebase config
│   └── src/main/java/...       # Android-specific code
├── iosApp/                 # iOS app
│   ├── GoogleService-Info.plist # iOS Firebase config
│   ├── Podfile                 # iOS dependencies
│   └── iosApp/                 # iOS Swift code
└── shared/                 # Shared Kotlin code
    ├── commonMain/             # Common business logic
    ├── androidMain/            # Android-specific implementations
    └── iosMain/                # iOS-specific implementations
```

## 🔥 Firebase Project Setup

### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create project: `movein-app`
3. Enable Google Analytics

### Step 2: Add Both Platforms
1. **Android App**:
   - Package: `com.example.movein`
   - Download: `google-services.json`
   - SHA-1: Get from debug keystore

2. **iOS App**:
   - Bundle ID: `com.example.movein` (same as Android)
   - Download: `GoogleService-Info.plist`
   - App Store ID: Optional

### Step 3: Enable Services
- ✅ Authentication → Google Sign-In
- ✅ Firestore Database
- ✅ Analytics

## 🤖 Android Setup (Already Done)

Your Android setup is complete with:
- ✅ Updated dependencies in `build.gradle.kts`
- ✅ Dynamic client ID configuration
- ✅ Error handling improvements
- ✅ Security measures (.gitignore)

## 🍎 iOS Setup (New)

### Prerequisites
- Xcode 12.0+
- iOS 11.0+
- CocoaPods

### Step 1: Initialize CocoaPods
```bash
cd iosApp
pod init
```

### Step 2: Add Dependencies
Copy `Podfile-template` to `iosApp/Podfile`:
```ruby
platform :ios, '11.0'

target 'iosApp' do
  use_frameworks!
  
  # Firebase dependencies
  pod 'Firebase/Auth'
  pod 'Firebase/Firestore'
  pod 'Firebase/Analytics'
  pod 'GoogleSignIn'
end
```

### Step 3: Install Dependencies
```bash
pod install
open iosApp.xcworkspace  # Use workspace, not project
```

### Step 4: Configure iOS App
Update `iosApp/iosApp/iosAppApp.swift`:
```swift
import SwiftUI
import Firebase
import GoogleSignIn

@main
struct iosAppApp: App {
    init() {
        FirebaseApp.configure()
        
        // Configure Google Sign-In
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              let plist = NSDictionary(contentsOfFile: path),
              let clientId = plist["CLIENT_ID"] as? String else {
            fatalError("GoogleService-Info.plist not found")
        }
        
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientId)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### Step 5: Configure URL Schemes
1. In Xcode → Project → Info → URL Types
2. Add URL Scheme: `YOUR_REVERSED_CLIENT_ID`
3. Get `REVERSED_CLIENT_ID` from `GoogleService-Info.plist`

## 🔧 Shared Code Integration

### Current Shared Structure
Your shared module already has:
- ✅ `commonMain/` - Business logic
- ✅ `androidMain/` - Android implementations
- ✅ `iosMain/` - iOS implementations

### Firebase Integration Points
The shared code can access Firebase through:
1. **Android**: Direct Firebase SDK calls
2. **iOS**: Swift interop with Firebase SDK
3. **Common**: Abstract interfaces for auth/data

## 📋 Configuration Files

### Android: `google-services.json`
```json
{
  "project_info": {
    "project_id": "your-firebase-project-id"
  },
  "client": [
    {
      "oauth_client": [
        {
          "client_id": "YOUR_ANDROID_CLIENT_ID",
          "client_type": 1
        },
        {
          "client_id": "YOUR_WEB_CLIENT_ID",
          "client_type": 3
        }
      ]
    }
  ]
}
```

### iOS: `GoogleService-Info.plist`
```xml
<dict>
  <key>CLIENT_ID</key>
  <string>YOUR_IOS_CLIENT_ID</string>
  <key>REVERSED_CLIENT_ID</key>
  <string>com.googleusercontent.apps.YOUR_IOS_CLIENT_ID</string>
</dict>
```

## 🔒 Security Configuration

### .gitignore Updates
```gitignore
# Firebase configuration (contains sensitive data)
app/google-services.json
iosApp/GoogleService-Info.plist

# iOS specific
iosApp/Pods/
iosApp/*.xcworkspace
iosApp/Podfile.lock
```

### Environment Management
- **Development**: Use debug certificates
- **Production**: Use release certificates
- **Staging**: Separate Firebase project

## 🧪 Testing Both Platforms

### Android Testing
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### iOS Testing
```bash
cd iosApp
pod install
open iosApp.xcworkspace
# Build and run in Xcode
```

### Cross-Platform Testing
1. Test Google Sign-In on both platforms
2. Verify data sync between platforms
3. Test offline functionality
4. Validate error handling

## 🚀 Deployment Checklist

### Before Release
- [ ] Real Firebase project created
- [ ] Both `google-services.json` and `GoogleService-Info.plist` configured
- [ ] SHA-1 fingerprints added to Firebase
- [ ] OAuth consent screen configured
- [ ] URL schemes configured for iOS
- [ ] Dependencies installed (`pod install`)
- [ ] Google Sign-In tested on both platforms
- [ ] Firestore rules configured
- [ ] Analytics enabled

### Production Considerations
- [ ] Separate Firebase projects for dev/staging/prod
- [ ] Release certificates configured
- [ ] App Store/Play Store configurations
- [ ] Privacy policy and terms of service
- [ ] Data retention policies

## 🆘 Troubleshooting

### Common Issues
1. **"Invalid client"**: Check SHA-1 fingerprint
2. **"Sign-in failed"**: Verify OAuth consent screen
3. **iOS build errors**: Ensure using `.xcworkspace`
4. **Missing dependencies**: Run `pod install`

### Debug Steps
1. Check Firebase Console for error logs
2. Verify configuration files are in correct locations
3. Ensure all dependencies are properly installed
4. Test with debug certificates first

## 📞 Support Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Google Sign-In for iOS](https://developers.google.com/identity/sign-in/ios)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

Your cross-platform Firebase setup is now ready! 🎉
