package com.example.myplaces.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.myplaces.domain.Place
import com.example.myplaces.util.BitmapUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onMapLongClick: (GeoPoint) -> Unit,
    onMarkerClick: (Place) -> Unit,
    onShowList: () -> Unit,
    onShowSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val places by viewModel.places.collectAsState()
    val availableEmojis by viewModel.availableEmojis.collectAsState()
    val selectedFilter by viewModel.selectedEmojiFilter.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Configuration d'une source alternative (CartoDB Voyager)
    val cartoDbSource = remember {
        XYTileSource(
            "CartoDB_Voyager",
            0, 20, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
            ),
            "© OpenStreetMap contributors, © CARTO"
        )
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(cartoDbSource)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(48.8566, 2.3522)) // Paris
        }
    }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            locationOverlay.enableMyLocation()
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                mv.overlays.clear()
                mv.overlays.add(locationOverlay)

                val mapEventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        p?.let { onMapLongClick(it) }
                        return true
                    }
                }
                mv.overlays.add(MapEventsOverlay(mapEventsReceiver))

                places.forEach { place ->
                    val marker = Marker(mv).apply {
                        position = GeoPoint(place.latitude, place.longitude)
                        title = place.title
                        icon = BitmapUtils.createEmojiMarker(context, place.emoji)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setOnMarkerClickListener { _, _ ->
                            onMarkerClick(place)
                            true
                        }
                    }
                    mv.overlays.add(marker)
                }
                mv.invalidate()
            }
        )

        // Bandeau de filtrage par emoji
        if (availableEmojis.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableEmojis) { emoji ->
                    FilterChip(
                        selected = selectedFilter == emoji,
                        onClick = { viewModel.toggleFilter(emoji) },
                        label = { Text(emoji) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        // Bouton récapitulatif (Liste)
        SmallFloatingActionButton(
            onClick = onShowList,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(Icons.Default.List, contentDescription = "Récapitulatif")
        }

        // Bouton Paramètres
        SmallFloatingActionButton(
            onClick = onShowSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Paramètres")
        }

        FloatingActionButton(
            onClick = {
                val myLocation = locationOverlay.myLocation
                if (myLocation != null) {
                    mapView.controller.animateTo(myLocation)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Ma position")
        }
    }
}
