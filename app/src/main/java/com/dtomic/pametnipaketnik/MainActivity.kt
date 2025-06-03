package com.dtomic.pametnipaketnik

import android.R.attr.type
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dtomic.pametnipaketnik.composable.pages.Page_Login
import com.dtomic.pametnipaketnik.composable.pages.Page_Login2FA
import com.dtomic.pametnipaketnik.composable.pages.Page_MainMenu
import com.dtomic.pametnipaketnik.composable.pages.Page_Register
import com.dtomic.pametnipaketnik.composable.pages.Page_Register2FA
import com.dtomic.pametnipaketnik.composable.pages.Page_Title
import com.dtomic.pametnipaketnik.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation()
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
        composable("MainMenuPage") {Page_MainMenu(navController)}
    }
}