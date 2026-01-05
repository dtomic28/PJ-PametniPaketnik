package com.dtomic.pametnipaketnik.utils

import com.dtomic.core.TSPData
import java.io.File

class PacketParcer {
    var lines: List<String> = listOf()

    fun fromCoords(indexes: Array<Int>): TSPData {
        val coords : Array<TSPData.Coord> = Array(indexes.size) { TSPData.Coord(0.0,0.0) }
        val latLines: List<String> = File("app/src/main/java/com/dtomic/pametnipaketnik/utils/latitudes.txt").readLines()
        val lonLines: List<String> = File("app/src/main/java/com/dtomic/pametnipaketnik/utils/longitudes.txt").readLines()
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
        val w = IntArray(indexes.size * indexes.size)
        val matrix: List<List<Int>> = File("app/src/main/java/com/dtomic/pametnipaketnik/utils/distamces.txt").readLines()
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
        val w = IntArray(indexes.size * indexes.size)
        val matrix: List<List<Int>> = File("app/src/main/java/com/dtomic/pametnipaketnik/utils/durations.txt").readLines()
            .map { line ->
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