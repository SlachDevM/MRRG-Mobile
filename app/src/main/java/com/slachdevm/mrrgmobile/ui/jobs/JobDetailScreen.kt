package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import android.content.Intent
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable
import com.slachdevm.mrrgmobile.ui.components.StatusChip
import com.slachdevm.mrrgmobile.ui.components.toJobTypeLabel
import com.slachdevm.mrrgmobile.ui.components.toPriorityLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    viewModel: JobDetailViewModel,
    onBack: () -> Unit
) {
    val state = viewModel.uiState
    val scrollState = rememberScrollState()
    var notesText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedPhotoType by remember { mutableStateOf<String?>(null) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    fun createImageUri(): Uri {
        val imageFile = File.createTempFile(
            "mrrg_photo_",
            ".jpg",
            context.cacheDir
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    fun startNavigation(address: String) {
        val encodedAddress = Uri.encode(address)

        val navigationIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("google.navigation:q=$encodedAddress")
        ).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (navigationIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(navigationIntent)
        } else {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedAddress")
            )
            context.startActivity(browserIntent)
        }
    }

    fun callClient(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }

        context.startActivity(intent)
    }

    fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                val resizedBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    1024,
                    (1024f / originalBitmap.width * originalBitmap.height).toInt(),
                    true
                )

                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)

                val bytes = outputStream.toByteArray()

                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val base64 = uriToBase64(uri)

            if (base64 != null) {
                viewModel.addPhoto(
                    url = base64,
                    isBefore = selectedPhotoType == "before"
                )
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                val base64 = uriToBase64(uri)

                if (base64 != null) {
                    viewModel.addPhoto(
                        url = base64,
                        isBefore = selectedPhotoType == "before"
                    )
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri()
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(state.job) {
        state.job?.notes?.let { notesText = it }
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            onBack()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.job != null) {
            val job = state.job
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(text = job.clientName, style = MaterialTheme.typography.headlineMedium)
                Text(text = job.clientAddress, style = MaterialTheme.typography.bodyLarge)
                TextButton(
                    onClick = { startNavigation(job.clientAddress) }
                ) {
                    Text("Start navigation")
                }
                Text(text = job.clientPhone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                TextButton(
                    onClick = { callClient(job.clientPhone) }
                ) {
                    Text("Call client")
                }
                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(label = "Type", value = job.jobTypes.toJobTypeLabel())
                StatusChip(status = job.status)
                DetailItem(label = "Priority", value = job.priorityLevel.toPriorityLabel())
                DetailItem(label = "Details", value = job.details ?: "No extra details")

                Spacer(modifier = Modifier.height(24.dp))

                PhotoSection(
                    title = "Before Photos",
                    photos = job.beforePhotos,
                    onAddPhoto = {
                        selectedPhotoType = "before"
                        showPhotoSourceDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PhotoSection(
                    title = "After Photos",
                    photos = job.afterPhotos,
                    onAddPhoto = {
                        selectedPhotoType = "after"
                        showPhotoSourceDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Add notes here...") }
                )
                Button(
                    onClick = { viewModel.updateNotes(notesText) },
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                    enabled = !state.isUpdating
                ) {
                    Text("Save Notes")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.completeJob() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    enabled = !state.isUpdating
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VALIDATE / COMPLETE")
                }

                if (state.error != null) {
                    Text(text = state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                title = { Text("Add photo") },
                text = { Text("Choose a photo source") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false

                            val uri = createImageUri()
                            cameraImageUri = uri

                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) {
                        Text("Camera")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Text("Gallery")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PhotoSection(
    title: String,
    photos: List<String>,
    onAddPhoto: () -> Unit
) {
    var selectedPhoto by remember { mutableStateOf<String?>(null) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onAddPhoto) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
            }
        }

        if (photos.isEmpty()) {
            Text(text = "No photos added", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos) { photoUrl ->
                    Card(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { selectedPhoto = photoUrl }
                    ) {
                        val cleanBase64 = photoUrl
                            .substringAfter("base64,", photoUrl)
                            .replace("\n", "")
                            .replace("\r", "")
                            .trim()

                        val imageBytes = try {
                            Base64.decode(cleanBase64, Base64.DEFAULT)
                        } catch (e: Exception) {
                            null
                        }

                        val bitmap = imageBytes?.let {
                            BitmapFactory.decodeByteArray(it, 0, it.size)
                        }

                        if (bitmap != null) {
                            val bitmap = imageBytes?.let {
                                BitmapFactory.decodeByteArray(it, 0, it.size)
                            }

                            val displayBitmap = bitmap?.let {
                                Bitmap.createScaledBitmap(it, 300, 300, true)
                            }

                            if (displayBitmap != null) {
                                Image(
                                    bitmap = displayBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("Image unavailable")
                            }
                        } else {
                            Text("Image unavailable")
                        }
                    }
                }
            }
        }
    }

    selectedPhoto?.let { photoUrl ->
        Dialog(
            onDismissRequest = { selectedPhoto = null }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                val cleanBase64 = photoUrl
                    .substringAfter("base64,", photoUrl)
                    .replace("\n", "")
                    .replace("\r", "")
                    .trim()

                val imageBytes = try {
                    Base64.decode(cleanBase64, Base64.DEFAULT)
                } catch (e: Exception) {
                    null
                }

                val bitmap = imageBytes?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)
                }

                val displayBitmap = bitmap?.let {
                    Bitmap.createScaledBitmap(
                        it,
                        900,
                        (900f / it.width * it.height).toInt(),
                        true
                    )
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
}
