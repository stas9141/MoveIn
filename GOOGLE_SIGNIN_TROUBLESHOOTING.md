# 🔧 Google Sign-In Troubleshooting Guide

## 🚨 **Issue: "Sign in with Google" button does nothing**

### **Step 1: Enable Google Sign-In in Firebase Console**

1. **Go to**: [Firebase Console](https://console.firebase.google.com/)
2. **Select project**: `movein-b020b`
3. **Navigate to**: Authentication → Sign-in method
4. **Click on**: Google provider
5. **Enable**: Toggle "Enable" switch
6. **Add support email**: Your email address
7. **Click**: "Save"

### **Step 2: Configure OAuth Consent Screen**

1. **Go to**: [Google Cloud Console](https://console.cloud.google.com/)
2. **Select project**: `movein-b020b`
3. **Navigate to**: APIs & Services → OAuth consent screen
4. **Configure**:
   - User Type: External
   - App name: MoveIn
   - User support email: Your email
   - Developer contact: Your email
5. **Add scopes**: 
   - `../auth/userinfo.email`
   - `../auth/userinfo.profile`
   - `openid`
6. **Add test users**: Your email address
7. **Save and continue**

### **Step 3: Verify SHA-1 Fingerprint**

Your current SHA-1: `573d3920eb702799a277d355432144c8bf997a84`

**Check in Firebase Console**:
1. Go to Project Settings → Your apps
2. Select your Android app
3. Verify SHA-1 fingerprint matches exactly

### **Step 4: Test with Logs**

Run the app with logs to see what's happening:

```bash
bash gradlew installDebug
adb logcat | grep -E "(GoogleSignIn|Firebase|Auth|Error)"
```

### **Step 5: Common Issues & Solutions**

#### **Issue A: "Google Sign-In failed: 10"
- **Solution**: SHA-1 fingerprint mismatch
- **Fix**: Update SHA-1 in Firebase Console

#### **Issue B: "No ID token received"
- **Solution**: OAuth consent screen not configured
- **Fix**: Complete Step 2 above

#### **Issue C: "Google Sign-In setup error"
- **Solution**: Missing dependencies or configuration
- **Fix**: Check build.gradle dependencies

#### **Issue D: Button click does nothing
- **Solution**: Check if GoogleSignInHelper is properly initialized
- **Fix**: Verify MainActivity setup

### **Step 6: Debug the Helper Class**

Add debug logs to see what's happening:

```kotlin
private fun startGoogleSignIn() {
    try {
        Log.d("GoogleSignIn", "Starting Google Sign-In...")
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getWebClientId())
                    .build()
            )
            .build()
        
        Log.d("GoogleSignIn", "Web Client ID: ${getWebClientId()}")
        
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                Log.d("GoogleSignIn", "Sign-in request successful")
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    signInLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Failed to launch: ${e.message}")
                    onSignInResult?.invoke(Result.failure(Exception("Failed to launch Google Sign-In: ${e.message}")))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GoogleSignIn", "Sign-in request failed: ${exception.message}")
                onSignInResult?.invoke(Result.failure(Exception("Google Sign-In failed: ${exception.message}")))
            }
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Setup failed: ${e.message}")
        onSignInResult?.invoke(Result.failure(Exception("Google Sign-In setup failed: ${e.message}")))
    }
}
```

### **Step 7: Test with Different Approach**

If the current approach doesn't work, try using the traditional Google Sign-In:

```kotlin
// Alternative implementation using GoogleSignInClient
private fun signInWithGoogleTraditional() {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getWebClientId())
        .requestEmail()
        .build()
    
    val googleSignInClient = GoogleSignIn.getClient(activity, gso)
    val signInIntent = googleSignInClient.signInIntent
    activity.startActivityForResult(signInIntent, RC_SIGN_IN)
}
```

### **Step 8: Verify Dependencies**

Check your `build.gradle.kts` has these dependencies:

```kotlin
implementation("com.google.android.gms:play-services-auth:20.7.0")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
```

## 🎯 **Quick Test Checklist**

- [ ] Google Sign-In enabled in Firebase Console
- [ ] OAuth consent screen configured
- [ ] SHA-1 fingerprint matches
- [ ] Web client ID is correct
- [ ] App has internet permission
- [ ] No runtime errors in logs

## 🚀 **Next Steps**

1. **Complete Steps 1-2** (Firebase Console setup)
2. **Run the app** and check logs
3. **Test Google Sign-In** button
4. **Report any error messages** you see

---

**If you're still having issues, please share:**
1. Any error messages from the logs
2. What happens when you click the button
3. Whether you completed the Firebase Console setup
