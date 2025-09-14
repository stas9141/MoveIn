# 🔒 Security Guide for MoveIn App

## ⚠️ **CRITICAL: Firebase Configuration Security**

### **Never Commit Real Firebase Configuration Files**

The following files contain sensitive information and should **NEVER** be committed to version control:

- `app/google-services.json` (Android Firebase config)
- `iosApp/GoogleService-Info.plist` (iOS Firebase config)

### **What These Files Contain:**
- API keys
- Project IDs
- Client IDs
- SHA-1 fingerprints
- Database URLs
- Storage bucket names

### **Security Risks:**
- Unauthorized access to your Firebase project
- Data breaches
- Unauthorized API usage
- Potential billing charges
- Project hijacking

## 🛡️ **How to Handle Firebase Configuration**

### **1. Use Template Files**
- Keep template files with placeholder values in the repository
- Replace placeholders with real values locally
- Never commit the real files

### **2. Environment Variables (Recommended)**
For production apps, consider using environment variables or secure configuration management.

### **3. Team Setup Process**
When team members clone the repository:

1. **Download real config files** from Firebase Console
2. **Replace template files** with real ones
3. **Never commit** the real files
4. **Use .gitignore** to prevent accidental commits

## 📋 **Current Template Files**

### **Android (`app/google-services.json`):**
```json
{
  "project_info": {
    "project_number": "YOUR_PROJECT_NUMBER",
    "project_id": "YOUR_PROJECT_ID",
    "storage_bucket": "YOUR_PROJECT_ID.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "YOUR_MOBILE_SDK_APP_ID",
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
            "certificate_hash": "YOUR_SHA1_FINGERPRINT"
          }
        },
        {
          "client_id": "YOUR_WEB_CLIENT_ID",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "YOUR_API_KEY"
        }
      ]
    }
  ]
}
```

### **iOS (`iosApp/GoogleService-Info.plist`):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CLIENT_ID</key>
    <string>YOUR_CLIENT_ID</string>
    <key>REVERSED_CLIENT_ID</key>
    <string>YOUR_REVERSED_CLIENT_ID</string>
    <key>API_KEY</key>
    <string>YOUR_API_KEY</string>
    <key>GCM_SENDER_ID</key>
    <string>YOUR_SENDER_ID</string>
    <key>PLIST_VERSION</key>
    <string>1</string>
    <key>BUNDLE_ID</key>
    <string>com.example.movein</string>
    <key>PROJECT_ID</key>
    <string>YOUR_PROJECT_ID</string>
    <key>STORAGE_BUCKET</key>
    <string>YOUR_PROJECT_ID.firebasestorage.app</string>
    <key>IS_ADS_ENABLED</key>
    <false/>
    <key>IS_ANALYTICS_ENABLED</key>
    <false/>
    <key>IS_APPINVITE_ENABLED</key>
    <true/>
    <key>IS_GCM_ENABLED</key>
    <true/>
    <key>IS_SIGNIN_ENABLED</key>
    <true/>
    <key>GOOGLE_APP_ID</key>
    <string>YOUR_GOOGLE_APP_ID</string>
</dict>
</plist>
```

## 🔧 **Setup Instructions for Team Members**

### **For Android:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to Project Settings → Your apps
4. Download `google-services.json`
5. Replace the template file in `app/google-services.json`

### **For iOS:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to Project Settings → Your apps
4. Download `GoogleService-Info.plist`
5. Replace the template file in `iosApp/GoogleService-Info.plist`

## 🚨 **If You Accidentally Committed Real Files**

### **Immediate Actions:**
1. **Remove from repository:**
   ```bash
   git rm --cached app/google-services.json
   git rm --cached iosApp/GoogleService-Info.plist
   ```

2. **Regenerate API keys** in Firebase Console
3. **Update .gitignore** to prevent future commits
4. **Force push** to remove from history (if recent)
5. **Notify team** to update their local files

### **For Sensitive Data Already in History:**
```bash
# Remove from entire git history (DANGEROUS - use with caution)
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch app/google-services.json' \
  --prune-empty --tag-name-filter cat -- --all
```

## ✅ **Best Practices**

1. **Always use template files** in the repository
2. **Never commit** real configuration files
3. **Use .gitignore** to prevent accidental commits
4. **Document setup process** for team members
5. **Regularly rotate** API keys and credentials
6. **Monitor Firebase usage** for unauthorized access
7. **Use environment variables** for production deployments

## 📞 **Emergency Contacts**

If you suspect a security breach:
1. **Immediately regenerate** all API keys in Firebase Console
2. **Review Firebase usage** and billing
3. **Update all team members** with new configuration files
4. **Consider project migration** if severely compromised

---

**Remember: Security is everyone's responsibility! 🔒**
