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

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            moveFiles()
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
                StatusMoverScreen(
                    onMoveClick = {
                        if (checkPermission()) {
                            moveFiles()
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

    private fun moveFiles() {
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

            var moveCount = 0
            sourceDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val destFile = File(destinationDir, file.name)
                    if (file.renameTo(destFile)) {
                        moveCount++
                    }
                }
            }

            Toast.makeText(this, "Successfully moved $moveCount files", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun StatusMoverScreen(onMoveClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onMoveClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Move Status Files",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}