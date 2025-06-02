package com.dtomic.pametnipaketnik.utils

import android.R.attr.text
import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

fun saveBase64ToFile(context: android.content.Context, fileName: String, decodedBytes: ByteArray): Boolean {
    return try {
        val fos: FileOutputStream = context.openFileOutput(fileName, android.content.Context.MODE_PRIVATE)
        fos.write(decodedBytes)
        fos.close()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

fun extractZip(context: android.content.Context, zipFileName: String, destinationDir: File): List<String> {
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

fun playAudio(context: android.content.Context, filePath: String) {
    val mediaPlayer = android.media.MediaPlayer().apply {
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

fun registerUser(
    username: String,
    email: String,
    password: String,
    repeatPassword: String
): String {
    val emailRegex = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")

    return if (password != repeatPassword) {
        "Passwords don't match!"
    } else if (username == "Tilen") { //TODO IMPLEMENT LATER
        "Username has already been taken!"
    } else if (!emailRegex.containsMatchIn(email)) {
        "Not a valid email!"
    }
    else {
        //TODO REGISTER USER
        ""
    }
}