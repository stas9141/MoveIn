package com.example.movein.shared.cloud

import android.content.Context
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class CloudStorage(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow(AuthState(false))
    private val _syncStatus = MutableStateFlow(SyncStatus())
    
    private var userDataListener: ListenerRegistration? = null
    private var checklistDataListener: ListenerRegistration? = null
    private var defectsListener: ListenerRegistration? = null
    
    actual val authState: Flow<AuthState> = _authState.asStateFlow()
    actual val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()
    
    init {
        // Listen to auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _authState.value = AuthState(
                isAuthenticated = user != null,
                userId = user?.uid,
                email = user?.email
            )
        }
    }
    
    actual suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            Result.failure(e)
        }
    }
    
    actual suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            Result.failure(e)
        }
    }
    
    actual suspend fun signInWithGoogle(): Result<Unit> {
        // Google Sign-In is handled by GoogleSignInHelper in the app module
        return Result.failure(Exception("Google Sign-In handled by GoogleSignInHelper"))
    }
    
    actual suspend fun signOut(): Result<Unit> {
        return try {
            disableRealtimeSync()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    actual suspend fun saveUserData(userData: UserData): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            
            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()
            
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncTime = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = false, error = e.message)
            Result.failure(e)
        }
    }
    
    actual suspend fun loadUserData(): Result<UserData?> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val userData = document.toObject(UserData::class.java)
                Result.success(userData)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual suspend fun saveChecklistData(checklistData: ChecklistData): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            
            firestore.collection("users")
                .document(userId)
                .collection("data")
                .document("checklist")
                .set(checklistData)
                .await()
            
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncTime = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = false, error = e.message)
            Result.failure(e)
        }
    }
    
    actual suspend fun loadChecklistData(): Result<ChecklistData?> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val document = firestore.collection("users")
                .document(userId)
                .collection("data")
                .document("checklist")
                .get()
                .await()
            
            if (document.exists()) {
                val checklistData = document.toObject(ChecklistData::class.java)
                Result.success(checklistData)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual suspend fun saveDefects(defects: List<Defect>): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            
            firestore.collection("users")
                .document(userId)
                .collection("data")
                .document("defects")
                .set(mapOf("defects" to defects))
                .await()
            
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncTime = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = false, error = e.message)
            Result.failure(e)
        }
    }
    
    actual suspend fun loadDefects(): Result<List<Defect>> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val document = firestore.collection("users")
                .document(userId)
                .collection("data")
                .document("defects")
                .get()
                .await()
            
            if (document.exists()) {
                val defects = document.get("defects") as? List<Defect> ?: emptyList()
                Result.success(defects)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual suspend fun enableRealtimeSync(): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Set up real-time listeners for data changes
            userDataListener = firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = _syncStatus.value.copy(error = error.message)
                    } else if (snapshot != null && snapshot.exists()) {
                        // Data changed, could trigger local update
                        _syncStatus.value = _syncStatus.value.copy(lastSyncTime = System.currentTimeMillis())
                    }
                }
            
            checklistDataListener = firestore.collection("users")
                .document(userId)
                .collection("data")
                .document("checklist")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = _syncStatus.value.copy(error = error.message)
                    } else if (snapshot != null && snapshot.exists()) {
                        _syncStatus.value = _syncStatus.value.copy(lastSyncTime = System.currentTimeMillis())
                    }
                }
            
            defectsListener = firestore.collection("users")
                .document(userId)
                .collection("data")
                .document("defects")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = _syncStatus.value.copy(error = error.message)
                    } else if (snapshot != null && snapshot.exists()) {
                        _syncStatus.value = _syncStatus.value.copy(lastSyncTime = System.currentTimeMillis())
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual suspend fun disableRealtimeSync(): Result<Unit> {
        return try {
            userDataListener?.remove()
            checklistDataListener?.remove()
            defectsListener?.remove()
            userDataListener = null
            checklistDataListener = null
            defectsListener = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual suspend fun forceSync(): Result<Unit> {
        return try {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            
            // Force sync all data
            val userDataResult = loadUserData()
            val checklistDataResult = loadChecklistData()
            val defectsResult = loadDefects()
            
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncTime = System.currentTimeMillis()
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = false, error = e.message)
            Result.failure(e)
        }
    }
}
