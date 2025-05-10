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
fun saveBase64ToFile(context: Context, fileName: String, decodedBytes: ByteArray): Boolean {
    return try {
        val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        fos.write(decodedBytes)
        fos.close()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
fun extractZip(context: Context, zipFileName: String, destinationDir: File): List<String> {
    val extractedFiles = mutableListOf<String>()

    val zipFile = File(context.filesDir, zipFileName)
    val zis = ZipInputStream(zipFile.inputStream())

    var entry = zis.nextEntry
    while (entry != null) {
        val entryName = entry.name
        val outputFile = File(destinationDir, entryName)

        if (entry.isDirectory) {
            outputFile.mkdirs()
        } else {
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { fos ->
                zis.copyTo(fos)
            }
        }

        extractedFiles.add(entryName)
        entry = zis.nextEntry
    }

    zis.closeEntry()
    zis.close()

    return extractedFiles
}
fun playAudio(context: Context, filePath: String) {
    val mediaPlayer = MediaPlayer().apply {
        try {
            setDataSource(filePath)
            prepare()
            start()
            Log.i("Playback", "Playing audio from: $filePath")
        } catch (e: IOException) {
            Log.e("Playback", "Error preparing or starting playback", e)
        } finally {
            // Optional: Release MediaPlayer after playback finishes
            setOnCompletionListener { release() }
        }
    }
}

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
                    .addHeader("Authorization", "Bearer API KEY HERE")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    val gson = Gson()
                    val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)

                    val base64Data = apiResponse.data

                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val success = saveBase64ToFile(context, "token.zip", decodedBytes)

                    if (success) {
                        Log.i("FileSave", "Saved to: ${context.filesDir.absolutePath}/token.zip")
                        extractDir = File(context.cacheDir, "extracted_audio")
                        extractDir!!.mkdirs()

                        extractedFiles = extractZip(context, "token.zip", extractDir!!)

                        Log.i("ExtractedFiles", extractedFiles!!.joinToString())
                    } else {
                        Log.e("FileSave", "Failed to save file")
                    }

                    if (extractedFiles!!.contains("token.wav")) {
                        playAudio(context, File(extractDir, "token.wav").absolutePath)
                    } else {
                        Log.e("ZIP", "No audio file found in ZIP archive")
                    }
                }
            } catch (e: Exception) {
                Log.e("NetworkError", "Failed to make request", e)
            }
        }
    }
}