package com.reviling.filamentandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val dataPoints = mutableListOf<Pair<Float, Float>>() // (Time, Value) pairs
    private val linePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
    }

    fun addDataPoint(time: Float, value: Float) {
        dataPoints.add(Pair(time, value))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty()) return

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val maxX = dataPoints.maxOf { it.first }
        val maxY = dataPoints.maxOf { it.second }
        val minX = dataPoints.minOf { it.first }
        val minY = dataPoints.minOf { it.second }

        val scaleX = width / (maxX - minX)
        val scaleY = height / (maxY - minY)

        val translatedPoints = dataPoints.map { Pair((it.first - minX) * scaleX, height - (it.second - minY) * scaleY) }

        var prevPoint: Pair<Float, Float>? = null
        for (point in translatedPoints) {
            if (prevPoint != null) {
                canvas.drawLine(prevPoint.first, prevPoint.second, point.first, point.second, linePaint)
            }
            prevPoint = point
        }
    }
}
