package com.simats.smileai

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DentistNewCase4Activity : ComponentActivity() {

    private var faceImageUri: Uri? = null
    private var intraImageUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var isFaceUpload: Boolean = true // Track which section is being uploaded

    private lateinit var ivPreviewFace: ImageView
    private lateinit var ivUploadIconFace: ImageView
    private lateinit var tvUploadTextFace: TextView
    private lateinit var tvSupportedFormatFace: TextView

    private lateinit var ivPreviewIntra: ImageView
    private lateinit var ivUploadIconIntra: ImageView
    private lateinit var tvUploadTextIntra: TextView

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (isFaceUpload) {
                faceImageUri = it
                updateFacePreview(it)
            } else {
                intraImageUri = it
                updateIntraPreview(it)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath ?: return@registerForActivityResult)
            val uri = Uri.fromFile(file)
            
            if (isFaceUpload) {
                faceImageUri = uri
                updateFacePreview(uri)
            } else {
                intraImageUri = uri
                updateIntraPreview(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_new_case_4)

        // Face Upload Views
        ivPreviewFace = findViewById(R.id.ivPreviewFace)
        ivUploadIconFace = findViewById(R.id.ivUploadIcon)
        tvUploadTextFace = findViewById(R.id.tvUploadText)
        tvSupportedFormatFace = findViewById(R.id.tvSupportedFormat)

        // Intraoral Upload Views
        ivPreviewIntra = findViewById(R.id.ivPreviewIntra)
        ivUploadIconIntra = findViewById(R.id.ivUploadIconIntra)
        tvUploadTextIntra = findViewById(R.id.tvUploadTextIntra)

        findViewById<LinearLayout>(R.id.layoutUploadFace).setOnClickListener {
            isFaceUpload = true
            showImagePickerOptions()
        }

        findViewById<LinearLayout>(R.id.layoutUploadIntra).setOnClickListener {
            isFaceUpload = false
            showImagePickerOptions()
        }

        setupShadeSelection()

        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnPrevious).setOnClickListener { finish() }
        
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val intent = Intent(this, DentistNewCase5Activity::class.java).apply {
                putExtras(getIntent())
                putExtra("EXTRA_SHADE", selectedShade)
                faceImageUri?.let { putExtra("EXTRA_FACE_IMAGE_URI", it.toString()) }
                intraImageUri?.let { putExtra("EXTRA_INTRA_IMAGE_URI", it.toString()) }
            }
            startActivity(intent)
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle(if (isFaceUpload) "Upload Face Photo" else "Upload Intraoral Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.simats.smileai.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    cameraLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun updateFacePreview(uri: Uri) {
        ivPreviewFace.setImageURI(uri)
        ivPreviewFace.visibility = View.VISIBLE
        ivUploadIconFace.visibility = View.GONE
        tvUploadTextFace.text = "Change Face Photo"
        tvSupportedFormatFace.visibility = View.GONE
    }

    private fun updateIntraPreview(uri: Uri) {
        ivPreviewIntra.setImageURI(uri)
        ivPreviewIntra.visibility = View.VISIBLE
        ivUploadIconIntra.visibility = View.GONE
        tvUploadTextIntra.text = "Change Intraoral Photo"
    }

    private var selectedShade: String = ""
    private fun setupShadeSelection() {
        val shades = listOf(R.id.shadeA1, R.id.shadeA2, R.id.shadeA3, R.id.shadeA35, R.id.shadeB1, R.id.shadeB2, R.id.shadeC1, R.id.shadeBL1)
        shades.forEach { id ->
            findViewById<TextView>(id).setOnClickListener { view ->
                shades.forEach { findViewById<TextView>(it).setBackgroundResource(R.drawable.bg_input_box) }
                view.setBackgroundResource(R.drawable.bg_icon_circle_green_light)
                selectedShade = (view as TextView).text.toString()
            }
        }
    }
}
