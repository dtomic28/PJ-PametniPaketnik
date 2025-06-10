package com.dtomic.pametnipaketnik.composable.pages

import Custom_CameraPreview
import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.composable.parts.Custom_CameraButton
import com.dtomic.pametnipaketnik.composable.parts.Custom_CameraPermission
import com.dtomic.pametnipaketnik.composable.parts.Custom_ErrorBox
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

// MODEL
class Login2FAViewModel : ViewModel() {

    val username = mutableStateOf("")

    private val _errorMessage = MutableStateFlow("Look at the camera and press the + button!")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _moveToMainMenu = MutableStateFlow(false)
    val moveToMainMenu: StateFlow<Boolean> = _moveToMainMenu


    fun sendImageToBackend(username: String, file: File) {
        HttpClientWrapper.postFile(
            endpoint = "orv/predict/$username",
            file = file,
            callback = { success, response ->
                if (success && response != null) {
                    Log.d("TILEN", "Prediction result: $response")
                    try {
                        val json = JSONObject(response)
                        val prediction = json.optInt("prediction", -1)
                        if (prediction == 1) {
                            _moveToMainMenu.value = true
                        } else {
                            _errorMessage.value = "2FA unsuccessful"
                            Log.w("TILEN", "Prediction not accepted: $prediction")
                        }
                    } catch (e: Exception) {
                        Log.e("TILEN", "Failed to parse response JSON: ${e.message}")
                    }
                } else {
                    Log.e("TILEN", "Prediction failed: $response")
                }
            }
        )
    }


    fun takePicture(context: Context, username: String, imageCapture: ImageCapture) {
        val photoFile = File(context.cacheDir, "photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("TILEN", "took image")
                    sendImageToBackend(username, photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("TILEN", "Failed: ${exception.message}", exception)
                }
            }
        )
    }
    fun resetNavigation() {
        _moveToMainMenu.value = false
    }
}

// VIEW
@Composable
fun Page_Login2FA(navController: NavController, viewModel: Login2FAViewModel = viewModel(), username: String) {
    viewModel.username.value = username
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val context = LocalContext.current

    val errorMessage by viewModel.errorMessage.collectAsState()
    val error = errorMessage.isNotEmpty()

    val navTrigger by viewModel.moveToMainMenu.collectAsState()

    LaunchedEffect(navTrigger) {
        if (navTrigger) {
            navController.navigate("MainMenuPage/${username}")
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
                Custom_CameraPermission {
                    Custom_CameraPreview(Modifier.fillMaxSize(), imageCapture)
                }
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
                    onClick = {
                        imageCapture.value?.let {
                            viewModel.takePicture(context, username, it)
                        }
                    }
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