package com.dtomic.pametnipaketnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.composable.pages.Page_Login
import com.dtomic.pametnipaketnik.composable.pages.Page_Register
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

    NavHost(navController = navController, startDestination = "TitlePage") {
        composable("TitlePage") { Page_Title(navController) }
        composable("LoginPage") { Page_Login(navController) }
        composable("RegisterPage") { Page_Register(navController) }
    }
}