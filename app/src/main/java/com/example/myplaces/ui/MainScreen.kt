package com.example.myplaces.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myplaces.domain.Place
import com.example.myplaces.ui.map.MapScreen
import com.example.myplaces.ui.map.MapViewModel
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddPlace: (GeoPoint) -> Unit,
    onShowList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        MapScreen(
            viewModel = viewModel,
            onMapLongClick = onAddPlace,
            onMarkerClick = { place ->
                selectedPlace = place
                showBottomSheet = true
            },
            onShowList = onShowList
        )

        if (showBottomSheet && selectedPlace != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                PlaceDetailsContent(place = selectedPlace!!)
            }
        }
    }
}

@Composable
fun PlaceDetailsContent(place: Place) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = place.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = place.emoji, style = MaterialTheme.typography.displayMedium)
        }
        
        Text(
            text = dateFormat.format(Date(place.createdAt)),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Text(text = place.displayAddress, style = MaterialTheme.typography.bodyMedium)
        
        HorizontalDivider()
        
        Text(text = place.description, style = MaterialTheme.typography.bodyLarge)
        
        if (place.photoPath != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = place.photoPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
