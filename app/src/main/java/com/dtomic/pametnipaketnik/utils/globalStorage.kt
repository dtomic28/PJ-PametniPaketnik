package com.dtomic.pametnipaketnik.utils

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
}