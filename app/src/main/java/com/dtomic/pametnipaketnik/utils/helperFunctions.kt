package com.dtomic.pametnipaketnik.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import androidx.core.graphics.scale

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

fun uriToBase64(context: Context, uri: Uri?): String? {
    if (uri == null) return null

    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val resizedBitmap = originalBitmap.scale(128, 128)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)

        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}