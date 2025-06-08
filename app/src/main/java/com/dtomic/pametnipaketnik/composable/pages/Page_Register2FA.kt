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
import androidx.compose.material3.Text
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// MODEL
class Register2FAViewModel : ViewModel() {

    val username = mutableStateOf("")
    private val allCapturedImages = mutableListOf<File>()

    private val _numOfBatches = MutableStateFlow(0)
    val numOfBatches: StateFlow<Int> = _numOfBatches

    private val _completeRegistration2FA = MutableStateFlow(false)
    val completeRegistration2FA: StateFlow<Boolean> = _completeRegistration2FA

    private val _errorMessage = MutableStateFlow("Press the + button and move your head for 2 seconds while looking at the camera. Repeat 10 times!")
    val errorMessage: StateFlow<String> = _errorMessage

    private fun uploadTrainingData(username: String, files: List<File>) {
        // TODO: Zip files or choose one image and POST with `HttpClientWrapper`.
        // Here’s where you'd use: /api/orv/train/{username}
    }

    fun takePictureBatch(context: Context, username: String, imageCapture: ImageCapture) {
        viewModelScope.launch {
            repeat(10) { i ->
                val photoFile = File.createTempFile("img_${_numOfBatches.value}_$i", ".jpg", context.cacheDir)
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                val result = CompletableDeferred<Unit>()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            allCapturedImages.add(photoFile)
                            result.complete(Unit)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            result.completeExceptionally(exception)
                        }
                    }
                )

                delay(200L)
                result.await()
            }

            _numOfBatches.value++
            if (_numOfBatches.value >= 5) {
                zipAndUploadImages(context, username)
            }
        }
    }
    private fun zipAndUploadImages(context: Context, username: String) {
        val zipFile = File(context.cacheDir, "images.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
            allCapturedImages.forEach { file ->
                val entry = ZipEntry(file.name)
                out.putNextEntry(entry)
                file.inputStream().use { it.copyTo(out) }
                out.closeEntry()
            }
        }

        sendZipToBackend(context, username, zipFile)
    }
    private fun sendZipToBackend(context: Context, username: String, zipFile: File) {
        val uploadEndpoint = "orv/upload/$username"
        val trainEndpoint = "orv/train/$username"

        HttpClientWrapper.postFile(
            endpoint = uploadEndpoint,
            file = zipFile,
            mimeType = "application/zip",
            callback = { success, response ->
                if (success) {
                    Log.d("TILEN", "Upload successful: $response")

                    HttpClientWrapper.postJson(
                        endpoint = trainEndpoint,
                        jsonBody = "{}",
                        callback = { trainSuccess, trainResponse ->
                            if (trainSuccess) {
                                Log.d("Train", "Training triggered: $trainResponse")
                                _completeRegistration2FA.value = true
                            } else {
                                Log.e("Train", "Training failed: $trainResponse")
                            }
                        }
                    )
                } else {
                    Log.e("TILEN", "Upload failed: $response")
                }
            }
        )
    }


    fun resetNavigation() {
        _completeRegistration2FA.value = false
    }
}

// VIEW
@Composable
fun Page_Register2FA(navController: NavController, viewModel: Register2FAViewModel = viewModel(), username: String) {
    viewModel.username.value = username
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val context = LocalContext.current

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
                    onClick = {
                        imageCapture.value?.let {
                            viewModel.takePictureBatch(context, username, it)
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
        Page_Register2FA(navController, username = "Tilen")
    }
}