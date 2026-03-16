package com.simats.smileai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.simats.smileai.network.CaseFile
import com.simats.smileai.network.RetrofitClient
import com.simats.smileai.utils.FileDownloader

class DentistExportFilesActivity : ComponentActivity() {
    private var caseFiles: List<CaseFile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dentist_export_files)

        val sharedPref = getSharedPreferences("SmileAI", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val caseId = intent.getIntExtra("EXTRA_CASE_ID", -1)

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnDownloadReportPdf = findViewById<LinearLayout>(R.id.btnDownloadReportPdf)
        val btnDownloadReportImage = findViewById<LinearLayout>(R.id.btnDownloadReportImage)
        val btnDownloadSmilePdf = findViewById<LinearLayout>(R.id.btnDownloadSmilePdf)

        btnBack.setOnClickListener {
            finish()
        }

        btnDownloadReportPdf.setOnClickListener {
            if (caseId != -1) {
                val url = "${RetrofitClient.BASE_URL}cases/$caseId/download/report/pdf"
                FileDownloader.downloadFile(this, url, "Report_Case_$caseId.pdf", accessToken)
            }
        }

        btnDownloadReportImage.setOnClickListener {
            if (caseId != -1) {
                val url = "${RetrofitClient.BASE_URL}cases/$caseId/download/report/image"
                FileDownloader.downloadFile(this, url, "Report_Case_$caseId.png", accessToken)
            }
        }

        btnDownloadSmilePdf.setOnClickListener {
            if (caseId != -1) {
                val url = "${RetrofitClient.BASE_URL}cases/$caseId/download/smile/pdf"
                FileDownloader.downloadFile(this, url, "Smile_Case_$caseId.pdf", accessToken)
            }
        }

        // Auto-download if flag is set
        val autoDownload = intent.getBooleanExtra("EXTRA_AUTO_DOWNLOAD", false)
        if (autoDownload && caseId != -1) {
            val url = "${RetrofitClient.BASE_URL}cases/$caseId/download/report/pdf"
            FileDownloader.downloadFile(this, url, "Report_Case_$caseId.pdf", accessToken)
        }
    }
}
