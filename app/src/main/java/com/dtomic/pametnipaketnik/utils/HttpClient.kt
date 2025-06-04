package com.dtomic.pametnipaketnik.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class HttpClientWrapper {

    //private val baseUrl = "https://pp.dtomic.com/api/"
    private val baseUrl = "http://192.168.1.45:3001/api/"
    private val client = OkHttpClient()

    fun get(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        val request = Request.Builder()
            .url(baseUrl + endpoint)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
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

        val request = Request.Builder()
            .url(baseUrl + endpoint)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
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
}
