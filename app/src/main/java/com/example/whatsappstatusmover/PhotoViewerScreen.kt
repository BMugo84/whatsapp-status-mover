package com.example.whatsappstatusmover

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Update PhotoItem to include selection state
data class PhotoItem(
    val uri: Uri,
    val filename: String,
    val date: Long,
    val file: File  // Add reference to actual file for deletion
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for photos list
    var photos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }

    // State for selection mode and selected photos
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedPhotos by remember { mutableStateOf(setOf<PhotoItem>()) }

    // State for showing delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load photos when screen is created
    LaunchedEffect(Unit) {
        photos = loadPhotos(context)
    }

    // Function to handle photo deletion
    fun deleteSelectedPhotos() {
        scope.launch {
            withContext(Dispatchers.IO) {
                selectedPhotos.forEach { photo ->
                    try {
                        if (photo.file.delete()) {
                            // File deleted successfully
                        }
                    } catch (e: Exception) {
                        // Handle deletion error
                    }
                }
                // Refresh photos list
                photos = loadPhotos(context)
                // Reset selection mode
                selectedPhotos = emptySet()
                isSelectionMode = false
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Photos") },
            text = { Text("Delete ${selectedPhotos.size} selected photos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteSelectedPhotos()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedPhotos.size} Selected")
                    } else {
                        Text("Saved Photos")
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSelectionMode) {
                                // Exit selection mode
                                isSelectionMode = false
                                selectedPhotos = emptySet()
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show delete button when in selection mode
                    AnimatedVisibility(visible = isSelectionMode && selectedPhotos.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete selected")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No photos found")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(photos) { photo ->
                    PhotoGridItem(
                        photo = photo,
                        isSelected = selectedPhotos.contains(photo),
                        isSelectionMode = isSelectionMode,
                        onLongClick = {
                            isSelectionMode = true
                            selectedPhotos = selectedPhotos + photo
                        },
                        onClick = {
                            if (isSelectionMode) {
                                selectedPhotos = if (selectedPhotos.contains(photo)) {
                                    selectedPhotos - photo
                                } else {
                                    selectedPhotos + photo
                                }
                                // Exit selection mode if no items selected
                                if (selectedPhotos.isEmpty()) {
                                    isSelectionMode = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGridItem(
    photo: PhotoItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .padding(2.dp)
            // Add selection border when selected
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    Modifier
                }
            )
            // Use combinedClickable for both click and long click
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box {
            AsyncImage(
                model = photo.uri,
                contentDescription = "Saved photo: ${photo.filename}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(percent = 50),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

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
                        date = file.lastModified(),
                        file = file  // Store file reference
                    )
                )
            }
        }

        photos.sortedByDescending { it.date }
    }
}