package com.dtomic.pametnipaketnik.composable.pages

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.composable.parts.Custom_CameraButton
import com.dtomic.pametnipaketnik.composable.parts.Custom_ErrorBox
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// MODEL
class Register2FAViewModel : ViewModel() {

    val username = mutableStateOf("")

    private val _numOfBatches = MutableStateFlow(0)
    val numOfBatches: StateFlow<Int> = _numOfBatches

    private val _completeRegistration2FA = MutableStateFlow(false)
    val completeRegistration2FA: StateFlow<Boolean> = _completeRegistration2FA

    private val _errorMessage = MutableStateFlow("Press the + button and move your head for 2 seconds while looking at the camera. Repeat 10 times!")
    val errorMessage: StateFlow<String> = _errorMessage


    fun takePictureBatch() {
        _numOfBatches.value++
        /*
        TODO Tomic:
            vsakic ku prides sm not zajem 10 slik v spannu 2 sekund (5 fps)
            pa jih posl n backend z treniranje modela. username uporabnika
            mas shranjen pod "username". ta funkcija se izvede usakic ku
            uporabnik prtisne "+" button (5x)
         */
        if (numOfBatches.value > 4) {
            _completeRegistration2FA.value = true
        }
    }
    fun resetNavigation() {
        _completeRegistration2FA.value = false
    }
}

// VIEW
@Composable
fun Page_Register2FA(navController: NavController, viewModel: Register2FAViewModel = viewModel(), username: String) {
    viewModel.username.value = username

    val numOfBatches by viewModel.numOfBatches.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val error = errorMessage.isNotEmpty()

    val navTrigger by viewModel.completeRegistration2FA.collectAsState()

    LaunchedEffect(navTrigger) {
        if (navTrigger) {
            navController.navigate("TitlePage")
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.6f),
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                /*
                TODO tomic:
                    camera preview v tem okvirju
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
                        .weight(0.1f)
                )
                Surface(
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.20f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,

                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text="${numOfBatches}/5",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                }
                Spacer(
                    modifier = Modifier
                        .weight(0.1f)
                )
                Custom_CameraButton(
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.20f),
                    onClick = { viewModel.takePictureBatch() }
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
        Page_Register2FA(navController, username = "Tilen")
    }
}