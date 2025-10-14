package com.example.movein.auth

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Biometric authentication manager for fingerprint and face unlock
 */
class BiometricAuthManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BiometricAuthManager"
    }
    
    private val biometricManager = BiometricManager.from(context)
    
    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): Boolean {
        val availability = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        val isAvailable = availability == BiometricManager.BIOMETRIC_SUCCESS
        
        Log.d(TAG, "Biometric availability: $isAvailable (code: $availability)")
        return isAvailable
    }
    
    /**
     * Check if biometric authentication is available with detailed status
     */
    fun getBiometricAvailabilityStatus(): BiometricAvailabilityStatus {
        val availability = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        
        return when (availability) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometric authentication is available")
                BiometricAvailabilityStatus.AVAILABLE
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d(TAG, "No biometric hardware available")
                BiometricAvailabilityStatus.NO_HARDWARE
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(TAG, "Biometric hardware is unavailable")
                BiometricAvailabilityStatus.HARDWARE_UNAVAILABLE
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(TAG, "No biometrics enrolled")
                BiometricAvailabilityStatus.NONE_ENROLLED
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.d(TAG, "Security update required for biometrics")
                BiometricAvailabilityStatus.SECURITY_UPDATE_REQUIRED
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.d(TAG, "Biometric authentication is unsupported")
                BiometricAvailabilityStatus.UNSUPPORTED
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.d(TAG, "Biometric status is unknown")
                BiometricAvailabilityStatus.UNKNOWN
            }
            else -> {
                Log.d(TAG, "Unknown biometric availability status: $availability")
                BiometricAvailabilityStatus.UNKNOWN
            }
        }
    }
    
    /**
     * Authenticate using biometrics
     */
    suspend fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Use your biometric to authenticate",
        negativeButtonText: String = "Cancel"
    ): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            Log.d(TAG, "Starting biometric authentication")
            
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Biometric authentication error: $errorCode - $errString")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_CANCELED -> {
                            Log.d(TAG, "Biometric authentication canceled by user")
                            continuation.resume(Result.failure(Exception("Authentication canceled")))
                        }
                        BiometricPrompt.ERROR_LOCKOUT -> {
                            Log.e(TAG, "Biometric authentication locked out")
                            continuation.resume(Result.failure(Exception("Too many failed attempts. Please try again later.")))
                        }
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            Log.e(TAG, "Biometric authentication permanently locked out")
                            continuation.resume(Result.failure(Exception("Biometric authentication is permanently disabled. Please use your password.")))
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            Log.e(TAG, "No biometrics enrolled")
                            continuation.resume(Result.failure(Exception("No biometrics enrolled. Please set up biometric authentication in your device settings.")))
                        }
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                            Log.e(TAG, "No biometric hardware present")
                            continuation.resume(Result.failure(Exception("No biometric hardware available on this device.")))
                        }
                        BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                            Log.e(TAG, "Biometric hardware unavailable")
                            continuation.resume(Result.failure(Exception("Biometric hardware is currently unavailable.")))
                        }
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            Log.d(TAG, "User tapped negative button")
                            continuation.resume(Result.failure(Exception("Authentication canceled")))
                        }
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                            Log.e(TAG, "No device credential available")
                            continuation.resume(Result.failure(Exception("No device credential available. Please set up a screen lock.")))
                        }
                        BiometricPrompt.ERROR_NO_SPACE -> {
                            Log.e(TAG, "No space available for biometric authentication")
                            continuation.resume(Result.failure(Exception("No space available for biometric authentication.")))
                        }
                        BiometricPrompt.ERROR_TIMEOUT -> {
                            Log.e(TAG, "Biometric authentication timeout")
                            continuation.resume(Result.failure(Exception("Authentication timeout. Please try again.")))
                        }
                        BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> {
                            Log.e(TAG, "Unable to process biometric authentication")
                            continuation.resume(Result.failure(Exception("Unable to process biometric authentication. Please try again.")))
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            Log.d(TAG, "User canceled biometric authentication")
                            continuation.resume(Result.failure(Exception("Authentication canceled")))
                        }
                        BiometricPrompt.ERROR_VENDOR -> {
                            Log.e(TAG, "Vendor-specific biometric error")
                            continuation.resume(Result.failure(Exception("Biometric authentication error. Please try again.")))
                        }
                        else -> {
                            Log.e(TAG, "Unknown biometric error: $errorCode")
                            continuation.resume(Result.failure(Exception("Authentication failed: $errString")))
                        }
                    }
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Biometric authentication succeeded")
                    continuation.resume(Result.success(Unit))
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Biometric authentication failed")
                    continuation.resume(Result.failure(Exception("Authentication failed. Please try again.")))
                }
            })
            
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()
            
            try {
                biometricPrompt.authenticate(promptInfo)
                Log.d(TAG, "Biometric prompt launched successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch biometric prompt", e)
                continuation.resume(Result.failure(Exception("Failed to start biometric authentication: ${e.message}")))
            }
            
            continuation.invokeOnCancellation {
                Log.d(TAG, "Biometric authentication canceled")
                try {
                    biometricPrompt.cancelAuthentication()
                } catch (e: Exception) {
                    Log.e(TAG, "Error canceling biometric authentication", e)
                }
            }
        }
    }
    
    /**
     * Authenticate for login with custom messages
     */
    suspend fun authenticateForLogin(activity: FragmentActivity): Result<Unit> {
        return authenticateWithBiometrics(
            activity = activity,
            title = "Login with Biometric",
            subtitle = "Use your fingerprint or face to sign in",
            negativeButtonText = "Use Password"
        )
    }
    
    /**
     * Authenticate for sensitive operations
     */
    suspend fun authenticateForSensitiveOperation(activity: FragmentActivity): Result<Unit> {
        return authenticateWithBiometrics(
            activity = activity,
            title = "Confirm Identity",
            subtitle = "Use your biometric to confirm this action",
            negativeButtonText = "Cancel"
        )
    }
    
    /**
     * Check if device has biometric hardware
     */
    fun hasBiometricHardware(): Boolean {
        val availability = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return availability != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE && 
               availability != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
    }
    
    /**
     * Check if biometrics are enrolled
     */
    fun hasEnrolledBiometrics(): Boolean {
        val availability = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return availability == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    /**
     * Get user-friendly error message for biometric availability
     */
    fun getAvailabilityErrorMessage(): String {
        return when (getBiometricAvailabilityStatus()) {
            BiometricAvailabilityStatus.AVAILABLE -> "Biometric authentication is available"
            BiometricAvailabilityStatus.NO_HARDWARE -> "This device doesn't have biometric hardware"
            BiometricAvailabilityStatus.HARDWARE_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricAvailabilityStatus.NONE_ENROLLED -> "No biometrics enrolled. Please set up fingerprint or face unlock in device settings"
            BiometricAvailabilityStatus.SECURITY_UPDATE_REQUIRED -> "Security update required for biometric authentication"
            BiometricAvailabilityStatus.UNSUPPORTED -> "Biometric authentication is not supported on this device"
            BiometricAvailabilityStatus.UNKNOWN -> "Unable to determine biometric availability"
        }
    }
    
    /**
     * Get user-friendly setup instructions
     */
    fun getSetupInstructions(): String {
        return when (getBiometricAvailabilityStatus()) {
            BiometricAvailabilityStatus.NONE_ENROLLED -> {
                "To use biometric authentication:\n\n" +
                "1. Go to Settings > Security\n" +
                "2. Set up Fingerprint or Face unlock\n" +
                "3. Follow the on-screen instructions\n" +
                "4. Return to this app and try again"
            }
            BiometricAvailabilityStatus.NO_HARDWARE -> {
                "This device doesn't support biometric authentication. " +
                "You can still use the app with your password."
            }
            BiometricAvailabilityStatus.HARDWARE_UNAVAILABLE -> {
                "Biometric hardware is currently unavailable. " +
                "Please try again later or use your password."
            }
            else -> "Biometric authentication is ready to use."
        }
    }
}

/**
 * Biometric availability status enumeration
 */
enum class BiometricAvailabilityStatus {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NONE_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN
}

