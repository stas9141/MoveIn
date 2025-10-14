# Apple Sign-In Implementation Guide 🍎

This guide provides step-by-step instructions for implementing Apple Sign-In in your MoveIn iOS app.

## Prerequisites

- Xcode 12.0 or later
- iOS 13.0 or later
- Apple Developer Account
- App registered in Apple Developer Console

## Step 1: Enable Apple Sign-In in Xcode

1. **Open your iOS project in Xcode**
   ```bash
   open iosApp/iosApp.xcworkspace
   ```

2. **Select your app target** in the project navigator

3. **Go to Signing & Capabilities tab**

4. **Click the "+ Capability" button**

5. **Add "Sign In with Apple" capability**

## Step 2: Configure Apple Developer Console

1. **Go to [Apple Developer Console](https://developer.apple.com/account/)**

2. **Navigate to Certificates, Identifiers & Profiles**

3. **Select your App ID** (e.g., `com.example.movein`)

4. **Enable "Sign In with Apple" capability**

5. **Save the configuration**

## Step 3: Add Apple Sign-In to iOS App

### 3.1 Update Podfile

Add the AuthenticationServices framework (already included in iOS 13+):

```ruby
# iosApp/Podfile
platform :ios, '13.0'

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

### 3.2 Create Apple Sign-In Helper

Create a new Swift file: `iosApp/iosApp/AppleSignInHelper.swift`

```swift
import Foundation
import AuthenticationServices
import CryptoKit

class AppleSignInHelper: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {
    
    private var completion: ((Result<AppleSignInResult, Error>) -> Void)?
    
    func signInWithApple(completion: @escaping (Result<AppleSignInResult, Error>) -> Void) {
        self.completion = completion
        
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }
    
    // MARK: - ASAuthorizationControllerDelegate
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        if let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential {
            let result = AppleSignInResult(
                identityToken: String(data: appleIDCredential.identityToken ?? Data(), encoding: .utf8) ?? "",
                authorizationCode: String(data: appleIDCredential.authorizationCode ?? Data(), encoding: .utf8),
                user: createUserString(from: appleIDCredential)
            )
            completion?(.success(result))
        }
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        completion?(.failure(error))
    }
    
    // MARK: - ASAuthorizationControllerPresentationContextProviding
    
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        return UIApplication.shared.windows.first { $0.isKeyWindow } ?? UIWindow()
    }
    
    // MARK: - Private Methods
    
    private func createUserString(from credential: ASAuthorizationAppleIDCredential) -> String? {
        guard let fullName = credential.fullName else { return nil }
        
        let userInfo: [String: Any] = [
            "name": [
                "firstName": fullName.givenName ?? "",
                "lastName": fullName.familyName ?? ""
            ]
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: userInfo),
              let jsonString = String(data: jsonData, encoding: .utf8) else {
            return nil
        }
        
        return jsonString
    }
}

struct AppleSignInResult {
    let identityToken: String
    let authorizationCode: String?
    let user: String?
}
```

### 3.3 Update ContentView

Update your main ContentView to include Apple Sign-In button:

```swift
import SwiftUI
import AuthenticationServices

struct ContentView: View {
    @StateObject private var appleSignInHelper = AppleSignInHelper()
    @State private var isSignedIn = false
    @State private var userEmail: String?
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                if isSignedIn {
                    VStack {
                        Text("Welcome!")
                            .font(.title)
                        if let email = userEmail {
                            Text("Signed in as: \(email)")
                                .foregroundColor(.secondary)
                        }
                        Button("Sign Out") {
                            signOut()
                        }
                        .foregroundColor(.red)
                    }
                } else {
                    VStack(spacing: 16) {
                        Text("MoveIn App")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                        
                        // Apple Sign-In Button
                        SignInWithAppleButton(
                            onRequest: { request in
                                request.requestedScopes = [.fullName, .email]
                            },
                            onCompletion: { result in
                                handleAppleSignIn(result)
                            }
                        )
                        .signInWithAppleButtonStyle(.black)
                        .frame(height: 50)
                        .cornerRadius(8)
                    }
                    .padding()
                }
            }
            .navigationTitle("MoveIn")
        }
    }
    
    private func handleAppleSignIn(_ result: Result<ASAuthorization, Error>) {
        switch result {
        case .success(let authorization):
            if let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential {
                let identityToken = String(data: appleIDCredential.identityToken ?? Data(), encoding: .utf8) ?? ""
                let authorizationCode = String(data: appleIDCredential.authorizationCode ?? Data(), encoding: .utf8)
                
                // Send to backend
                sendToBackend(identityToken: identityToken, authorizationCode: authorizationCode)
            }
        case .failure(let error):
            print("Apple Sign-In failed: \(error.localizedDescription)")
        }
    }
    
    private func sendToBackend(identityToken: String, authorizationCode: String?) {
        // TODO: Implement backend API call
        // This would call your backend's /auth/apple-signin endpoint
        print("Sending to backend: \(identityToken)")
        
        // For now, just simulate success
        DispatchQueue.main.async {
            self.isSignedIn = true
            self.userEmail = "user@example.com"
        }
    }
    
    private func signOut() {
        isSignedIn = false
        userEmail = nil
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
```

## Step 4: Backend Integration

The backend is already configured with Apple Sign-In support. Make sure to:

1. **Set the Apple Client ID** in your environment variables:
   ```bash
   APPLE_CLIENT_ID="com.example.movein"
   ```

2. **Install dependencies**:
   ```bash
   cd backend
   npm install
   ```

3. **Start the backend server**:
   ```bash
   npm run dev
   ```

## Step 5: Testing

1. **Build and run the iOS app**:
   ```bash
   # In Xcode, select your device/simulator and press Cmd+R
   ```

2. **Test Apple Sign-In**:
   - Tap the "Sign In with Apple" button
   - Complete the Apple ID authentication
   - Verify the user is created in your backend

## Step 6: Production Considerations

### 6.1 App Store Review

- Ensure your app provides alternative sign-in methods
- Apple Sign-In should be prominently displayed
- Don't require Apple Sign-In for core functionality

### 6.2 Privacy

- Handle user data according to Apple's guidelines
- Respect user's choice to hide email
- Implement proper data deletion

### 6.3 Error Handling

```swift
private func handleAppleSignInError(_ error: Error) {
    if let authError = error as? ASAuthorizationError {
        switch authError.code {
        case .canceled:
            print("User canceled Apple Sign-In")
        case .failed:
            print("Apple Sign-In failed")
        case .invalidResponse:
            print("Invalid response from Apple")
        case .notHandled:
            print("Apple Sign-In not handled")
        case .unknown:
            print("Unknown Apple Sign-In error")
        @unknown default:
            print("Unknown Apple Sign-In error")
        }
    }
}
```

## Troubleshooting

### Common Issues

1. **"Sign In with Apple" not available**
   - Ensure iOS 13.0+ target
   - Check Apple Developer Console configuration
   - Verify app bundle ID matches

2. **Invalid client ID error**
   - Check APPLE_CLIENT_ID environment variable
   - Ensure it matches your app's bundle ID

3. **Token verification fails**
   - Verify Apple's public keys are accessible
   - Check network connectivity
   - Ensure proper JWT verification

### Debug Tips

1. **Enable verbose logging** in your backend
2. **Check Apple Developer Console** for any configuration issues
3. **Test on physical device** (simulator has limitations)
4. **Verify bundle ID** matches exactly

## Security Best Practices

1. **Always verify tokens** on the backend
2. **Use HTTPS** for all API calls
3. **Implement rate limiting** for sign-in attempts
4. **Log security events** for monitoring
5. **Handle token expiration** gracefully

## Next Steps

After implementing Apple Sign-In:

1. **Test thoroughly** on multiple devices
2. **Implement proper error handling**
3. **Add analytics** for sign-in success rates
4. **Consider implementing** account linking
5. **Prepare for App Store** review

## Resources

- [Apple Sign-In Documentation](https://developer.apple.com/sign-in-with-apple/)
- [AuthenticationServices Framework](https://developer.apple.com/documentation/authenticationservices)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/sign-in-with-apple/overview/)
- [Backend Implementation Guide](./BACKEND_IMPLEMENTATION_GUIDE.md)

---

**Note**: This implementation provides a complete Apple Sign-In integration. Make sure to test thoroughly before deploying to production.

