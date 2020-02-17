package ru.tzkt.chartview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

private const val ZERO_POINT = 0f
private const val BACKGROUND_ALPHA = 150
private const val BACKGROUND_RED = 0
private const val BACKGROUND_GREEN = 0
private const val BACKGROUND_BLUE = 0

class CameraView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }
    private val backgroundColor = Color.argb(
        BACKGROUND_ALPHA,
        BACKGROUND_RED,
        BACKGROUND_GREEN,
        BACKGROUND_BLUE
    )
    private val paint = Paint().apply {
        strokeWidth = 5f
        color = Color.DKGRAY
        style = Paint.Style.STROKE
    }
    private val paintTransparent = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }
    private val innerRectangle = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)

        canvas.drawRect(innerRectangle, paintTransparent)
        canvas.drawRect(innerRectangle, paint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        val halfInner = (width / 2 - width / 8f)
        val innerBottom = height / 2f + halfInner
        val innerTop = height / 2f - halfInner

        with(innerRectangle) {
            top = innerTop
            bottom = innerBottom
            left = width / 8f
            right = width / 8f * 7
        }
    }
}