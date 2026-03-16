package com.simats.smileai.network

data class ApiResponse(
    val status: String,
    val message: String,
    val user: User? = null,
    val case_id: Int? = null,
    val access_token: String? = null,
    val ai_deficiency: String? = null,
    val ai_report: String? = null,
    val ai_score: Int? = null,
    val ai_grade: String? = null,
    val ai_recommendation: String? = null,
    val phone: String? = null,
    val clinic_address: String? = null,
    val specialization: String? = null,
    val plan_type: String? = null,
    val patient_id: String? = null,
    val suggested_restoration: String? = null,
    val suggested_material: String? = null,
    val active_cases: Int? = null,
    val total_reports: Int? = null,
    val unread_notifications: Int? = null,
    val caries_status: String? = null,
    val gum_inflammation_status: String? = null,
    val aesthetic_symmetry: String? = null
)

data class User(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String? = "dentist",
    val phone: String? = null,
    val clinic_address: String? = null,
    val specialization: String? = null,
    val plan_type: String? = null,
    val patient_id: String? = null,
    val dentist_id: String? = null
)

data class Case(
    val id: Int? = null,
    val dentist_id: Int? = null,
    val patient_first_name: String,
    val patient_last_name: String,
    val patient_dob: String? = null,
    val patient_gender: String? = null,
    val tooth_numbers: String? = null,
    val condition: String? = null,
    val scan_id: String? = null,
    val restoration_type: String? = null,
    val material: String? = null,
    val shade: String? = null,
    val ai_deficiency: String? = null,
    val ai_report: String? = null,
    val ai_score: Int? = null,
    val ai_grade: String? = null,
    val ai_recommendation: String? = null,
    val face_photo_path: String? = null,
    val intra_photo_path: String? = null,
    val patient_id: String? = null,
    val suggested_restoration: String? = null,
    val suggested_material: String? = null,
    val status: String? = "Active",
    val created_at: String? = null,
    val dentist_name: String? = null
)

data class Medication(
    val id: Int? = null,
    val case_id: Int,
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val notes: String? = null
)

data class CareTip(
    val id: Int? = null,
    val case_id: Int,
    val tip_text: String,
    val is_positive: Boolean
)

data class Report(
    val id: Int? = null,
    val case_id: Int,
    val deficiency_addressed: String,
    val ai_reasoning: String,
    val final_recommendation: String,
    val risk_analysis: String? = null,
    val aesthetic_prognosis: String? = null,
    val placement_strategy: String? = null,
    val hyperdontia_status: String? = null,
    val aesthetic_symmetry: String? = null,
    val golden_ratio: String? = null,
    val missing_teeth_status: String? = null,
    val medications: String? = null,
    val care_instructions: String? = null,
    val patient_id: String? = null,
    val dentist_name: String? = null
)

data class Notification(
    val id: Int,
    val user_id: Int,
    val title: String,
    val message: String,
    val is_read: Boolean,
    val created_at: String
)

data class CaseFile(
    val id: Int,
    val case_id: Int,
    val file_path: String,
    val file_type: String,
    val uploaded_at: String
)
