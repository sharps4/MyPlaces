package com.example.myplaces

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myplaces.ui.AppViewModelProvider
import com.example.myplaces.ui.MainScreen
import com.example.myplaces.ui.add.AddPlaceScreen
import com.example.myplaces.ui.list.PlaceListScreen
import com.example.myplaces.ui.theme.MyPlacesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyPlacesTheme {
                MyPlacesNavHost()
            }
        }
    }
}

@Composable
fun MyPlacesNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "map") {
        composable("map") {
            val viewModel: com.example.myplaces.ui.map.MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                MainScreen(
                    onAddPlace = { geoPoint ->
                        navController.navigate("add/${geoPoint.latitude}/${geoPoint.longitude}")
                    },
                    onShowList = {
                        navController.navigate("list")
                    },
                    modifier = Modifier.padding(innerPadding),
                    viewModel = viewModel
                )
            }
        }
        composable("list") {
            val viewModel: com.example.myplaces.ui.map.MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
            PlaceListScreen(
                viewModel = viewModel,
                onPlaceClick = { place ->
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "add/{lat}/{lng}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            AddPlaceScreen(
                latitude = lat,
                longitude = lng,
                onPlaceAdded = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }
    }
}