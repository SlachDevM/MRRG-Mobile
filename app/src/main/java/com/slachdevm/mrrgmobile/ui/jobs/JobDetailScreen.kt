package com.slachdevm.mrrgmobile.ui.jobs

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.slachdevm.mrrgmobile.R
import java.io.ByteArrayOutputStream
import java.io.File

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
                title = { Text(stringResource(R.string.job_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                JobInfoSection(
                    job = job,
                    onStartNavigation = ::startNavigation,
                    onCallClient = ::callClient
                )

                Spacer(modifier = Modifier.height(24.dp))

                PhotoSection(
                    title = stringResource(R.string.before_photos),
                    photos = job.beforePhotos,
                    onAddPhoto = {
                        selectedPhotoType = "before"
                        showPhotoSourceDialog = true
                    },
                    onDeletePhoto = { viewModel.removePhoto(it, isBefore = true) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PhotoSection(
                    title = stringResource(R.string.after_photos),
                    photos = job.afterPhotos,
                    onAddPhoto = {
                        selectedPhotoType = "after"
                        showPhotoSourceDialog = true
                    },
                    onDeletePhoto = { viewModel.removePhoto(it, isBefore = false) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                NotesSection(
                    notesText = notesText,
                    isUpdating = state.isUpdating,
                    onNotesChange = { notesText = it },
                    onSaveNotes = { viewModel.updateNotes(notesText) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                CompleteJobButton(
                    status = job.status,
                    isUpdating = state.isUpdating,
                    error = state.error,
                    onCompleteJob = viewModel::completeJob
                )
            }
        }

        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                title = { Text(stringResource(R.string.dialog_add_photo_title)) },
                text = { Text(stringResource(R.string.dialog_add_photo_message)) },
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
                        Text(stringResource(R.string.action_camera))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Text(stringResource(R.string.action_gallery))
                    }
                }
            )
        }
    }
}
