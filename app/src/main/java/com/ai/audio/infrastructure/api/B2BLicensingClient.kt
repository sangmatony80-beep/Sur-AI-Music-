package com.ai.audio.infrastructure.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// রিকোয়েস্ট এবং রেসপন্স ডেটা মডেল
data class LicensingRequest(val projectId: String, val assetId: String, val usageType: String)
data class LicensingResponse(val licenseKey: String, val isApproved: Boolean, val downloadUrl: String)

interface B2BSyncLicensingApiService {
    @POST("v1/enterprise/sync/license")
    suspend fun requestSyncLicense(
        @Header("Authorization") bearerToken: String,
        @Header("X-Enterprise-Client-Id") clientId: String,
        @Body request: LicensingRequest
    ): Response<LicensingResponse>
}

class B2BLicensingClient(private val baseUrl: String) {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val apiService: B2BSyncLicensingApiService by lazy {
        retrofit.create(B2BSyncLicensingApiService::class.java)
    }
    
    // অটোমেটেড এপিআই কল এক্সিকিউশন
    suspend fun executeB2BLicensing(
        token: String, 
        clientId: String, 
        projectId: String, 
        assetId: String
    ): LicensingResponse? {
        val request = LicensingRequest(projectId, assetId, "HOLLYWOOD_AAA_GAME_SYNC")
        return try {
            val response = apiService.requestSyncLicense("Bearer $token", clientId, request)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
