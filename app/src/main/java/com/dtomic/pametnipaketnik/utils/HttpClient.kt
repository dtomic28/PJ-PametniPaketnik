package com.dtomic.pametnipaketnik.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpClientWrapper {
    private val baseUrl = "http://192.168.64.8:3001/"
    private val baseApiUrl = "${baseUrl}api/"
    private var bearerToken: String? = null

    val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()

    fun getBaseUrl() : String {
        return baseUrl
    }
    fun clearBearerToken() {
        bearerToken = null
    }
    fun setBearerToken(token: String) {
        bearerToken = token
    }

    fun postImageFromUri(
        endpoint: String,
        context: Context,
        uri: Uri,
        formFieldName: String = "image",
        fileName: String = "upload.jpg",
        mimeType: String = "image/jpeg",
        headers: Map<String, String> = emptyMap(),
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        val bytes = try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }

        if (bytes == null) {
            callback(false, "Failed to read image bytes")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                formFieldName,
                fileName,
                bytes.toRequestBody(mimeType.toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(baseUrl + endpoint)
            .apply {
                headers.forEach { (k, v) -> addHeader(k, v) }
                bearerToken?.let { addHeader("Authorization", "Bearer $it") }
            }
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful, response.body?.string())
            }
        })
    }

    fun get(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        Log.d("HttpClientWrapper GET", "URL: $baseApiUrl$endpoint");
        val request = Request.Builder()
            .url(baseApiUrl + endpoint)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
                bearerToken?.let { addHeader("Authorization", "Bearer $it") }
            }
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful, response.body?.string())
            }
        })
    }

    fun postJson(
        endpoint: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap(),
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        Log.d("HttpClientWrapper POST", "URL: $baseApiUrl$endpoint")
        Log.d("POST BODY", jsonBody)
        val request = Request.Builder()
            .url(baseApiUrl + endpoint)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
                bearerToken?.let { addHeader("authorization", "Bearer $it") }
            }
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful, response.body?.string())
            }
        })
    }

    fun postFile(
        endpoint: String,
        file: File,
        fieldName: String = "file",
        mimeType: String = "image/zip",
        headers: Map<String, String> = emptyMap(),
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                fieldName,
                file.name,
                file.asRequestBody(mimeType.toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(baseApiUrl + endpoint)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
                bearerToken?.let { addHeader("Authorization", "Bearer $it") }
            }
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful, response.body?.string())
            }
        })
    }
}
