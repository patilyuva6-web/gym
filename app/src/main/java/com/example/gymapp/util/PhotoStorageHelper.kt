package com.example.gymapp.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PhotoStorageHelper {

    private const val PHOTOS_DIR = "member_photos"
    private const val CAMERA_CACHE_DIR = "camera_captures"

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, PHOTOS_DIR).apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(photosDir, "member_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            destinationFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun createTempCameraImageUri(context: Context): Pair<Uri, File> {
        val cacheDir = File(context.cacheDir, CAMERA_CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
        val tempFile = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return uri to tempFile
    }

    fun moveTempFileToInternalStorage(context: Context, tempFile: File): String? {
        if (!tempFile.exists() || tempFile.length() == 0L) return null
        return try {
            val photosDir = File(context.filesDir, PHOTOS_DIR).apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(photosDir, "member_${UUID.randomUUID()}.jpg")
            tempFile.copyTo(destinationFile, overwrite = true)
            tempFile.delete()
            destinationFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun deletePhotoIfExists(photoPath: String?) {
        if (photoPath.isNullOrBlank()) return
        try {
            val file = File(photoPath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }
}
