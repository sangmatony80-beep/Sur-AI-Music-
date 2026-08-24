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
        
        // Get configured client ID or use standard OAuth client request
        val serverClientId = try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.gms.auth.api.credentials.SERVER_CLIENT_ID")
                ?: "default-web-client-id"
        } catch (_: Exception) {
            "default-web-client-id"
        }

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
            Log.w("GoogleAuthManager", "Google sign-in credential exception: ${e.message}")
        } catch (e: Exception) {
            Log.w("GoogleAuthManager", "Google sign-in exception: ${e.message}")
        }
        return@withContext null
    }
}
