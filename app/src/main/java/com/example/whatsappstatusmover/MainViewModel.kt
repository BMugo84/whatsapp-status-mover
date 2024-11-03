package com.example.whatsappstatusmover

import android.app.Application
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
                MainViewModel(application)
            }
        }
    }

    fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            app,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun copyFiles() {
        try {
            val sourceDir = File("/storage/emulated/0/WhatsApp/Media/.Statuses")
            val destinationDir = File("/storage/emulated/0/DCIM/whatsapp_statuses")

            if (!sourceDir.exists()) {
                showToast("Source directory not found")
                return
            }

            if (!destinationDir.exists()) {
                val created = destinationDir.mkdirs()
                if (!created) {
                    showToast("Failed to create destination directory")
                    return
                }
            }

            var copyCount = 0
            sourceDir.listFiles()?.forEach { sourceFile ->
                if (sourceFile.isFile &&
                    !sourceFile.name.equals(".nomedia") &&
                    (sourceFile.name.endsWith(".jpg") ||
                            sourceFile.name.endsWith(".jpeg") ||
                            sourceFile.name.endsWith(".png") ||
                            sourceFile.name.endsWith(".mp4") ||
                            sourceFile.name.endsWith(".gif"))) {

                    val destFile = File(destinationDir, sourceFile.name)
                    if (!destFile.exists()) {
                        try {
                            FileInputStream(sourceFile).use { input ->
                                FileOutputStream(destFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            copyCount++
                        } catch (e: Exception) {
                            showToast("Error copying ${sourceFile.name}: ${e.message}")
                        }
                    }
                }
            }

            val message = when {
                copyCount > 0 -> "Successfully copied $copyCount new files"
                else -> "No new files to copy"
            }
            showToast(message)
        } catch (e: Exception) {
            showToast("Error: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
    }
}