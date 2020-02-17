package ru.tzkt.chartview

data class ChartData(
    val x: List<Long>,
    val y0: List<Int>,
    val y1: List<Int>,
    val colors: Colors,
    val names: Names,
    val types: Types,
    val diffy0: Int,
    val diffy1: Int,
    val diffy: Int,
    val diffx: Long,
    val minx: Long,
    val maxx: Long
)

data class Colors(
    val y0: String,
    val y1: String
)

data class Types(
    val y0: String,
    val y1: String,
    val x: String
)

data class Names(
    val y0: String,
    val y1: String
)