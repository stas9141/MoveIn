package com.example.movein.auth

import android.util.Log
import android.util.Patterns
import java.util.regex.Pattern

/**
 * Comprehensive input validation manager for authentication forms
 * Provides validation for email, password, names, phone numbers, and more
 */
object ValidationManager {
    
    private const val TAG = "ValidationManager"
    
    // Password requirements
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 128
    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"
    )
    
    // Name requirements
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 50
    private val NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]+$")
    
    // Phone requirements
    private val PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$")
    
    // Username requirements (if needed)
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 30
    private val USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$")
    
    /**
     * Validation result data class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val strength: PasswordStrength? = null
    )
    
    /**
     * Password strength enumeration
     */
    enum class PasswordStrength {
        EMPTY, WEAK, MEDIUM, STRONG, VERY_STRONG
    }
    
    /**
     * Validate email address
     */
    fun validateEmail(email: String): ValidationResult {
        Log.d(TAG, "Validating email: ${email.take(3)}***")
        
        return when {
            email.isBlank() -> {
                Log.d(TAG, "Email validation failed: empty")
                ValidationResult(false, "Email is required")
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Log.d(TAG, "Email validation failed: invalid format")
                ValidationResult(false, "Please enter a valid email address")
            }
            email.length > 254 -> {
                Log.d(TAG, "Email validation failed: too long")
                ValidationResult(false, "Email address is too long")
            }
            email.contains("..") -> {
                Log.d(TAG, "Email validation failed: consecutive dots")
                ValidationResult(false, "Email address cannot contain consecutive dots")
            }
            email.startsWith(".") || email.endsWith(".") -> {
                Log.d(TAG, "Email validation failed: starts/ends with dot")
                ValidationResult(false, "Email address cannot start or end with a dot")
            }
            else -> {
                Log.d(TAG, "Email validation passed")
                ValidationResult(true)
            }
        }
    }
    
    /**
     * Validate password strength
     */
    fun validatePassword(password: String): ValidationResult {
        Log.d(TAG, "Validating password strength")
        
        val strength = getPasswordStrength(password)
        
        return when {
            password.isBlank() -> {
                Log.d(TAG, "Password validation failed: empty")
                ValidationResult(false, "Password is required", PasswordStrength.EMPTY)
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                Log.d(TAG, "Password validation failed: too short")
                ValidationResult(false, "Password must be at least $MIN_PASSWORD_LENGTH characters", strength)
            }
            password.length > MAX_PASSWORD_LENGTH -> {
                Log.d(TAG, "Password validation failed: too long")
                ValidationResult(false, "Password is too long", strength)
            }
            !PASSWORD_PATTERN.matcher(password).matches() -> {
                Log.d(TAG, "Password validation failed: doesn't meet requirements")
                ValidationResult(false, "Password must contain uppercase, lowercase, number, and special character", strength)
            }
            else -> {
                Log.d(TAG, "Password validation passed with strength: $strength")
                ValidationResult(true, strength = strength)
            }
        }
    }
    
    /**
     * Validate name fields (first name, last name)
     */
    fun validateName(name: String, fieldName: String): ValidationResult {
        Log.d(TAG, "Validating $fieldName: ${name.take(2)}***")
        
        return when {
            name.isBlank() -> {
                Log.d(TAG, "$fieldName validation failed: empty")
                ValidationResult(false, "$fieldName is required")
            }
            name.length < MIN_NAME_LENGTH -> {
                Log.d(TAG, "$fieldName validation failed: too short")
                ValidationResult(false, "$fieldName must be at least $MIN_NAME_LENGTH characters")
            }
            name.length > MAX_NAME_LENGTH -> {
                Log.d(TAG, "$fieldName validation failed: too long")
                ValidationResult(false, "$fieldName is too long")
            }
            !NAME_PATTERN.matcher(name).matches() -> {
                Log.d(TAG, "$fieldName validation failed: invalid characters")
                ValidationResult(false, "$fieldName contains invalid characters")
            }
            name.trim() != name -> {
                Log.d(TAG, "$fieldName validation failed: leading/trailing spaces")
                ValidationResult(false, "$fieldName cannot have leading or trailing spaces")
            }
            else -> {
                Log.d(TAG, "$fieldName validation passed")
                ValidationResult(true)
            }
        }
    }
    
    /**
     * Validate phone number
     */
    fun validatePhone(phone: String): ValidationResult {
        Log.d(TAG, "Validating phone: ${phone.take(3)}***")
        
        return when {
            phone.isBlank() -> {
                Log.d(TAG, "Phone validation passed: empty (optional)")
                ValidationResult(true) // Phone is optional
            }
            !PHONE_PATTERN.matcher(phone).matches() -> {
                Log.d(TAG, "Phone validation failed: invalid format")
                ValidationResult(false, "Please enter a valid phone number")
            }
            phone.length < 10 -> {
                Log.d(TAG, "Phone validation failed: too short")
                ValidationResult(false, "Phone number is too short")
            }
            phone.length > 15 -> {
                Log.d(TAG, "Phone validation failed: too long")
                ValidationResult(false, "Phone number is too long")
            }
            else -> {
                Log.d(TAG, "Phone validation passed")
                ValidationResult(true)
            }
        }
    }
    
    /**
     * Validate username (if needed for your app)
     */
    fun validateUsername(username: String): ValidationResult {
        Log.d(TAG, "Validating username: $username")
        
        return when {
            username.isBlank() -> {
                Log.d(TAG, "Username validation failed: empty")
                ValidationResult(false, "Username is required")
            }
            username.length < MIN_USERNAME_LENGTH -> {
                Log.d(TAG, "Username validation failed: too short")
                ValidationResult(false, "Username must be at least $MIN_USERNAME_LENGTH characters")
            }
            username.length > MAX_USERNAME_LENGTH -> {
                Log.d(TAG, "Username validation failed: too long")
                ValidationResult(false, "Username is too long")
            }
            !USERNAME_PATTERN.matcher(username).matches() -> {
                Log.d(TAG, "Username validation failed: invalid characters")
                ValidationResult(false, "Username can only contain letters, numbers, dots, underscores, and hyphens")
            }
            username.startsWith(".") || username.endsWith(".") -> {
                Log.d(TAG, "Username validation failed: starts/ends with dot")
                ValidationResult(false, "Username cannot start or end with a dot")
            }
            else -> {
                Log.d(TAG, "Username validation passed")
                ValidationResult(true)
            }
        }
    }
    
    /**
     * Validate password confirmation
     */
    fun validatePasswordConfirmation(password: String, confirmPassword: String): ValidationResult {
        Log.d(TAG, "Validating password confirmation")
        
        return when {
            confirmPassword.isBlank() -> {
                Log.d(TAG, "Password confirmation validation failed: empty")
                ValidationResult(false, "Please confirm your password")
            }
            password != confirmPassword -> {
                Log.d(TAG, "Password confirmation validation failed: mismatch")
                ValidationResult(false, "Passwords do not match")
            }
            else -> {
                Log.d(TAG, "Password confirmation validation passed")
                ValidationResult(true)
            }
        }
    }
    
    /**
     * Get password strength indicator
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.isBlank()) return PasswordStrength.EMPTY
        
        var score = 0
        var feedback = mutableListOf<String>()
        
        // Length check
        if (password.length >= 8) {
            score++
        } else {
            feedback.add("at least 8 characters")
        }
        
        // Lowercase check
        if (password.any { it.isLowerCase() }) {
            score++
        } else {
            feedback.add("lowercase letter")
        }
        
        // Uppercase check
        if (password.any { it.isUpperCase() }) {
            score++
        } else {
            feedback.add("uppercase letter")
        }
        
        // Number check
        if (password.any { it.isDigit() }) {
            score++
        } else {
            feedback.add("number")
        }
        
        // Special character check
        if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) {
            score++
        } else {
            feedback.add("special character")
        }
        
        // Length bonus
        if (password.length >= 12) score++
        if (password.length >= 16) score++
        
        // Complexity bonus
        if (password.count { it.isLetter() } >= 4) score++
        if (password.count { it.isDigit() } >= 2) score++
        
        return when (score) {
            0, 1 -> PasswordStrength.WEAK
            2, 3 -> PasswordStrength.MEDIUM
            4, 5 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }
    
    /**
     * Get password strength feedback
     */
    fun getPasswordStrengthFeedback(password: String): String {
        val strength = getPasswordStrength(password)
        
        return when (strength) {
            PasswordStrength.EMPTY -> "Enter a password"
            PasswordStrength.WEAK -> "Weak password"
            PasswordStrength.MEDIUM -> "Medium strength password"
            PasswordStrength.STRONG -> "Strong password"
            PasswordStrength.VERY_STRONG -> "Very strong password"
        }
    }
    
    /**
     * Get password requirements as a list
     */
    fun getPasswordRequirements(): List<String> {
        return listOf(
            "At least 8 characters",
            "One uppercase letter",
            "One lowercase letter",
            "One number",
            "One special character (!@#$%^&*()_+-=[]{}|;:,.<>?)"
        )
    }

    
    /**
     * Validate all signup fields at once
     */
    fun validateSignupData(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        phoneNumber: String? = null
    ): Map<String, ValidationResult> {
        Log.d(TAG, "Validating complete signup data")
        
        return mapOf(
            "email" to validateEmail(email),
            "password" to validatePassword(password),
            "confirmPassword" to validatePasswordConfirmation(password, confirmPassword),
            "firstName" to validateName(firstName, "First name"),
            "lastName" to validateName(lastName, "Last name"),
            "phoneNumber" to validatePhone(phoneNumber ?: "")
        )
    }
    
    /**
     * Validate all login fields at once
     */
    fun validateLoginData(email: String, password: String): Map<String, ValidationResult> {
        Log.d(TAG, "Validating complete login data")
        
        return mapOf(
            "email" to validateEmail(email),
            "password" to ValidationResult(password.isNotBlank(), if (password.isBlank()) "Password is required" else null)
        )
    }
    
    /**
     * Check if all validation results are valid
     */
    fun areAllValid(validationResults: Map<String, ValidationResult>): Boolean {
        return validationResults.values.all { it.isValid }
    }
    
    /**
     * Get first error message from validation results
     */
    fun getFirstError(validationResults: Map<String, ValidationResult>): String? {
        return validationResults.values.firstOrNull { !it.isValid }?.errorMessage
    }
    
    /**
     * Get all error messages from validation results
     */
    fun getAllErrors(validationResults: Map<String, ValidationResult>): List<String> {
        return validationResults.values
            .filter { !it.isValid }
            .mapNotNull { it.errorMessage }
    }
}
