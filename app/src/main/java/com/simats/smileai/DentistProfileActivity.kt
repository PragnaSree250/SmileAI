package com.simats.smileai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.simats.smileai.network.ApiResponse
import com.simats.smileai.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class DentistProfileActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etClinicAddress: EditText
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileSpecialization: TextView
    private lateinit var tvDentistId: TextView
    private lateinit var ivProfilePhoto: ImageView
    private var accessToken: String = ""
    private var tempImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { uploadPhoto(it) }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { uploadPhoto(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_profile)

        // Initialize Views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etClinicAddress = findViewById(R.id.etClinicAddress)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileSpecialization = findViewById(R.id.tvProfileSpecialization)
        tvDentistId = findViewById(R.id.tvDentistId)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnUploadPhoto = findViewById<ImageButton>(R.id.btnUploadPhoto)
        val btnSaveChanges = findViewById<Button>(R.id.btnSaveChanges)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)
        val btnNotifications = findViewById<ImageView>(R.id.btnNotifications)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)

        // Retrieve Token
        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        accessToken = sharedPref.getString("access_token", "") ?: ""
        if (accessToken.isNotEmpty()) {
            RetrofitClient.authToken = accessToken
        }

        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load Data
        loadProfileData()

        btnBack.setOnClickListener {
            finish()
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, DentistNotificationsActivity::class.java))
        }

        btnMenu.setOnClickListener {
            startActivity(Intent(this, DentistMenuBarActivity::class.java))
        }

        btnUploadPhoto.setOnClickListener {
            showImagePickerDialog()
        }

        btnSaveChanges.setOnClickListener {
            saveProfileData()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun loadProfileData() {
        RetrofitClient.instance.getProfile().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val user = response.body()?.user
                    user?.let {
                        val names = it.full_name.split(" ")
                        etFirstName.setText(names.getOrNull(0) ?: "")
                        etLastName.setText(names.drop(1).joinToString(" "))
                        etEmail.setText(it.email)
                        etPhone.setText(it.phone ?: "")
                        etClinicAddress.setText(it.clinic_address ?: "")
                        
                        tvProfileName.text = it.full_name
                        tvProfileSpecialization.text = it.specialization ?: "General Dentist"

                        // Extract and formulate dentist clinical ID
                        val rawId = it.dentist_id ?: "d-00${it.id}"
                        val formattedId = if (rawId.startsWith("d", ignoreCase = true) && !rawId.contains("-")) {
                            "d-" + rawId.substring(1)
                        } else {
                            rawId
                        }
                        tvDentistId.text = "Clinical ID: $formattedId"

                        // Load profile photo if exists
                        it.profile_photo?.let { photoUrl ->
                            Glide.with(this@DentistProfileActivity)
                                .load(RetrofitClient.BASE_URL + photoUrl)
                                .circleCrop()
                                .into(ivProfilePhoto)
                        }

                        // Update stored name in case it changed
                        getSharedPreferences("SmileAI", MODE_PRIVATE).edit()
                            .putString("user_name", it.full_name)
                            .apply()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    android.util.Log.e("DentistProfile", "Failed to load profile: ${response.code()} - $errorBody")
                    Toast.makeText(this@DentistProfileActivity, "Failed to load profile (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileData() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val fullName = "$firstName $lastName"
        val phone = etPhone.text.toString().trim()
        val address = etClinicAddress.text.toString().trim()

        val updateData = mutableMapOf(
            "full_name" to fullName,
            "phone" to phone,
            "clinic_address" to address
        )
        
        // Also capture specialization from TV if it was changed/loaded
        val spec = tvProfileSpecialization.text.toString()
        if (spec.isNotEmpty() && spec != "General Dentist") {
            updateData["specialization"] = spec
        }

        RetrofitClient.instance.updateProfile(updateData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@DentistProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    tvProfileName.text = fullName
                    
                    // Update stored name
                    getSharedPreferences("SmileAI", MODE_PRIVATE).edit()
                        .putString("user_name", fullName)
                        .apply()

                    finish()
                } else {
                    Toast.makeText(this@DentistProfileActivity, "Update Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DentistProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteAccountConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you absolutely sure you want to delete your account? This action is permanent and all your data will be lost.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        val imageFile = File(cacheDir, "temp_profile_image.jpg")
        tempImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
        tempImageUri?.let { takePictureLauncher.launch(it) }
    }

    private fun uploadPhoto(uri: Uri) {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Uploading photo...")
            setCancelable(false)
            show()
        }

        val file = File(cacheDir, "profile_upload.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.instance.uploadProfilePhoto(body).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@DentistProfileActivity, "Photo updated!", Toast.LENGTH_SHORT).show()
                    val photoUrl = response.body()?.photo_url
                    if (photoUrl != null) {
                        Glide.with(this@DentistProfileActivity)
                            .load(RetrofitClient.BASE_URL + photoUrl)
                            .circleCrop()
                            .into(ivProfilePhoto)
                    }
                } else {
                    Toast.makeText(this@DentistProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@DentistProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteAccount() {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Deleting account...")
            setCancelable(false)
            show()
        }

        RetrofitClient.instance.deleteProfile().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@DentistProfileActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    
                    // Clear session and logout
                    val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
                    sharedPref.edit().clear().apply()
                    RetrofitClient.authToken = null
                    
                    val intent = Intent(this@DentistProfileActivity, SmartLoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to delete account"
                    Toast.makeText(this@DentistProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@DentistProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
