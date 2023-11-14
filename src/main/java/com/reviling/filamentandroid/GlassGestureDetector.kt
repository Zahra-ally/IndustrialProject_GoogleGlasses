package com.reviling.filamentandroid

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.tan

class GlassGestureDetector(context: Context, private val onGestureListener: OnGestureListener) : GestureDetector.OnGestureListener {

    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD_PX = 100
        private const val SWIPE_VELOCITY_THRESHOLD_PX = 100
        private val TAN_60_DEGREES = Math.tan(Math.toRadians(60.0))
    }

    interface OnGestureListener {
        fun onDown(e: MotionEvent): Boolean
        fun onShowPress(e: MotionEvent)
        fun onSingleTapUp(e: MotionEvent): Boolean
        fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean
        fun onLongPress(e: MotionEvent)
        fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean
        fun onGesture(gesture: Gesture): Boolean
    }

    enum class Gesture {
        TAP,
        SWIPE_FORWARD,
        SWIPE_BACKWARD,
        SWIPE_UP,
        SWIPE_DOWN
    }

    private val gestureDetector: GestureDetector = GestureDetector(context, this)

    fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        // Empty implementation
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return onGestureListener.onGesture(Gesture.TAP)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        // Empty implementation
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val deltaX = e2.x - e1.x
        val deltaY = e2.y - e1.y
        val tan = if (deltaX != 0f) Math.abs(deltaY / deltaX) else Double.MAX_VALUE

        val tolerance = 1e-6 // A small tolerance value for floating-point comparisons
        val x: Double= tan.minus(tan(Math.toRadians(60.0))) as Double;
        if (abs(x) < tolerance) {
            if (Math.abs(deltaY) < SWIPE_DISTANCE_THRESHOLD_PX || Math.abs(velocityY) < SWIPE_VELOCITY_THRESHOLD_PX) {
                return false
            } else if (deltaY < 0) {
                return onGestureListener.onGesture(Gesture.SWIPE_UP)
            } else {
                return onGestureListener.onGesture(Gesture.SWIPE_DOWN)
            }
        }
        else {
            if (Math.abs(deltaX) < SWIPE_DISTANCE_THRESHOLD_PX || Math.abs(velocityX) < SWIPE_VELOCITY_THRESHOLD_PX) {
                return false
            } else if (deltaX < 0) {
                return onGestureListener.onGesture(Gesture.SWIPE_FORWARD)
            } else {
                return onGestureListener.onGesture(Gesture.SWIPE_BACKWARD)
            }
        }
    }
}

private fun Number.minus(tan: Double): Any {
    if (this is Double && tan is Double) {
        return this - tan
    } else {
        throw UnsupportedOperationException("Subtraction not supported for these types.")
    }
}
