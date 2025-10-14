# 🔥 Firebase Console Setup - Step by Step Guide

## 🎯 **Your Project Details:**
- **Project ID**: `movein-b020b`
- **Project Number**: `54162983506`
- **Package Name**: `com.example.movein`
- **SHA-1 Fingerprint**: `573d3920eb702799a277d355432144c8bf997a84`

## 📋 **Step 1: Enable Google Sign-In in Firebase Console**

### **1.1 Open Firebase Console**
1. Go to: https://console.firebase.google.com/
2. Sign in with your Google account
3. Look for project: **"movein-b020b"**
4. Click on it to open

### **1.2 Navigate to Authentication**
1. In the left sidebar, look for **"Authentication"**
2. Click on **"Authentication"**
3. You should see a page with tabs at the top

### **1.3 Go to Sign-in Method**
1. Click on the **"Sign-in method"** tab
2. You'll see a list of sign-in providers
3. Look for **"Google"** in the list

### **1.4 Enable Google Sign-In**
1. Click on **"Google"** (it should be in the list)
2. You'll see a configuration page
3. **Toggle the "Enable" switch to ON** (it's probably OFF right now)
4. In the **"Project support email"** field, enter your email address
5. Click **"Save"**

## 📋 **Step 2: Configure OAuth Consent Screen**

### **2.1 Open Google Cloud Console**
1. Go to: https://console.cloud.google.com/
2. Sign in with the same Google account
3. Make sure you're in the **"movein-b020b"** project

### **2.2 Navigate to OAuth Consent Screen**
1. In the left sidebar, look for **"APIs & Services"**
2. Click on **"APIs & Services"**
3. Click on **"OAuth consent screen"**

### **2.3 Configure OAuth Consent Screen**
1. **User Type**: Select **"External"**
2. Click **"Create"**
3. Fill in the required fields:
   - **App name**: `MoveIn`
   - **User support email**: Your email address
   - **Developer contact information**: Your email address
4. Click **"Save and Continue"**

### **2.4 Add Scopes**
1. Click **"Add or Remove Scopes"**
2. Add these scopes:
   - `../auth/userinfo.email`
   - `../auth/userinfo.profile`
   - `openid`
3. Click **"Update"**
4. Click **"Save and Continue"**

### **2.5 Add Test Users**
1. In the **"Test users"** section, click **"Add Users"**
2. Add your email address
3. Click **"Save and Continue"**

## 🧪 **Step 3: Test Google Sign-In**

### **3.1 Try the Button**
1. Go back to your MoveIn app
2. Click the **"Sign in with Google"** button
3. **What should happen**: A Google sign-in popup should appear

### **3.2 Expected Results**
- ✅ **Success**: Google sign-in popup appears
- ❌ **Still nothing**: Check if you completed all steps above
- ❌ **Error message**: Note the exact error and tell me

## 🚨 **Troubleshooting**

### **If Google Sign-In is still not working:**

1. **Check Firebase Console**:
   - Go back to Firebase Console → Authentication → Sign-in method
   - Verify Google is **"Enabled"** (not disabled)

2. **Check OAuth Consent Screen**:
   - Go to Google Cloud Console → APIs & Services → OAuth consent screen
   - Verify it's configured and published

3. **Check SHA-1 Fingerprint**:
   - In Firebase Console → Project Settings → Your apps
   - Verify SHA-1 matches: `573d3920eb702799a277d355432144c8bf997a84`

## 📞 **What to Tell Me**

After completing the steps, please tell me:

1. **Did you successfully enable Google Sign-In in Firebase Console?**
2. **Did you configure the OAuth consent screen?**
3. **What happens when you click the Google Sign-In button now?**
4. **Any error messages you see?**

## 🎯 **Quick Checklist**

- [ ] Firebase Console → Authentication → Sign-in method → Google → Enable ON
- [ ] Google Cloud Console → OAuth consent screen → Configure
- [ ] Test Google Sign-In button in app
- [ ] Report results

---

**Follow these steps exactly, and Google Sign-In should work!** 🚀

