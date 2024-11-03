package com.example.whatsappstatusmover

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
import coil3.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Data class to hold photo information
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(photos) { photo ->
                PhotoItem(photo)
            }
        }
    }
}

@Composable
private fun PhotoItem(photo: PhotoItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = photo.uri,
                contentDescription = "Saved photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )

            Text(
                text = photo.filename,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(Date(photo.date)),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Function to load photos from storage
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