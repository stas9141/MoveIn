# 🧪 Google Sign-In Test Guide

## ✅ **Current Status:**
- ✅ Firebase configuration updated with real credentials
- ✅ App built and installed successfully
- ✅ Debug logging enabled
- ⏳ **Pending**: Firebase Console setup

## 🎯 **Test Steps:**

### **1. Open the App**
- Launch the MoveIn app on your device/emulator

### **2. Try Google Sign-In**
- Click the **"Sign in with Google"** button
- **Observe what happens**:
  - Does a Google sign-in popup appear?
  - Does it show an error message?
  - Does nothing happen at all?

### **3. Check Debug Logs**
The app now has debug logging enabled. Look for these log messages:

**If button click is registered:**
```
MainActivity: Google Sign-In button clicked
GoogleSignIn: signInWithGoogle called
```

**If Google Sign-In starts:**
```
GoogleSignIn: Starting Google Sign-In...
GoogleSignIn: Web Client ID: [your-client-id]
GoogleSignIn: Sign-in request built, calling beginSignIn...
```

**If successful:**
```
GoogleSignIn: Sign-in request successful
GoogleSignIn: Launching sign-in intent...
GoogleSignIn: Handling sign-in result...
GoogleSignIn: ID token received, signing in with Firebase...
GoogleSignIn: Firebase sign-in successful
MainActivity: Google Sign-In result: true
MainActivity: Navigating to Dashboard
```

**If there's an error:**
```
GoogleSignIn: Sign-in request failed: [error message]
MainActivity: Google Sign-In failed: [error message]
```

## 🚨 **Expected Issues & Solutions:**

### **Issue 1: "Nothing happens" when clicking button**
**Cause**: Google Sign-In not enabled in Firebase Console
**Solution**: 
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `movein-b020b`
3. Go to Authentication → Sign-in method
4. Enable Google provider

### **Issue 2: "Sign-in request failed: 10"**
**Cause**: SHA-1 fingerprint mismatch
**Solution**: Verify SHA-1 in Firebase Console matches: `573d3920eb702799a277d355432144c8bf997a84`

### **Issue 3: "No ID token received"**
**Cause**: OAuth consent screen not configured
**Solution**: Configure OAuth consent screen in Google Cloud Console

### **Issue 4: "Google Sign-In setup error"**
**Cause**: Missing dependencies or configuration
**Solution**: Check build.gradle dependencies

## 📱 **How to View Logs:**

### **Option 1: Android Studio**
1. Open Android Studio
2. Go to View → Tool Windows → Logcat
3. Filter by "GoogleSignIn" or "MainActivity"

### **Option 2: Command Line**
```bash
adb logcat | grep -E "(GoogleSignIn|MainActivity|Firebase)"
```

### **Option 3: Device/Emulator**
- Check if any error dialogs appear
- Look for toast messages or error indicators

## 🎯 **What to Report:**

Please tell me:
1. **What happens** when you click the Google Sign-In button?
2. **Any error messages** you see?
3. **Any log messages** you can find?
4. **Whether you completed** the Firebase Console setup?

## 🚀 **Next Steps:**

Based on your test results, I'll help you:
- Fix any remaining configuration issues
- Enable Google Sign-In in Firebase Console
- Configure OAuth consent screen
- Implement alternative approaches if needed

---

**Ready to test? Open the app and try the Google Sign-In button!** 🧪

