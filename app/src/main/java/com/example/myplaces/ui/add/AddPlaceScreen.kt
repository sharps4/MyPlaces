package com.example.myplaces.ui.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myplaces.ui.components.CameraView
import com.example.myplaces.util.PhotoStorage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddPlaceScreen(
    latitude: Double,
    longitude: Double,
    onPlaceAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddPlaceViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showCamera by remember { mutableStateOf(false) }
    
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            scope.launch {
                val path = PhotoStorage.importFromUri(context, it)
                viewModel.updatePhoto(path)
            }
        }
    }

    if (showCamera) {
        if (cameraPermissionState.status.isGranted) {
            CameraView(
                onPhotoCaptured = { path ->
                    viewModel.updatePhoto(path)
                    showCamera = false
                },
                onClose = { showCamera = false }
            )
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            showCamera = false
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un lieu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("Choisir un emoji :", style = MaterialTheme.typography.titleMedium)
            val emojis = listOf("📍", "🏠", "🍕", "🌳", "🏢", "🏖️", "🏔️", "☕", "🍺", "🍦")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                emojis.chunked(5).forEach { rowEmojis ->
                    Column {
                        rowEmojis.forEach { emoji ->
                            FilterChip(
                                selected = viewModel.emoji == emoji,
                                onClick = { viewModel.emoji = emoji },
                                label = { Text(emoji, style = MaterialTheme.typography.headlineSmall) }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.photoPath != null) {
                    AsyncImage(
                        model = viewModel.photoPath,
                        contentDescription = "Photo du lieu",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCamera = true },
                    modifier = Modifier.weight(1.0f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Appareil")
                }
                Button(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1.0f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galerie")
                }
            }

            Button(
                onClick = { viewModel.savePlace(latitude, longitude, onPlaceAdded) },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.title.isNotBlank()
            ) {
                Text("Enregistrer le lieu")
            }
        }
    }
}
