package com.dtomic.pametnipaketnik.utils

import android.content.Context
import java.io.File

object DataCache {
    private const val DIR = "DataCache"

    const val DISTANCES = "distances.txt"
    const val DURATIONS = "durations.txt"
    const val LATITUDES = "latitudes.txt"
    const val LONGITUDES = "longitudes.txt"

    private fun dir(context: Context): File =
        File(context.filesDir, DIR).apply { mkdirs() }

    fun file(context: Context, name: String): File = File(dir(context), name)

    fun isReady(context: Context): Boolean =
        listOf(DISTANCES, DURATIONS, LATITUDES, LONGITUDES)
            .map { file(context, it) }
            .all { it.exists() && it.length() > 0L }
}