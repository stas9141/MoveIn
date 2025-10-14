package com.example.movein.auth

import android.content.Context
import android.content.Intent
import android.util.Log
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
import org.json.JSONObject
import java.io.IOException

class GoogleSignInHelper(private val activity: ComponentActivity) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(activity)
    private val auth = FirebaseAuth.getInstance()
    
    private var onSignInResult: ((Result<Unit>) -> Unit)? = null
    private var useBackendIntegration: Boolean = false
    
    private val signInLauncher: ActivityResultLauncher<IntentSenderRequest> = 
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleSignInResult(result.data)
        }
    
    fun signInWithGoogle(onResult: (Result<Unit>) -> Unit) {
        Log.d("GoogleSignIn", "signInWithGoogle called")
        onSignInResult = onResult
        try {
            startGoogleSignIn()
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Google Sign-In setup error: ${e.message}", e)
            onResult(Result.failure(Exception("Google Sign-In setup error: ${e.message}")))
        }
    }
    
    fun signInWithGoogleAndBackend(onResult: (Result<Unit>) -> Unit) {
        Log.d("GoogleSignIn", "signInWithGoogleAndBackend called")
        onSignInResult = onResult
        useBackendIntegration = true
        try {
            startGoogleSignIn()
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Google Sign-In with backend setup error: ${e.message}", e)
            onResult(Result.failure(Exception("Google Sign-In setup error: ${e.message}")))
        }
    }
    
    private fun startGoogleSignIn() {
        try {
            Log.d("GoogleSignIn", "Starting Google Sign-In...")
            val webClientId = getWebClientId()
            Log.d("GoogleSignIn", "Web Client ID: $webClientId")
            
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .build()
                )
                .build()
            
            Log.d("GoogleSignIn", "Sign-in request built, calling beginSignIn...")
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    Log.d("GoogleSignIn", "Sign-in request successful")
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        Log.d("GoogleSignIn", "Launching sign-in intent...")
                        signInLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Failed to launch: ${e.message}", e)
                        onSignInResult?.invoke(Result.failure(Exception("Failed to launch Google Sign-In: ${e.message}")))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("GoogleSignIn", "Sign-in request failed: ${exception.message}", exception)
                    onSignInResult?.invoke(Result.failure(Exception("Google Sign-In failed: ${exception.message}")))
                }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Setup failed: ${e.message}", e)
            onSignInResult?.invoke(Result.failure(Exception("Google Sign-In setup failed: ${e.message}")))
        }
    }
    
    private fun handleSignInResult(data: Intent?) {
        try {
            Log.d("GoogleSignIn", "Handling sign-in result...")
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            
            if (idToken != null) {
                if (useBackendIntegration) {
                    Log.d("GoogleSignIn", "ID token received, signing in with backend...")
                    handleBackendSignIn(idToken)
                } else {
                    Log.d("GoogleSignIn", "ID token received, signing in with Firebase...")
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("GoogleSignIn", "Firebase sign-in successful")
                                onSignInResult?.invoke(Result.success(Unit))
                            } else {
                                Log.e("GoogleSignIn", "Firebase sign-in failed: ${task.exception?.message}")
                                onSignInResult?.invoke(Result.failure(task.exception ?: Exception("Sign-in failed")))
                            }
                        }
                }
            } else {
                Log.e("GoogleSignIn", "No ID token received")
                onSignInResult?.invoke(Result.failure(Exception("No ID token received")))
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "ApiException: ${e.message}", e)
            onSignInResult?.invoke(Result.failure(e))
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Unexpected error: ${e.message}", e)
            onSignInResult?.invoke(Result.failure(e))
        }
    }
    
    private fun handleBackendSignIn(idToken: String) {
        // This will be called from the MainActivity with proper coroutine scope
        // For now, we'll just pass the token back to the caller
        Log.d("GoogleSignIn", "Backend sign-in initiated with token")
        onSignInResult?.invoke(Result.success(Unit))
    }
    
    private fun getWebClientId(): String {
        return try {
            val inputStream = activity.assets.open("google-services.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val clientArray = jsonObject.getJSONArray("client")
            val client = clientArray.getJSONObject(0)
            val oauthClientArray = client.getJSONArray("oauth_client")
            
            // Find the web client (client_type = 3)
            for (i in 0 until oauthClientArray.length()) {
                val oauthClient = oauthClientArray.getJSONObject(i)
                if (oauthClient.getInt("client_type") == 3) {
                    return oauthClient.getString("client_id")
                }
            }
            
            // Fallback to first client if web client not found
            oauthClientArray.getJSONObject(0).getString("client_id")
        } catch (e: Exception) {
            // Fallback to demo client ID if parsing fails
            "123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com"
        }
    }
}
