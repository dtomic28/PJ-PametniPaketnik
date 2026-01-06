package com.dtomic.pametnipaketnik.utils

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object globalStorage {
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView
    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun getGoogleAPIKey(context: Context): String {
        val appInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)

        return appInfo.metaData
            ?.getString("com.google.android.geo.API_KEY")
            ?: error("Google Maps API key not found in manifest")
    }
}