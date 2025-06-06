package com.dtomic.pametnipaketnik.composable.pages

import android.R.attr.password
import android.util.Base64
import android.util.Log
import android.util.Log.e
import android.util.Log.i
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.composable.parts.Custom_CameraButton
import com.dtomic.pametnipaketnik.composable.parts.Custom_ErrorBox
import com.dtomic.pametnipaketnik.composable.parts.Custom_Logo
import com.dtomic.pametnipaketnik.composable.parts.Custom_TextField
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files.exists
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// MODEL
class Login2FAViewModel : ViewModel() {

    val username = mutableStateOf("")

    private val _errorMessage = MutableStateFlow("Look at the camera and press the + button!")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _moveToMainMenu = MutableStateFlow(false)
    val moveToMainMenu: StateFlow<Boolean> = _moveToMainMenu


    fun takePicture() {
        viewModelScope.launch {
            /*
            TODO tomic:
                tle dodej logic ku bo n backend poslau sliko pa verificiru.
                če je 2FA uspesen, ta funkcija se zazene ku se prtisne "+" button
             */
            _moveToMainMenu.value = true
        }

    }
    fun resetNavigation() {
        _moveToMainMenu.value = false
    }
}

// VIEW
@Composable
fun Page_Login2FA(navController: NavController, viewModel: Login2FAViewModel = viewModel(), username: String) {
    viewModel.username.value = username

    val errorMessage by viewModel.errorMessage.collectAsState()
    val error = errorMessage.isNotEmpty()

    val navTrigger by viewModel.moveToMainMenu.collectAsState()

    LaunchedEffect(navTrigger) {
        if (navTrigger) {
            navController.navigate("MainMenuPage")
            viewModel.resetNavigation()
        }
    }

    Box( // whole screen
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                if (error) Custom_ErrorBox(errorMessage)
            }
            Surface( // Center Island
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.6f),
                shadowElevation = 6.dp,

                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
            /*
            TODO Tomic:
                tle dodej camera preview
             */
            }
            Row( // Buttons
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Custom_Button( // back
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.5f),
                    text = stringResource(R.string.btn_back),
                    onClick = {navController.popBackStack()},
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(
                    modifier = Modifier
                        .weight(0.4f)
                )
                Custom_CameraButton(
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.20f),
                    onClick = { viewModel.takePicture() }
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val navController = rememberNavController()
    AppTheme {
        Page_Login2FA(navController, username = "tilen")
    }
}