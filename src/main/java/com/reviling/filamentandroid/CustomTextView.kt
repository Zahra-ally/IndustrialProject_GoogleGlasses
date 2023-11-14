package com.reviling.filamentandroid
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomTextView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val defaultTextColor = Color.BLACK
    private val defaultBackgroundColor = Color.WHITE
    private val defaultTextSize = 24f

    private var text: String = ""
    private var textColor: Int = defaultTextColor
    private var backgroundColor: Int = defaultBackgroundColor
    private var textSize: Float = defaultTextSize

    private val paint = Paint()

    init {
        // Load custom attributes from XML (if any)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView)
        text = typedArray.getString(R.styleable.CustomTextView_customText) ?: ""
        textColor = typedArray.getColor(R.styleable.CustomTextView_customTextColor, defaultTextColor)
        backgroundColor = typedArray.getColor(R.styleable.CustomTextView_customBackgroundColor, defaultBackgroundColor)
        textSize = typedArray.getDimension(R.styleable.CustomTextView_customTextSize, defaultTextSize)
        typedArray.recycle()

        // Set up the Paint object for drawing
        paint.color = textColor
        paint.textSize = textSize
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw the background
        paint.color = backgroundColor
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw the text in the center
        paint.color = textColor
        canvas?.drawText(text, width / 2f, height / 2f, paint)
    }

    fun setText(newText: String) {
        text = newText
        invalidate() // Redraw the view with the updated text
    }
}
