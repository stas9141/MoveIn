# Firebase Setup Guide for MoveIn Cross-Platform App

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Enter project name: `movein-app` (or your preferred name)
4. Enable Google Analytics (recommended)
5. Choose or create a Google Analytics account
6. Click "Create project"

## Step 2: Add Android App to Firebase

1. In your Firebase project, click "Add app" → Android
2. Enter package name: `com.example.movein`
3. Enter app nickname: `MoveIn Android`
4. Enter SHA-1 certificate fingerprint (see Step 4 below)
5. Click "Register app"

## Step 3: Add iOS App to Firebase

1. In your Firebase project, click "Add app" → iOS
2. Enter bundle ID: `com.example.movein` (same as Android package name)
3. Enter app nickname: `MoveIn iOS`
4. Enter App Store ID (optional, can be added later)
5. Click "Register app"

## Step 4: Get SHA-1 Certificate Fingerprint

### For Debug Build (Your Current Setup):
```bash
# On macOS (your system)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Copy the SHA1 fingerprint** (it looks like: `AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD`)

### For Release Build (When you create one):
```bash
# Replace with your actual keystore path and alias
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias_name
```

## Step 5: Download Configuration Files

### For Android:
1. After registering your Android app, download the `google-services.json` file
2. **Replace the existing file** at this exact path:
   ```
   /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/app/google-services.json
   ```
3. **Important**: Never commit the real `google-services.json` to version control

### For iOS:
1. After registering your iOS app, download the `GoogleService-Info.plist` file
2. **Add the file** to your iOS project at this exact path:
   ```
   /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/GoogleService-Info.plist
   ```
3. **In Xcode**:
   - Open: `/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp.xcodeproj`
   - Drag and drop `GoogleService-Info.plist` into the `iosApp` folder
   - Make sure "Copy items if needed" is checked
   - Add to target: `iosApp`
4. **Important**: Never commit the real `GoogleService-Info.plist` to version control

## Step 6: Enable Authentication

1. In Firebase Console, go to "Authentication" → "Sign-in method"
2. Enable "Google" sign-in provider
3. Add your Android app's SHA-1 fingerprint to the authorized domains
4. Configure OAuth consent screen if needed

## Step 7: Enable Firestore (if using)

1. Go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location for your database

## Step 8: Update App Configuration

After downloading the configuration files:
- **Android**: The app will automatically use the correct client IDs from `google-services.json`
- **iOS**: You'll need to configure the iOS app to use Firebase (see iOS setup below)

## iOS-Specific Setup

### Prerequisites:
- Xcode 12.0 or later
- iOS 11.0 or later
- CocoaPods (for dependency management)

### Step 1: Install Firebase iOS SDK
1. **Open Terminal** and navigate to your iOS project directory:
   ```bash
   cd /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp
   ```

2. **Initialize CocoaPods** (if not already done):
   ```bash
   pod init
   ```

3. **Add Firebase dependencies** to `Podfile` at:
   ```
   /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/Podfile
   ```
   
   Copy this content:
   ```ruby
   platform :ios, '11.0'
   
   target 'iosApp' do
     use_frameworks!
     
     # Firebase dependencies
     pod 'Firebase/Auth'
     pod 'Firebase/Firestore'
     pod 'Firebase/Analytics'
     pod 'GoogleSignIn'
     
     target 'iosAppTests' do
       inherit! :search_paths
     end
   end
   ```

4. **Install dependencies**:
   ```bash
   pod install
   ```

5. **Open the workspace** (not the project):
   ```bash
   open /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp.xcworkspace
   ```

### Step 2: Configure iOS App
1. **In Xcode**, open this file:
   ```
   /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp/iosAppApp.swift
   ```

2. **Replace the content** with Firebase configuration:
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
               fatalError("GoogleService-Info.plist not found or CLIENT_ID missing")
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

### Step 3: Configure URL Schemes
1. **In Xcode**, select your project: `iosApp`
2. **Go to "Info" tab**
3. **Add URL Schemes**:
   - Add a new URL Scheme with your `REVERSED_CLIENT_ID` from `GoogleService-Info.plist`
   - Format: `com.googleusercontent.apps.YOUR_CLIENT_ID`
   - **Example**: If your `REVERSED_CLIENT_ID` is `com.googleusercontent.apps.123456789012-abcdefghijklmnopqrstuvwxyz123456`, add exactly that as the URL Scheme

### Step 4: Update Info.plist
1. **In Xcode**, open this file:
   ```
   /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp/Info.plist
   ```

2. **Add the following** to your `Info.plist`:
   ```xml
   <key>CFBundleURLTypes</key>
   <array>
       <dict>
           <key>CFBundleURLName</key>
           <string>REVERSED_CLIENT_ID</string>
           <key>CFBundleURLSchemes</key>
           <array>
               <string>YOUR_REVERSED_CLIENT_ID</string>
           </array>
       </dict>
   </array>
   ```

3. **Replace `YOUR_REVERSED_CLIENT_ID`** with the actual value from your `GoogleService-Info.plist`

## Security Notes

- Keep your `google-services.json` and `GoogleService-Info.plist` files secure
- Add them to `.gitignore` to prevent accidental commits
- Use different Firebase projects for development and production
- Regularly rotate your API keys in production

## Testing

### Android Testing:
1. **Build and run** your Android app:
   ```bash
   cd /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn
   ./gradlew assembleDebug
   ```

2. **Install and test**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### iOS Testing:
1. **Build and run** your iOS app in Xcode:
   - Open: `/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp.xcworkspace`
   - Build and run on simulator or device

2. **Test Google Sign-In** on both platforms
3. **Check Firebase Console** for authentication logs
4. **Verify user data** is being stored correctly

## 📁 Quick Reference - All Your Paths

### Project Root:
```
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/
```

### Android Files:
```
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/app/google-services.json
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/app/build.gradle.kts
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/app/src/main/AndroidManifest.xml
```

### iOS Files:
```
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/GoogleService-Info.plist
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/Podfile
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp.xcodeproj
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp.xcworkspace
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp/iosAppApp.swift
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp/iosApp/Info.plist
```

### Shared Code:
```
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/shared/src/commonMain/
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/shared/src/androidMain/
/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/shared/src/iosMain/
```

### Terminal Commands:
```bash
# Navigate to project
cd /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn

# Navigate to iOS
cd /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/iosApp

# Get SHA-1 fingerprint
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Build Android
./gradlew assembleDebug

# Install iOS dependencies
pod install
```

## Troubleshooting

### Common Issues:
- **"Invalid client"**: Check SHA-1 fingerprint matches
- **"Sign-in failed"**: Verify OAuth consent screen is configured
- **"Network error"**: Check internet connection and Firebase project status
- **iOS build errors**: Ensure using `.xcworkspace` not `.xcodeproj`

### Debug Steps:
1. Check Firebase Console for error logs
2. Verify configuration files are in the correct locations (see paths above)
3. Ensure all dependencies are properly added
4. Check that the app package name matches Firebase configuration
5. For iOS: Make sure you're using the workspace file, not the project file
