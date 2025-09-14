# 🔧 Google Sign-In Fix Guide

## 🚨 Current Problem
Your `google-services.json` file contains **demo/placeholder values** instead of real Firebase configuration.

## ✅ Solution Steps

### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Project name: `movein-app`
4. Enable Google Analytics: Yes
5. Click "Create project"

### Step 2: Add Android App
1. Click "Add app" → Android icon
2. Package name: `com.example.movein`
3. App nickname: `MoveIn Android`
4. SHA-1 fingerprint: `3C:29:1F:83:99:B3:27:6D:46:65:D5:01:99:4C:CD:EF:10:CD:0C:53`
5. Click "Register app"

### Step 3: Download Real google-services.json
1. Download the real `google-services.json` file from Firebase Console
2. Replace the file at: `/Users/stas.dobromilskyi/AndroidStudioProjects/MoveIn/app/google-services.json`

### Step 4: Enable Google Sign-In
1. Firebase Console → Authentication → Sign-in method
2. Click "Google" → "Enable"
3. Project support email: Your email
4. Click "Save"

### Step 5: Test
```bash
# Build the app
bash gradlew assembleDebug

# Install via Android Studio or adb
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🔍 How to Verify Real Configuration

Your real `google-services.json` should look like this:

```json
{
  "project_info": {
    "project_number": "YOUR_REAL_PROJECT_NUMBER",
    "project_id": "your-real-project-id",
    "storage_bucket": "your-real-project-id.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "YOUR_REAL_MOBILE_SDK_APP_ID",
        "android_client_info": {
          "package_name": "com.example.movein"
        }
      },
      "oauth_client": [
        {
          "client_id": "YOUR_REAL_ANDROID_CLIENT_ID",
          "client_type": 1,
          "android_info": {
            "package_name": "com.example.movein",
            "certificate_hash": "YOUR_REAL_SHA1_HASH"
          }
        },
        {
          "client_id": "YOUR_REAL_WEB_CLIENT_ID",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "YOUR_REAL_API_KEY"
        }
      ]
    }
  ]
}
```

## ❌ Current Demo Configuration (WRONG)
- Project ID: `movein-app-demo` ❌
- Client ID: `123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com` ❌
- API Key: `AIzaSyDemoKeyForMoveInApp123456789` ❌

## ✅ Expected Real Configuration
- Project ID: `your-real-project-id` ✅
- Client ID: `YOUR_REAL_CLIENT_ID.apps.googleusercontent.com` ✅
- API Key: `AIzaSyYOUR_REAL_API_KEY` ✅

## 🚀 After Fix
Once you replace the demo file with the real one:
1. Google Sign-In will work perfectly
2. Your app will connect to real Firebase
3. Authentication will function properly

## 📞 Need Help?
If you're still having issues after following these steps, please share:
1. The first few lines of your new `google-services.json` (without sensitive data)
2. Any error messages you see when testing Google Sign-In
3. Screenshots of the Firebase Console setup
