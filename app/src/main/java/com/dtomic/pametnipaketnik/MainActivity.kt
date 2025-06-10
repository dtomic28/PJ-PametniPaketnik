package com.dtomic.pametnipaketnik

import android.R.attr.type
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dtomic.pametnipaketnik.composable.pages.Page_BuyItem
import com.dtomic.pametnipaketnik.composable.pages.Page_Login
import com.dtomic.pametnipaketnik.composable.pages.Page_Login2FA
import com.dtomic.pametnipaketnik.composable.pages.Page_MainMenu
import com.dtomic.pametnipaketnik.composable.pages.Page_Register
import com.dtomic.pametnipaketnik.composable.pages.Page_Register2FA
import com.dtomic.pametnipaketnik.composable.pages.Page_SellItem
import com.dtomic.pametnipaketnik.composable.pages.Page_Title
import com.dtomic.pametnipaketnik.composable.pages.QRCodeScannerScreen
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.globalStorage
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "TitlePage"
    ) {
        composable("TitlePage") { Page_Title(navController) }
        composable("LoginPage") { Page_Login(navController) }
        composable("RegisterPage") { Page_Register(navController) }
        composable("ItemSell") { Page_SellItem(navController) }
        composable(
            route = "RegisterPage2FA/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            Page_Register2FA(navController, username = username)
    }
        composable(
            route = "LoginPage2FA/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            Page_Login2FA(navController, username = username)
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