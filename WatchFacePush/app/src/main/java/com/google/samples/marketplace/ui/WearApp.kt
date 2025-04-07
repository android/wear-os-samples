package com.google.samples.marketplace.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.samples.marketplace.theme.WatchFaceMarketplaceTheme
import com.google.samples.marketplace.viewmodel.WatchFaceMarketplaceViewModel

@Composable
fun WearApp() {
    rememberSwipeDismissableNavController()
    WatchFaceMarketplaceTheme {
        AppScaffold {
            val navController = rememberSwipeDismissableNavController()
            // The viewModel is shared between the list page and the details page, so it is created
            // here and passed to the composables that need it.
            val viewModel: WatchFaceMarketplaceViewModel =
                viewModel(factory = WatchFaceMarketplaceViewModel.Factory)
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "watch_faces_list"
            ) {
                composable("watch_faces_list") {
                    WatchFacesPage(viewModel) { selectedWatchFace ->
                        viewModel.selectWatchFace(selectedWatchFace)
                        navController.navigate("watch_face")
                    }
                }
                composable("watch_face") { navBackStackEntry ->
                    WatchFaceItemPage(viewModel = viewModel)
                }
            }
        }
    }
}