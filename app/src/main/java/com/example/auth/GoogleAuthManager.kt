package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthManager(private val context: Context) {

    suspend fun getGoogleIdToken(): GoogleIdTokenCredential? = withContext(Dispatchers.Main) {
        val credentialManager = CredentialManager.create(context)
        
        // This is a placeholder Web Client ID. In a real app, use the actual Server Client ID.
        val serverClientId = "999999999999-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                return@withContext credential
            } else if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return@withContext GoogleIdTokenCredential.createFrom(credential.data)
            }
        } catch (e: GetCredentialException) {
            Log.w("GoogleAuthManager", "Google sign-in unavailable on this device/emulator: ${e.message}")
        }
        return@withContext null
    }
}
