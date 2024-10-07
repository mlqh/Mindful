package com.ece452s24g7.mindful.activities.decorators

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

// unused for now, may reimplement
class MultiDotSpan(
    private val radius: Float,
    private val colors: List<Int>,
    private val numDots: Int
) : LineBackgroundSpan {

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int
    ) {
        val dotSpacing = 20
        val totalWidth = dotSpacing * (numDots - 1)
        var offset = -totalWidth / 2
        val oldColor = paint.color

        for (i in 0 until numDots) {
            paint.color = colors[i % colors.size]
            canvas.drawCircle(
            ((left + right) / 2 + offset).toFloat(),
            bottom + radius,
            radius,
            paint
            )
            offset += dotSpacing
        }
        paint.color = oldColor
    }
}