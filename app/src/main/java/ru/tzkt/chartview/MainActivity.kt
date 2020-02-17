package ru.tzkt.chartview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartView.setData(loadFromAssets())
        chartView.invalidate()
        testButton.setOnClickListener { chartView.removeOnePath() }
    }

    private fun loadFromAssets(): ChartData {
        val inputStream = assets.open("chart_data.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val json = String(buffer)

        return parseChartData(json)
    }

    private fun parseChartData(jsonString: String): ChartData {
        val jsonArray = JSONArray(jsonString)
        val json = jsonArray[0] as JSONObject
        val columnsJson = json.getJSONArray("columns")
        val colorsJson = json.getJSONObject("colors")
        val typesJson = json.getJSONObject("types")
        val namesJson = json.getJSONObject("names")

        // columns
        // x
        val xJsonArray = columnsJson[0] as JSONArray
        val xValues = mutableListOf<Long>()
        for (i in 1 until xJsonArray.length()) {
            xValues.add(xJsonArray[i].toString().slice(0..9).toLong())
        }
        val minx = xValues.min()!!
        val maxx = xValues.max()!!
        val diffx = maxx - minx
        // y0
        val y0JsonArray = columnsJson[1] as JSONArray
        val y0Values = mutableListOf<Int>()
        for (i in 1 until y0JsonArray.length()) {
            y0Values.add(y0JsonArray[i] as Int)
        }
        val maxy0 = y0Values.max()!!
        val miny0 = y0Values.min()!!
        val diffy0 = maxy0 - miny0

        // y1
        val y1JsonArray = columnsJson[2] as JSONArray
        val y1Values = mutableListOf<Int>()
        for (i in 1 until y1JsonArray.length()) {
            y1Values.add(y1JsonArray[i] as Int)
        }

        val maxy1 = y1Values.max()!!
        val miny1 = y1Values.min()!!
        val diffy1 = maxy1 - miny1

        val minTotal = min(miny0, miny1)
        val maxTotal = max(maxy0, maxy1)

        val maxDiffY = maxTotal - minTotal

        // colors
        val y0Color = colorsJson.getString("y0")
        val y1Color = colorsJson.getString("y1")
        val colors = Colors(y0Color, y1Color)

        // names
        val y0Name = namesJson.getString("y0")
        val y1Name = namesJson.getString("y1")
        val names = Names(y0Name, y1Name)

        // types
        val y0Type = typesJson.getString("y0")
        val y1Type = typesJson.getString("y1")
        val xType = typesJson.getString("x")
        val types = Types(y0Type, y1Type, xType)

        return ChartData(
            xValues,
            y0Values,
            y1Values,
            colors,
            names,
            types,
            diffy0,
            diffy1,
            maxDiffY,
            diffx,
            minx,
            maxx
        )
    }
}
