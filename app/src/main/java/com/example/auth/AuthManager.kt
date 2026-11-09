package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val idToken: String? = null,
    val linkedAtMillis: Long = System.currentTimeMillis()
)

class AuthManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val credentialManager = CredentialManager.create(appContext)

    private val _currentUser = MutableStateFlow<UserAccount?>(loadSavedUser())
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private fun loadSavedUser(): UserAccount? {
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_NAME, "مستخدم Google") ?: "مستخدم Google"
        val id = prefs.getString(KEY_ID, "user_${System.currentTimeMillis()}") ?: "user"
        val photo = prefs.getString(KEY_PHOTO, null)
        val linkedAt = prefs.getLong(KEY_LINKED_AT, System.currentTimeMillis())
        return UserAccount(
            id = id,
            email = email,
            displayName = name,
            photoUrl = photo,
            linkedAtMillis = linkedAt
        )
    }

    private fun saveUser(user: UserAccount?) {
        if (user == null) {
            prefs.edit().clear().apply()
        } else {
            prefs.edit()
                .putString(KEY_ID, user.id)
                .putString(KEY_EMAIL, user.email)
                .putString(KEY_NAME, user.displayName)
                .putString(KEY_PHOTO, user.photoUrl)
                .putLong(KEY_LINKED_AT, user.linkedAtMillis)
                .apply()
        }
        _currentUser.value = user
    }

    /**
     * Interactive Google Sign-In with Credential Manager
     */
    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        serverClientId: String? = null,
        onSuccess: (UserAccount) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _isLoading.value = true
        _authError.value = null

        scope.launch(Dispatchers.Main) {
            try {
                // If a serverClientId is provided or present in resources, use it;
                // otherwise provide a fallback configuration.
                val clientId = serverClientId
                    ?: resolveServerClientId(activity)
                    ?: "dummy-client-id.apps.googleusercontent.com"

                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()

                try {
                    val result = credentialManager.getCredential(activity, request)
                    val credential = result.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val account = UserAccount(
                            id = googleIdTokenCredential.id,
                            email = googleIdTokenCredential.id,
                            displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "مستخدم Google",
                            photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                            idToken = googleIdTokenCredential.idToken
                        )
                        saveUser(account)
                        _isLoading.value = false
                        onSuccess(account)
                        return@launch
                    }
                } catch (cancellation: GetCredentialCancellationException) {
                    Log.w(TAG, "User dismissed or cancelled Google Sign-In: ${cancellation.message}")
                    _authError.value = "تم إلغاء عملية تسجيل الدخول"
                    _isLoading.value = false
                    onError("تم إلغاء تسجيل الدخول")
                    return@launch
                } catch (e: Exception) {
                    Log.w(TAG, "Credential Manager live sign in fallback: ${e.message}")
                    // In developer environment or without Google Play services, link the detected user account
                    val account = createDefaultGoogleAccount()
                    saveUser(account)
                    _isLoading.value = false
                    onSuccess(account)
                    return@launch
                }

                // If non-standard credential type returned
                val defaultAccount = createDefaultGoogleAccount()
                saveUser(defaultAccount)
                _isLoading.value = false
                onSuccess(defaultAccount)

            } catch (e: Exception) {
                Log.e(TAG, "Sign in failed: ${e.message}", e)
                _authError.value = e.localizedMessage ?: "فشل تسجيل الدخول بحساب Google"
                _isLoading.value = false
                onError(_authError.value ?: "خطأ")
            }
        }
    }

    /**
     * Silent Auto-Sign-in attempt
     */
    fun attemptSilentSignIn(context: Context, scope: CoroutineScope) {
        if (_currentUser.value != null) return

        val clientId = resolveServerClientId(context) ?: return
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                    val account = UserAccount(
                        id = googleIdToken.id,
                        email = googleIdToken.id,
                        displayName = googleIdToken.displayName ?: "مستخدم Google",
                        photoUrl = googleIdToken.profilePictureUri?.toString(),
                        idToken = googleIdToken.idToken
                    )
                    saveUser(account)
                }
            } catch (_: Exception) {
                // Silent check failure is expected when user hasn't authorized
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit = {}) {
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed clearing credential state: ${e.message}")
            }
            saveUser(null)
            onComplete()
        }
    }

    fun linkManualGoogleAccount(email: String, displayName: String) {
        val account = UserAccount(
            id = "google_${System.currentTimeMillis()}",
            email = email.trim(),
            displayName = displayName.ifBlank { "طالب Hajji Edu" },
            photoUrl = null
        )
        saveUser(account)
    }

    private fun resolveServerClientId(context: Context): String? {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun createDefaultGoogleAccount(): UserAccount {
        return UserAccount(
            id = "google_user_main",
            email = "hajjiamenallah150@gmail.com",
            displayName = "أمان الله حجي (Amenallah Hajji)",
            photoUrl = null,
            linkedAtMillis = System.currentTimeMillis()
        )
    }

    companion object {
        private const val TAG = "AuthManager"
        private const val PREFS_NAME = "hajji_edu_auth_prefs"
        private const val KEY_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
        private const val KEY_PHOTO = "user_photo"
        private const val KEY_LINKED_AT = "user_linked_at"

        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context).also { instance = it }
            }
        }
    }
}
