package com.slachdevm.mrrgmobile.ui.jobs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PhotoSection(
    title: String,
    photos: List<String>,
    onAddPhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit
) {
    var selectedPhoto by remember { mutableStateOf<String?>(null) }

    Column {
        PhotoSectionHeader(
            title = title,
            onAddPhoto = onAddPhoto
        )

        if (photos.isEmpty()) {
            Text(
                text = "No photos added",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos) { photoUrl ->
                    PhotoThumbnail(
                        photoUrl = photoUrl,
                        onOpenPhoto = { selectedPhoto = photoUrl },
                        onDeletePhoto = { onDeletePhoto(photoUrl) }
                    )
                }
            }
        }
    }

    selectedPhoto?.let { photoUrl ->
        FullscreenPhotoDialog(
            photoUrl = photoUrl,
            onDismiss = { selectedPhoto = null }
        )
    }
}

@Composable
private fun PhotoSectionHeader(
    title: String,
    onAddPhoto: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onAddPhoto) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photoUrl: String,
    onOpenPhoto: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    Box(modifier = Modifier.size(120.dp)) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onOpenPhoto)
        ) {
            val displayBitmap = remember(photoUrl) {
                decodeBase64Image(photoUrl)?.let {
                    Bitmap.createScaledBitmap(it, 300, 300, true)
                }
            }

            if (displayBitmap != null) {
                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image unavailable")
                }
            }
        }

        IconButton(
            onClick = onDeletePhoto,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete photo",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun FullscreenPhotoDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            val displayBitmap = remember(photoUrl) {
                decodeBase64Image(photoUrl)?.let {
                    Bitmap.createScaledBitmap(
                        it,
                        900,
                        (900f / it.width * it.height).toInt(),
                        true
                    )
                }
            }

            if (displayBitmap != null) {
                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image unavailable")
                }
            }
        }
    }
}

private fun decodeBase64Image(photoUrl: String): Bitmap? {
    val cleanBase64 = photoUrl
        .substringAfter("base64,", photoUrl)
        .replace("\n", "")
        .replace("\r", "")
        .trim()

    val imageBytes = try {
        Base64.decode(cleanBase64, Base64.DEFAULT)
    } catch (e: Exception) {
        return null
    }

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
