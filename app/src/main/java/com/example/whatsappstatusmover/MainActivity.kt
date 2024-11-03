// MainViewModel.kt
package com.example.whatsappstatusmover

import android.Manifest
//import android.app.Application
//import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.AndroidViewModel
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream



// MainActivity.kt
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.copyFiles()
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
                AppNavigation(viewModel)  // This will now use the AppNavigation from Navigation.kt
            }
        }
    }

    fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}

// Update MainScreen composable
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onViewPhotosClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current

        Button(
            onClick = {
                if (viewModel.checkPermission()) {
                    viewModel.copyFiles()
                } else {
                    (context as MainActivity).requestPermission()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Save Status Files",
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(
            onClick = onViewPhotosClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "View Saved Photos",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

