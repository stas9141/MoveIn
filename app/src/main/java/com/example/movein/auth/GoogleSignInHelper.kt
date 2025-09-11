package com.example.movein.auth

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(private val activity: ComponentActivity) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(activity)
    private val auth = FirebaseAuth.getInstance()
    
    private var onSignInResult: ((Result<Unit>) -> Unit)? = null
    
    private val signInLauncher: ActivityResultLauncher<IntentSenderRequest> = 
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleSignInResult(result.data)
        }
    
    fun signInWithGoogle(onResult: (Result<Unit>) -> Unit) {
        onSignInResult = onResult
        try {
            startGoogleSignIn()
        } catch (e: Exception) {
            onResult(Result.failure(Exception("Google Sign-In setup error: ${e.message}")))
        }
    }
    
    private fun startGoogleSignIn() {
        try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId("123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com") // TODO: Replace with real client ID from google-services.json
                        .build()
                )
                .build()
            
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        signInLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        onSignInResult?.invoke(Result.failure(Exception("Failed to launch Google Sign-In: ${e.message}")))
                    }
                }
                .addOnFailureListener { exception ->
                    onSignInResult?.invoke(Result.failure(Exception("Google Sign-In failed: ${exception.message}")))
                }
        } catch (e: Exception) {
            onSignInResult?.invoke(Result.failure(Exception("Google Sign-In setup failed: ${e.message}")))
        }
    }
    
    private fun handleSignInResult(data: Intent?) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSignInResult?.invoke(Result.success(Unit))
                        } else {
                            onSignInResult?.invoke(Result.failure(task.exception ?: Exception("Sign-in failed")))
                        }
                    }
            } else {
                onSignInResult?.invoke(Result.failure(Exception("No ID token received")))
            }
        } catch (e: ApiException) {
            onSignInResult?.invoke(Result.failure(e))
        }
    }
}
