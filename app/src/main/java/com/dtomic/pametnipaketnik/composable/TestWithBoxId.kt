package com.dtomic.pametnipaketnik.composable

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dtomic.pametnipaketnik.utils.ApiResponse
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import com.dtomic.pametnipaketnik.utils.extractZip
import com.dtomic.pametnipaketnik.utils.playAudio
import com.dtomic.pametnipaketnik.utils.saveBase64ToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TestWithBoxId(boxId: String) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Processing...") }

    LaunchedEffect(boxId) {
        withContext(Dispatchers.IO) {
            try {
                status = "Connecting to server..."
                val http = HttpClientWrapper()

                http.get("token/requestToken/$boxId") { success, responseBody ->
                    if (success && responseBody != null) {
                        try {
                            status = "Processing response..."
                            val gson = com.google.gson.Gson()
                            val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)

                            val base64Data = apiResponse.data
                            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)

                            val successSaving = saveBase64ToFile(context, "test.zip", decodedBytes)

                            if (successSaving) {
                                val extractDir = File(context.cacheDir, "extracted_audio")
                                extractDir.mkdirs()

                                val extractedFiles = extractZip(context, "test.zip", extractDir)
                                Log.i("ExtractedFiles", extractedFiles.joinToString())

                                if (extractedFiles.contains("token.wav")) {
                                    status = "Playing audio..."
                                    playAudio(context, File(extractDir, "token.wav").absolutePath)
                                    status = "Success! Audio played"
                                } else {
                                    status = "Error: No audio file found"
                                }
                            } else {
                                status = "Error: Failed to save ZIP"
                            }
                        } catch (e: Exception) {
                            status = "Error parsing or handling response: ${e.localizedMessage}"
                            Log.e("ResponseError", e.message ?: "Unknown error", e)
                        }
                    } else {
                        status = "Error: $responseBody"
                    }
                }
            } catch (e: Exception) {
                status = "Error: ${e.localizedMessage}"
                Log.e("RequestError", "Exception in HTTP request", e)
            }
        }
    }

    Text(
        text = status,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
