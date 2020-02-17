package ru.tzkt.chartview

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

private const val TOUCH_DELTA = 20f
private const val BOTTOM_CONTROL_BORDER = 24f

class ChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private lateinit var data: ChartData
    private val bottomControl = BottomControl()
    private val chart = Chart()
    private val grid = Grid()
    private var touchedArea = TouchArea.NONE

    // paints
    private val painty0 = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
        pathEffect = CornerPathEffect(8f)
    }

    private val painty1 = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8f
        pathEffect = CornerPathEffect(8f)
    }

    private val painty0Bottom = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = CornerPathEffect(2f)
    }

    private val painty1Bottom = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = CornerPathEffect(2f)
    }

    private val paintAlphaBottom = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BAD4DA")
        alpha = 60
    }

    private val paintAlphaBottomDark = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#88A8B0")
        alpha = 60
    }

    private val paintGrid = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A3AFC3")
        textSize = 60f
    }

    private val current = PointF()

    fun setData(data: ChartData) {
        this.data = data
        bottomControl.startVal = data.minx
        bottomControl.endVal = data.maxx

        val y0Color = Color.parseColor(data.colors.y0)
        val y1Color = Color.parseColor(data.colors.y1)

        painty0.color = y0Color
        painty1.color = y1Color
        painty0Bottom.color = y0Color
        painty1Bottom.color = y1Color

        invalidate()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        with(chart) {
            height = h * 6 / 8
            width = w
            yratio = height / data.diffy.toFloat()
        }

        with(bottomControl) {
            width = w
            height = h / 8
            startY = (h - height).toFloat()
            yratio = height / data.diffy.toFloat()
            xratio = width / data.diffx.toFloat()
        }
        with(grid) {
            height = chart.height / 10 * 9 / 5
        }
        calculateChart(bottomControl)
    }


    private fun normalize(height: Int, value: Float): Float {
        return when {
            value < 0 -> 0f
            value > height -> height.toFloat()
            else -> value
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawGrid(canvas)
        drawBottomControl(canvas)
        drawChart(canvas)
    }

    private fun drawBottomControl(canvas: Canvas?) {
        with(bottomControl) {
            canvas?.translate(startX, startY)
            canvas?.drawPath(path0, painty0Bottom)
            canvas?.drawPath(path1, painty1Bottom)

            canvas?.drawRect(
                startX,
                0f,
                startTransparent,
                height.toFloat(),
                paintAlphaBottom
            )
            canvas?.drawRect(
                endTransparent,
                0f,
                width.toFloat(),
                height.toFloat(),
                paintAlphaBottom
            )
            canvas?.drawRect(
                startTransparent,
                0f,
                startTransparent + BOTTOM_CONTROL_BORDER,
                height.toFloat(),
                paintAlphaBottomDark
            )
            canvas?.drawRect(
                endTransparent - BOTTOM_CONTROL_BORDER,
                0f,
                endTransparent,
                height.toFloat(),
                paintAlphaBottomDark
            )
            canvas?.drawRect(
                startTransparent + BOTTOM_CONTROL_BORDER,
                0f,
                endTransparent - BOTTOM_CONTROL_BORDER,
                4f,
                paintAlphaBottomDark
            )
            canvas?.drawRect(
                startTransparent + BOTTOM_CONTROL_BORDER,
                height.toFloat() - 4f,
                endTransparent - BOTTOM_CONTROL_BORDER,
                height.toFloat(),
                paintAlphaBottomDark
            )
        }
    }

    private fun drawChart(canvas: Canvas?) {
        with(chart) {
            canvas?.translate(-bottomControl.startX, -bottomControl.startY)
            canvas?.drawPath(path0, painty0)
            canvas?.drawPath(path1, painty1)
        }
    }

    private fun drawGrid(canvas: Canvas?) {
        canvas ?: return
        with(canvas) {
            var curY = chart.height.toFloat()
            var cury0  = grid.animatedHeight0
            for (i in 0 until 6) {
                drawLine(0f, curY, width.toFloat(), curY, paintGrid)

                // anim
                drawLine(0f, curY, width.toFloat(), curY, paintGrid)


                drawText(grid.yValues[i].toString(), 10f, curY - 20f, paintGrid)
                curY -= grid.height
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        with(bottomControl) {
            when {
                endTransparent >= width -> {
                    endTransparent = width.toFloat()
                    current.set(current.x - 1, event.y)
                }
                startTransparent <= 0 -> {
                    startTransparent = 0f
                    current.set(current.x + 1, event.y)
                }
                else -> current.set(event.x, event.y)
            }
        }
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                with(bottomControl) {
                    when {
                        isTransparentAreaTouched() -> {
                            touchedArea = TouchArea.CENTER
                            deltaTransparent = current.x - startTransparent
                            transparentWidth = endTransparent - startTransparent
                        }
                        isLeftTransparentEdgeTouched() -> {
                            touchedArea = TouchArea.LEFT_EDGE
                            deltaLeft = current.x - startTransparent
                        }
                        isRightTransparentEdgeTouched() -> {
                            touchedArea = TouchArea.RIGHT_EDGE
                            deltaRight - current.x - endTransparent
                        }
                        else -> {
                            touchedArea = TouchArea.NONE
                        }
                    }
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                handleMove()
                true
            }
            MotionEvent.ACTION_UP -> {
                touchedArea = TouchArea.NONE
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun handleMove() {
        when (touchedArea) {
            TouchArea.CENTER -> moveTransparentArea()
            TouchArea.LEFT_EDGE -> moveLeftEdge()
            TouchArea.RIGHT_EDGE -> moveRightEdge()
            TouchArea.NONE -> { // do nothing
            }
        }
    }

    private fun moveLeftEdge() {
        bottomControl.startTransparent = current.x - bottomControl.deltaLeft
        calculateChart()
        invalidate()
    }

    fun removeOnePath() {
        val animator = ObjectAnimator.ofInt(100, 0)
        animator.duration = 500
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val percent = animation.animatedValue as Int
            painty0.alpha = percent

            with(chart) {
                path0.reset()
                path0.moveTo(points0[0].x, points0[0].y)

                for (point in points0) {
                    path0.lineTo(point.x, (point.y - ((100 - percent) * 10)) * 2)
                }
            }

            invalidate()
        }
        animator.start()
    }

    private fun calculateChart() {
        chart.startVal =
            data.minx + (bottomControl.startTransparent / bottomControl.width * data.diffx).toLong()
        chart.endVal =
            data.minx + (bottomControl.endTransparent / bottomControl.width * data.diffx).toLong()
        calculateChart(chart, true)
    }

    private fun calculateChart(ichart: IChart, isRatioChangeable: Boolean = false) {
        with(ichart) {
            path0.reset()
            path1.reset()
            points0.clear()

            var fromIndex = data.x.indexOfLast { it <= startVal }
            if (fromIndex == -1) {
                fromIndex = 0
            }
            var toIndex = data.x.indexOfFirst { it >= endVal }
            if (toIndex == -1) {
                toIndex = data.x.size - 1
            }

            var max0 = 0
            var max1 = 0

            for (i in fromIndex until toIndex) {
                if (data.y0[i] > max0) {
                    max0 = data.y0[i]
                }
                if (data.y1[i] > max1) {
                    max1 = data.y1[i]
                }
            }

            val totalMax = Math.max(max0, max1)
            val rat = grid.curMax / totalMax.toFloat()
            if (rat > 1.5f || rat < 0.7f) {
                val oldMax = grid.curMax
                grid.curMax = totalMax
                calculateGrid()
                animateGrid(oldMax)
                if (isRatioChangeable) {
                    val oldRatio = yratio
                    val newRatio = chart.height / totalMax.toFloat()
                    animateRatio(ichart, oldRatio, newRatio)
                }
            }

            if (isRatioChangeable) {
                val diffx = endVal - startVal
                xratio = width / diffx.toFloat()
            }

            val norm0 = height - data.y0[fromIndex] * yratio
            val norm1 = height - data.y1[fromIndex] * yratio
            path0.moveTo(0f, norm0)
            path1.moveTo(0f, norm1)

            val p0 = PointF(0f, norm0)
            val p1 = PointF(0f, norm1)

            points0.add(p0)
            points1.add(p1)

            for (i in fromIndex + 1 until toIndex) {
                val xCoordi = (data.x[i] - startVal) * xratio
                val normalized0 = normalize(height, height - data.y0[i] * yratio)
                val normalized1 = normalize(height, height - data.y1[i] * yratio)

                val point0 = PointF(xCoordi, normalized0)
                val point1 = PointF(xCoordi, normalized1)

                points0.add(point0)
                points1.add(point1)

                path0.lineTo(point0.x, point0.y)
                path1.lineTo(point1.x, point1.y)
            }
        }
    }

    private fun animateGrid(oldMax: Int) {
        val anim1 = ObjectAnimator.ofFloat(1f, 3f)
        anim1.addUpdateListener { animation ->
            grid.animatedHeight1 = ((animation.animatedValue as Float) * height).toInt()
            invalidate()
        }
        val anim2 = ObjectAnimator.ofFloat(0.5f, 1f)
        anim2.addUpdateListener { animation ->
            grid.animatedHeight0 = ((animation.animatedValue as Float) * height).toInt()
            invalidate()
        }
        anim1.start()
        anim2.start()
    }

    private fun animateRatio(chart: IChart, oldRatio: Float, newRatio: Float) {
        val animator = ObjectAnimator.ofFloat(oldRatio, newRatio)
        animator.addUpdateListener { animation ->
            chart.yratio = animation.animatedValue as Float
            calculateChart(chart)
            invalidate()
        }
        animator.start()
    }

    private fun calculateGrid() {
        grid.yValues.clear()
        val step = grid.curMax / 5
        var curVal = 0
        for (i in 0 until 6) {
            grid.yValues.add(curVal)
            curVal += step
        }
    }


    private fun moveRightEdge() {
        bottomControl.endTransparent = current.x - bottomControl.deltaRight
        calculateChart()
        invalidate()
    }

    private fun moveTransparentArea() {
        with(bottomControl) {
            startTransparent = current.x - deltaTransparent
            endTransparent = startTransparent + transparentWidth
            calculateChart()
            invalidate()
        }
    }

    private fun isTransparentAreaTouched(): Boolean {
        return current.x < bottomControl.endTransparent - TOUCH_DELTA
                && current.x > bottomControl.startTransparent + TOUCH_DELTA
                && current.y > bottomControl.startY + TOUCH_DELTA
                && current.y < bottomControl.startY + height - TOUCH_DELTA
    }

    private fun isLeftTransparentEdgeTouched(): Boolean {
        return current.x < bottomControl.startTransparent + TOUCH_DELTA
                && current.x > bottomControl.startTransparent - TOUCH_DELTA
                && current.y > bottomControl.startY + TOUCH_DELTA
                && current.y < bottomControl.startY + height - TOUCH_DELTA
    }

    private fun isRightTransparentEdgeTouched(): Boolean {
        return current.x < bottomControl.endTransparent + TOUCH_DELTA
                && current.x > bottomControl.endTransparent - TOUCH_DELTA
                && current.y > bottomControl.startY + TOUCH_DELTA
                && current.y < bottomControl.startY + height - TOUCH_DELTA
    }

    private data class Grid(
        var curMax: Int = 0,
        var height: Int = 0,
        var animatedHeight0: Int = 0,
        var animatedHeight1: Int = 0,
        var yValues: MutableList<Int> = mutableListOf()
    )

    private enum class TouchArea {
        LEFT_EDGE, RIGHT_EDGE, CENTER, NONE
    }

    private interface IChart {
        var startVal: Long
        var endVal: Long
        var width: Int
        var height: Int
        var xratio: Float
        var yratio: Float
        val path0: Path
        val path1: Path
        val points0: MutableList<PointF>
        val points1: MutableList<PointF>
    }

    private data class Chart(
        override var endVal: Long = 0,
        override var startVal: Long = 0,
        override var height: Int = 0,
        override var width: Int = 0,
        override var xratio: Float = 0f,
        override var yratio: Float = 0f,
        override val path0: Path = Path(),
        override val path1: Path = Path(),
        override val points0: MutableList<PointF> = mutableListOf(),
        override val points1: MutableList<PointF> = mutableListOf()
    ) : IChart

    private data class BottomControl(
        override var endVal: Long = 0,
        override var startVal: Long = 0,
        var startX: Float = 0f,
        var startY: Float = 0f,
        override var xratio: Float = 0f,
        override var yratio: Float = 0f,
        override val path0: Path = Path(),
        override val path1: Path = Path(),
        override var width: Int = 0,
        override var height: Int = 0,
        var startTransparent: Float = 100f,
        var endTransparent: Float = 400f,
        var transparentWidth: Float = 0f,
        var deltaTransparent: Float = 0f,
        var deltaLeft: Float = 0f,
        var deltaRight: Float = 0f,
        override val points0: MutableList<PointF> = mutableListOf(),
        override val points1: MutableList<PointF> = mutableListOf()
    ) : IChart
}