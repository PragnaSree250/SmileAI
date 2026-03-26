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
    val risk_analysis: String? = null,
    val aesthetic_prognosis: String? = null,
    val placement_strategy: String? = null,
    val caries_status: String? = null,
    val hypodontia_status: String? = null,
    val discoloration_status: String? = null,
    val gum_inflammation_status: String? = null,
    val calculus_status: String? = null,
    val redness_analysis: String? = null,
    val aesthetic_symmetry: String? = null,
    val golden_ratio: String? = null,
    val photo_url: String? = null
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
    val dentist_id: String? = null,
    val profile_photo: String? = null
)

data class Case(
    val id: Int? = null,
    val dentist_id: Int? = null,
    val patient_id: String? = null,
    val patient_first_name: String? = null,
    val patient_last_name: String? = null,
    val patient_dob: String? = null,
    val patient_gender: String? = null,
    val tooth_numbers: String? = null,
    val condition: String? = null,
    val scan_id: String? = null,
    val restoration_type: String? = null,
    val material: String? = null,
    val shade: String? = null,
    val status: String? = null,
    val patient_name: String? = null,
    val dentist_name: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val ai_deficiency: String? = null,
    val ai_report: String? = null,
    val ai_score: Int? = null,
    val ai_grade: String? = null,
    val ai_recommendation: String? = null,
    val ai_symmetry: String? = null,
    val caries_status: String? = null,
    val calculus_status: String? = null,
    val gum_inflammation_status: String? = null,
    val discoloration_status: String? = null,
    val hypodontia_status: String? = null,
    val golden_ratio: String? = null,
    val suggested_restoration: String? = null,
    val suggested_material: String? = null,
    val intercanine_width: String? = null,
    val incisor_length: String? = null,
    val abutment_health: String? = null,
    val gingival_architecture: String? = null
)

data class Medication(
    val id: Int? = null,
    val case_id: Int,
    val name: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val notes: String? = null,
    val created_at: String? = null
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
    val deficiency_addressed: String? = null,
    val ai_reasoning: String? = null,
    val final_recommendation: String? = null,
    val risk_analysis: String? = null,
    val aesthetic_prognosis: String? = null,
    val placement_strategy: String? = null,
    val hyperdontia_status: String? = null,
    val aesthetic_symmetry: String? = null,
    val golden_ratio: String? = null,
    val missing_teeth_status: String? = null,
    val caries_status: String? = null,
    val discoloration_status: String? = null,
    val gum_inflammation_status: String? = null,
    val calculus_status: String? = null,
    val medications: String? = null,
    val care_instructions: String? = null,
    val ai_score: Int? = null,
    val patient_id: String? = null,
    val dentist_name: String? = null,
    val ai_deficiency: String? = null,
    val ai_grade: String? = null,
    val ai_recommendation: String? = null,
    val redness_analysis: String? = null,
    val suggested_restoration: String? = null,
    val suggested_material: String? = null,
    val created_at: String? = null
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

data class TimelineEvent(
    val id: Int? = null,
    val case_id: Int? = null,
    val event_title: String,
    val event_description: String,
    val event_date: String
)

data class TimelineResponse(
    val status: String,
    val timeline: List<TimelineEvent>
)

data class Appointment(
    val id: Int? = null,
    val case_id: Int,
    val patient_id: String,
    val dentist_id: Int? = null,
    val appointment_date: String,
    val appointment_day: String,
    val status: String? = "Scheduled"
)

data class AppointmentResponse(
    val status: String,
    val appointment: Appointment? = null,
    val message: String? = null
)
