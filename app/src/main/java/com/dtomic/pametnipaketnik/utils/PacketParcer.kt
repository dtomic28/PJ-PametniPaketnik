package com.dtomic.pametnipaketnik.utils

import android.content.Context
import com.dtomic.core.TSPData
import java.io.File


class PacketParcer(private val context: Context) {

    var lines: List<String> = listOf()

    fun fromCoords(indexes: Array<Int>): TSPData {
        require(DataCache.isReady(context)) {
            "Cache files missing. Download distances/durations/lat/lon first."
        }
        val coords : Array<TSPData.Coord> = Array(indexes.size) { TSPData.Coord(0.0,0.0) }
        val latLines = DataCache.file(context, DataCache.LATITUDES).readLines()
        val lonLines = DataCache.file(context, DataCache.LONGITUDES).readLines()
        var i = 0
        for ( index in indexes) {
            coords[i] = TSPData.Coord(
                latLines[index].toDouble(),
                lonLines[index].toDouble()
            )
            i++
        }

        return TSPData("PacketData", indexes.size, coords, null)
    }

    fun fromDistances(indexes: Array<Int>): TSPData {
        require(DataCache.isReady(context)) {
            "Cache files missing. Download distances/durations/lat/lon first."
        }
        val w = IntArray(indexes.size * indexes.size)
        val matrix = DataCache.file(context, DataCache.DISTANCES).readLines()
            .map { line ->
            line.split(" ").map { it.toInt() }
        }

        for( row in indexes){
            for( col in indexes){
                w[row * indexes.size + col] = matrix[row][col]
            }
        }

        return TSPData("PacketData", indexes.size, null, w)


    }

    fun fromDuractions(indexes: Array<Int>): TSPData {
        require(DataCache.isReady(context)) {
            "Cache files missing. Download distances/durations/lat/lon first."
        }
        val w = IntArray(indexes.size * indexes.size)

        val matrix = DataCache.file(context, DataCache.DURATIONS).readLines().map { line ->
            line.split(" ").map { it.toInt() }
        }

        for (row in indexes) {
            for (col in indexes) {
                w[row * indexes.size + col] = matrix[row][col]
            }
        }

        return TSPData("PacketData", indexes.size, null, w)
    }
}