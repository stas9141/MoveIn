# Firebase Authentication Setup Guide

## Issue: "Unable to create account" Error

The sign-up is failing because Firebase Authentication is not properly configured. Here's how to fix it:

## Step 1: Enable Firebase Authentication

1. **Go to Firebase Console**: https://console.firebase.google.com/
2. **Select your project**: `movein-b020b`
3. **Navigate to Authentication**:
   - Click on "Authentication" in the left sidebar
   - Click on "Get started" if you haven't set it up yet
4. **Enable Email/Password Authentication**:
   - Click on "Sign-in method" tab
   - Click on "Email/Password"
   - Toggle "Enable" to ON
   - Click "Save"

## Step 2: Verify Firebase Configuration

1. **Check google-services.json**:
   - Make sure the file is in `/app/google-services.json`
   - Verify the package name matches: `com.example.movein`
   - Verify the project ID matches: `movein-b020b`

2. **Check build.gradle.kts**:
   - Make sure Google Services plugin is applied
   - Make sure Firebase BOM is included

## Step 3: Test the Setup

1. **Run the app**
2. **Try to sign up** with a test email
3. **Check the logs** for specific error messages

## Common Issues and Solutions

### Issue 1: "Cloud storage not available"
- **Cause**: CloudStorage initialization failed
- **Solution**: Check Firebase configuration and ensure google-services.json is correct

### Issue 2: "Firebase Auth not enabled"
- **Cause**: Email/Password authentication is not enabled in Firebase Console
- **Solution**: Enable Email/Password authentication in Firebase Console

### Issue 3: "Invalid API key"
- **Cause**: google-services.json is incorrect or outdated
- **Solution**: Download fresh google-services.json from Firebase Console

### Issue 4: "Network error"
- **Cause**: No internet connection or Firebase service is down
- **Solution**: Check internet connection and Firebase status

## Step 4: Enable Additional Authentication Methods (Optional)

1. **Google Sign-In**:
   - In Firebase Console > Authentication > Sign-in method
   - Click on "Google"
   - Toggle "Enable" to ON
   - Add your SHA-1 fingerprint
   - Click "Save"

2. **Anonymous Authentication** (if needed):
   - Click on "Anonymous"
   - Toggle "Enable" to ON
   - Click "Save"

## Step 5: Test All Authentication Methods

1. **Email/Password Sign-up**
2. **Email/Password Sign-in**
3. **Google Sign-in** (if enabled)
4. **Password Reset**

## Troubleshooting

If you're still getting errors:

1. **Check Firebase Console** for any error messages
2. **Check Android Studio Logcat** for detailed error logs
3. **Verify internet connection**
4. **Try with a different email address**
5. **Check if the email domain is blocked**

## Quick Fix Commands

```bash
# Clean and rebuild
./gradlew clean
./gradlew :app:assembleDebug

# Check if google-services.json is valid
cat app/google-services.json | grep project_id
```

## Expected Behavior After Fix

- ✅ Sign-up should work with valid email/password
- ✅ Sign-in should work with existing credentials
- ✅ Error messages should be user-friendly
- ✅ Loading states should show during authentication
- ✅ Success navigation should work properly

