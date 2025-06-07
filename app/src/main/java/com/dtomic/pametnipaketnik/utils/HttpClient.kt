package com.dtomic.pametnipaketnik.utils

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object HttpClientWrapper {

    //private val baseUrl = "https://pp.dtomic.com/"
    private val baseUrl = "http://192.168.64.14:3001/"
    private val baseApiUrl = "${baseUrl}api/"
    private val client = OkHttpClient()
    private var bearerToken: String? = null

    fun getBaseUrl() : String {
        return baseUrl
    }

    fun clearBearerToken() {
        bearerToken = null
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

    fun setBearerToken(token: String) {
        bearerToken = token
    }
}
