package com.simats.smileai.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast

object FileDownloader {

    fun downloadFile(context: Context, url: String, fileName: String, token: String? = null) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading $fileName")
                .setDescription("Downloading file from SmileAI")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            token?.let {
                if (it.isNotBlank()) {
                    val finalToken = if (it.startsWith("Bearer ", ignoreCase = true)) it else "Bearer $it"
                    request.addRequestHeader("Authorization", finalToken)
                }
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "Download started to Downloads folder...", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
