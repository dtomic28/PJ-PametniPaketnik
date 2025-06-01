package com.dtomic.pametnipaketnik

import android.os.Bundle
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.dtomic.pametnipaketnik.composable.TitlePage
import com.dtomic.pametnipaketnik.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        enableEdgeToEdge()

        setContent {
            AppTheme {
                TitlePage()
            }
        }
    }
}