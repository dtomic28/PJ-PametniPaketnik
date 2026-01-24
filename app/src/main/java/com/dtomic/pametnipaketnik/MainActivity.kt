package com.dtomic.pametnipaketnik

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dtomic.pametnipaketnik.composable.pages.MapViewModel
import com.dtomic.pametnipaketnik.composable.pages.Page_BuyItem
import com.dtomic.pametnipaketnik.composable.pages.Page_ChooseTown
import com.dtomic.pametnipaketnik.composable.pages.Page_IntroVideo
import com.dtomic.pametnipaketnik.composable.pages.Page_Login
import com.dtomic.pametnipaketnik.composable.pages.Page_MainMenu
import com.dtomic.pametnipaketnik.composable.pages.Page_Map
import com.dtomic.pametnipaketnik.composable.pages.Page_Register
import com.dtomic.pametnipaketnik.composable.pages.Page_SellItem
import com.dtomic.pametnipaketnik.composable.pages.Page_Title
import com.dtomic.pametnipaketnik.composable.pages.QRCodeScannerScreen
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.globalStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.hide()
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by globalStorage.isDarkTheme.collectAsState()
            AppTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "IntroVideoPage"
    ) {
        composable("IntroVideoPage") { Page_IntroVideo(navController) }
        composable("TitlePage") { Page_Title(navController) }
        composable("LoginPage") { Page_Login(navController) }
        composable("RegisterPage") { Page_Register(navController) }
        composable("ItemSell") { Page_SellItem(navController) }
        composable("MapPage") {
            val vm: MapViewModel = viewModel()
            Page_Map(navController, vm)
        }

        composable("ChooseTownPage") {
            val vm: MapViewModel = viewModel(
                remember(navController) { navController.getBackStackEntry("MapPage") }
            )
            Page_ChooseTown(navController, vm)
        }
        composable(
            route = "MainMenuPage/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType } )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            Page_MainMenu(navController, username = username)}
        composable(
            route = "Page_BuyItem/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            Page_BuyItem(navController, itemId = itemId)
        }
        composable("QRCodeScanner") {
            QRCodeScannerScreen(
                onBoxIdScanned = { scannedBoxId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_box_id", scannedBoxId)
                    navController.popBackStack()
                }
            )
        }
    }
}