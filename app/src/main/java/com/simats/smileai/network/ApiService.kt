package com.simats.smileai.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.PartMap

interface ApiService {

    @POST("signup")
    fun signUp(@Body request: Map<String, String>): Call<ApiResponse>

    @POST("login")
    fun login(@Body request: Map<String, String>): Call<ApiResponse>

    @POST("forgot-password")
    fun forgotPassword(@Body request: Map<String, String>): Call<ApiResponse>

    @POST("verify-otp")
    fun verifyOtp(@Body request: Map<String, String>): Call<ApiResponse>

    @POST("reset-password")
    fun resetPassword(@Body request: Map<String, String>): Call<ApiResponse>

    @POST("cases")
    fun createCase(
        @Body case: Case
    ): Call<ApiResponse>

    @Multipart
    @POST("cases/{case_id}/upload")
    fun uploadCaseFile(
        @Path("case_id") caseId: Int,
        @Part file: MultipartBody.Part,
        @Part("file_type") fileType: RequestBody
    ): Call<ApiResponse>

    @POST("reports")
    fun createReport(
        @Body report: Report
    ): Call<ApiResponse>

    @GET("cases")
    fun getDentistCases(): Call<List<Case>>

    @GET("cases/active")
    fun getActiveCases(): Call<List<Case>>

    @GET("cases/patient/{patient_id}")
    fun getPatientCases(
        @Path("patient_id") patientId: String
    ): Call<List<Case>>

    @GET("cases/patient/{patient_id}/active")
    fun getPatientActiveCases(
        @Path("patient_id") patientId: String
    ): Call<List<Case>>

    @POST("medications")
    fun addMedication(
        @Body medication: Medication
    ): Call<ApiResponse>

    @GET("medications/{case_id}")
    fun getMedications(
        @Path("case_id") caseId: Int
    ): Call<List<Medication>>

    @GET("medications/patient/{patient_id}")
    fun getPatientMedications(
        @Path("patient_id") patientId: String
    ): Call<List<Medication>>

    @GET("reports/{case_id}")
    fun getReport(
        @Path("case_id") caseId: Int
    ): Call<Report>

    @GET("notifications")
    fun getNotifications(): Call<List<Notification>>

    @PUT("notifications/{notif_id}/read")
    fun markNotificationRead(
        @Path("notif_id") notifId: Int
    ): Call<ApiResponse>

    @GET("case-files/{case_id}")
    fun getCaseFiles(
        @Path("case_id") caseId: Int
    ): Call<List<CaseFile>>

    @GET("profile")
    fun getProfile(): Call<ApiResponse>

    @GET("patient/stats")
    fun getPatientStats(): Call<ApiResponse>

    @GET("patient/profile")
    fun getPatientProfile(): Call<ApiResponse>

    @GET("dentist/profile")
    fun getDentistProfile(): Call<ApiResponse>

    @PUT("profile")
    fun updateProfile(
        @Body profileData: Map<String, String?>
    ): Call<ApiResponse>

    @PATCH("dentist/profile")
    fun updateDentistProfile(
        @Body profileData: Map<String, String?>
    ): Call<ApiResponse>

    @PATCH("patient/profile")
    fun updatePatientProfile(
        @Body profileData: Map<String, String?>
    ): Call<ApiResponse>
    
    @POST("healing-logs")
    fun addHealingLog(
        @Body log: Map<String, String>
    ): Call<ApiResponse>

    @GET("healing-logs")
    fun getHealingLogs(): Call<List<Map<String, Any>>>

    @PUT("cases/{case_id}/status")
    fun updateCaseStatus(
        @Path("case_id") caseId: Int,
        @Body statusData: Map<String, String>
    ): Call<ApiResponse>

    @GET("cases/{case_id}/analyze")
    fun analyzeCase(
        @Path("case_id") caseId: Int
    ): Call<ApiResponse>
}
