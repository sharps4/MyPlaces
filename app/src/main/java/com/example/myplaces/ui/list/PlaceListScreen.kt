package com.example.myplaces.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myplaces.domain.Place
import com.example.myplaces.ui.map.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreen(
    viewModel: MapViewModel,
    onPlaceClick: (Place) -> Unit,
    onBack: () -> Unit
) {
    val places by viewModel.places.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Lieux") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (places.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aucun lieu enregistré")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(places) { place ->
                    PlaceItem(place = place, onClick = { onPlaceClick(place) })
                }
            }
        }
    }
}

@Composable
fun PlaceItem(place: Place, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (place.photoPath != null) {
                AsyncImage(
                    model = place.photoPath,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(place.emoji, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(place.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    place.displayAddress,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
            
            if (place.photoPath != null) {
                Text(place.emoji, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}
