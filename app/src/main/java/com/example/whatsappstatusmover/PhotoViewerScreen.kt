package com.example.whatsappstatusmover

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells  // Add this import
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid  // Add this import
import androidx.compose.foundation.lazy.grid.items  // Add this import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Data class remains the same
data class PhotoItem(
    val uri: Uri,
    val filename: String,
    val date: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    // State to hold our photos
    var photos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }

    // Load photos when screen is created
    LaunchedEffect(Unit) {
        photos = loadPhotos(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Photos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Show loading or empty state if needed
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No photos found")
            }
        } else {
            // Replace LazyColumn with LazyVerticalGrid
            LazyVerticalGrid(
                // Define grid with 4 columns of equal width
                columns = GridCells.Fixed(4),

                // Apply padding around the grid
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),

                // Add padding around the content
                contentPadding = PaddingValues(4.dp),

                // Add spacing between items
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(photos) { photo ->
                    PhotoGridItem(photo)
                }
            }
        }
    }
}

// Create a new composable for grid items
@Composable
private fun PhotoGridItem(photo: PhotoItem) {
    // Each grid item is now a smaller card
    Card(
        modifier = Modifier
            .aspectRatio(1f)  // Make it square
            .fillMaxWidth()   // Fill the grid cell width
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Simplified layout for grid items
        AsyncImage(
            model = photo.uri,
            contentDescription = "Saved photo: ${photo.filename}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// Function to load photos remains the same
private suspend fun loadPhotos(context: Context): List<PhotoItem> {
    return withContext(Dispatchers.IO) {
        val photos = mutableListOf<PhotoItem>()
        val directory = File("/storage/emulated/0/DCIM/whatsapp_statuses")

        if (directory.exists()) {
            directory.listFiles()?.filter { file ->
                file.isFile && (
                        file.name.endsWith(".jpg", ignoreCase = true) ||
                                file.name.endsWith(".jpeg", ignoreCase = true) ||
                                file.name.endsWith(".png", ignoreCase = true)
                        )
            }?.forEach { file ->
                val uri = Uri.fromFile(file)
                photos.add(
                    PhotoItem(
                        uri = uri,
                        filename = file.name,
                        date = file.lastModified()
                    )
                )
            }
        }

        photos.sortedByDescending { it.date }
    }
}