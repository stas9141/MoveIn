# 🔥 Complete Firebase Setup Guide for MoveIn App

## 📋 Prerequisites
- Google account
- Android Studio installed
- MoveIn project ready
- Terminal access

## 🎯 Step 1: Create Firebase Project

### 1.1 Go to Firebase Console
1. **Open**: [Firebase Console](https://console.firebase.google.com/)
2. **Sign in** with your Google account
3. **Click**: "Create a project" or "Add project"

### 1.2 Project Configuration
1. **Project name**: `movein-e2f3d` (or your preferred name)
2. **Enable Google Analytics**: ✅ Yes (recommended)
3. **Choose or create** Google Analytics account
4. **Click**: "Create project"
5. **Wait** for project creation (1-2 minutes)

## 📱 Step 2: Add Android App to Firebase

### 2.1 Register Android App
1. **In Firebase Console**, click "Add app" → **Android icon** (🤖)
2. **Package name**: `com.example.movein`
3. **App nickname**: `MoveIn Android`
4. **SHA-1 fingerprint**: `3C:29:1F:83:99:B3:27:6D:46:65:D5:01:99:4C:CD:EF:10:CD:0C:53`
5. **Click**: "Register app"

### 2.2 Download google-services.json
1. **Download** the `google-services.json` file
2. **Save** to your computer (Downloads folder)

### 2.3 Replace Configuration File
1. **Navigate to**: `/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/app/`
2. **Replace**: `google-services.json` with the downloaded file
3. **Verify** the file is named exactly `google-services.json`

## 🔐 Step 3: Enable Authentication

### 3.1 Enable Google Sign-In
1. **In Firebase Console**, go to **"Authentication"**
2. **Click**: "Get started" (if first time)
3. **Go to**: "Sign-in method" tab
4. **Click**: "Google" → "Enable"
5. **Project support email**: Your email address
6. **Click**: "Save"

### 3.2 Configure OAuth Consent Screen
1. **Go to**: [Google Cloud Console](https://console.cloud.google.com/)
2. **Select your project**: `movein-e2f3d`
3. **Go to**: APIs & Services → OAuth consent screen
4. **Configure**:
   - App name: `MoveIn`
   - User support email: Your email
   - Developer contact: Your email
5. **Add scopes** (if needed):
   - `../auth/userinfo.email`
   - `../auth/userinfo.profile`
6. **Save** configuration

## 🗄️ Step 4: Enable Firestore Database

### 4.1 Create Firestore Database
1. **In Firebase Console**, go to **"Firestore Database"**
2. **Click**: "Create database"
3. **Choose mode**: "Start in test mode" (for development)
4. **Select location**: Choose closest to your users
5. **Click**: "Done"

### 4.2 Configure Security Rules (Optional)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 🔧 Step 5: Configure Android Project

### 5.1 Verify google-services.json
Check that your `google-services.json` contains:
```json
{
  "project_info": {
    "project_id": "movein-e2f3d",
    "project_number": "YOUR_PROJECT_NUMBER"
  },
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.example.movein"
        }
      },
      "oauth_client": [
        {
          "client_id": "YOUR_ANDROID_CLIENT_ID",
          "client_type": 1,
          "android_info": {
            "package_name": "com.example.movein",
            "certificate_hash": "3C:29:1F:83:99:B3:27:6D:46:65:D5:01:99:4C:CD:EF:10:CD:0C:53"
          }
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

### 5.2 Verify Dependencies
Check `app/build.gradle.kts` contains:
```kotlin
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}
```

### 5.3 Verify Google Services Plugin
Check `app/build.gradle.kts` contains:
```kotlin
plugins {
    // ... other plugins
}

// Apply Google Services plugin
apply(plugin = "com.google.gms.google-services")
```

## 🧪 Step 6: Test Configuration

### 6.1 Build the Project
```bash
cd /Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn
bash gradlew assembleDebug
```

### 6.2 Install and Test
1. **Install APK** via Android Studio or adb
2. **Test Google Sign-In**:
   - Open app
   - Go to Login screen
   - Click "Sign in with Google"
   - Verify authentication works

### 6.3 Test Firestore
1. **Sign in** to the app
2. **Add a task** or defect
3. **Check Firebase Console** → Firestore Database
4. **Verify** data appears in the database

## 🔍 Step 7: Troubleshooting

### 7.1 Common Issues

#### Issue: "No matching client found for package name"
**Solution**: Verify package name in `google-services.json` matches `com.example.movein`

#### Issue: "Google Sign-In failed"
**Solution**: 
1. Check SHA-1 fingerprint is correct
2. Verify Google Sign-In is enabled in Firebase Console
3. Check OAuth consent screen is configured

#### Issue: "Firebase project not found"
**Solution**: Verify `google-services.json` contains correct project ID

### 7.2 Debug Commands
```bash
# Check current google-services.json
cat app/google-services.json | grep -A 5 "project_id"

# Verify SHA-1 fingerprint
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Clean and rebuild
bash gradlew clean
bash gradlew assembleDebug
```

## 📱 Step 8: iOS Configuration (Optional)

### 8.1 Add iOS App
1. **In Firebase Console**, click "Add app" → **iOS icon** (🍎)
2. **Bundle ID**: `com.example.movein`
3. **App nickname**: `MoveIn iOS`
4. **Download** `GoogleService-Info.plist`

### 8.2 Configure iOS Project
1. **Add** `GoogleService-Info.plist` to iOS project
2. **Configure** URL schemes in Info.plist
3. **Install** Firebase iOS SDK via CocoaPods

## ✅ Step 9: Verification Checklist

- [ ] Firebase project created (`movein-e2f3d`)
- [ ] Android app registered with correct package name
- [ ] SHA-1 fingerprint added
- [ ] `google-services.json` downloaded and placed correctly
- [ ] Google Sign-In enabled
- [ ] Firestore Database created
- [ ] OAuth consent screen configured
- [ ] App builds successfully
- [ ] Google Sign-In works in app
- [ ] Data syncs to Firestore

## 🚀 Step 10: Production Setup

### 10.1 Security Rules
Update Firestore rules for production:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 10.2 Release Configuration
1. **Generate release keystore**
2. **Add release SHA-1** to Firebase Console
3. **Download new** `google-services.json` for release
4. **Test** release build

## 📞 Support

If you encounter issues:
1. Check Firebase Console for error logs
2. Verify all configuration steps completed
3. Test with debug build first
4. Check Android Studio logs for detailed errors

## 🎉 Success!

Once all steps are completed, your MoveIn app will have:
- ✅ Google Sign-In authentication
- ✅ Cloud data storage with Firestore
- ✅ Offline functionality with sync
- ✅ Cross-platform support
- ✅ Production-ready configuration

Your Firebase setup is now complete! 🚀
