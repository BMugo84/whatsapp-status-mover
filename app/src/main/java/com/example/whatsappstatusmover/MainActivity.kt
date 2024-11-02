// MainActivity.kt
package com.example.whatsappstatusmover

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            copyFiles()
        } else {
            Toast.makeText(
                this,
                "Storage permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StatusCopierScreen(
                    onCopyClick = {
                        if (checkPermission()) {
                            copyFiles()
                        } else {
                            requestPermission()
                        }
                    }
                )
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun copyFiles() {
        try {
            val sourceDir = File("/storage/emulated/0/WhatsApp/Media/.Statuses")
            val destinationDir = File("/storage/emulated/0/DCIM/whatsapp_statuses")

            if (!sourceDir.exists()) {
                Toast.makeText(this, "Source directory not found", Toast.LENGTH_SHORT).show()
                return
            }

            if (!destinationDir.exists()) {
                val created = destinationDir.mkdirs()
                if (!created) {
                    Toast.makeText(this, "Failed to create destination directory", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            var copyCount = 0
            sourceDir.listFiles()?.forEach { sourceFile ->
                if (sourceFile.isFile) {
                    val destFile = File(destinationDir, sourceFile.name)
                    if (!destFile.exists()) {  // Only copy if file doesn't exist in destination
                        try {
                            FileInputStream(sourceFile).use { input ->
                                FileOutputStream(destFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            copyCount++
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error copying ${sourceFile.name}: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            val message = when {
                copyCount > 0 -> "Successfully copied $copyCount new files"
                else -> "No new files to copy"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun StatusCopierScreen(onCopyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onCopyClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Save Status Files",  // Changed text to better reflect the action
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}