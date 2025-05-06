
package com.dtomic.pametnipaketnik

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dtomic.pametnipaketnik.ui.theme.PametniPaketnikTheme
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import androidx.compose.runtime.LaunchedEffect
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import java.io.FileOutputStream
import java.io.IOException
import android.media.MediaPlayer
import java.io.File
import java.util.zip.ZipInputStream

data class ApiResponse(
    val data: String,
    val result: Int,
    val errorNumber: Number
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PametniPaketnikTheme {
                test()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PametniPaketnikTheme {
        test()
    }
}

@Composable
fun test() {
    val context = LocalContext.current
    var extractedFiles:List<String>? = null
    var extractDir: File? = null
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                val mediaType = "application/json".toMediaType()
                val requestBody = """
                    {
                      "deliveryId": 12345,
                      "boxId": 539,
                      "tokenFormat": 4,
                      "latitude": 46.056946,
                      "longitude": 14.505751,
                      "qrCodeInfo": null,
                      "terminalSeed": 111222,
                      "isMultibox": false,
                      "doorIndex": 0,
                      "addAccessLog": true
                    }
                """.trimIndent().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://api-d4me-stage.direct4.me/sandbox/v1/Access/openbox")
                    .addHeader("Authorization", "Bearer API KEY GOES HERE ")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    val gson = Gson()
                    val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)

                    val base64Data = apiResponse.data
                }
            } catch (e: Exception) {
                Log.e("NetworkError", "Failed to make request", e)
            }
        }
    }
}