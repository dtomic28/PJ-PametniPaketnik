package com.dtomic.pametnipaketnik.utils

import android.R.attr.text
import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
            setOnCompletionListener { release() }
        }
    }
}

fun playToken(boxId : String, context: Context) {
    val context = context
    try {

        HttpClientWrapper.get("token/requestToken/$boxId") { success, responseBody ->
            if (success && responseBody != null) {
                val gson = Gson()
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
                        playAudio(context, File(extractDir, "token.wav").absolutePath)
                        Log.d("TILEN", "should have played sound")
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("RequestError", "Exception in HTTP request", e)
    }
}