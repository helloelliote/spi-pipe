package kr.djspi.pipe01.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min
import kotlin.math.sqrt

@SuppressLint("ClickableViewAccessibility")
class ZoomableImageView : AppCompatImageView {
    private val matrix = Matrix()
    private val last = PointF()
    private val start = PointF()
    private var mode = NONE
    private var oldDistance = 1f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        isClickable = true
        scaleType = ScaleType.MATRIX
        imageMatrix = matrix/*setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(event.x, event.y)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDistance = spacing(event)
                    if (oldDistance > 10f) {
                        matrix.set(savedMatrix)
                        midPoint(mid, event)
                        mode = ZOOM
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mode == ZOOM) {
                        val newDistance = spacing(event)
                        if (newDistance > 10f) {
                            val scale = newDistance / oldDistance
                            matrix.set(savedMatrix)
                            matrix.postScale(scale, scale, mid.x, mid.y)
                        }
                    } else if (mode == DRAG) {
                        val dx = event.x - last.x
                        val dy = event.y - last.y
                        matrix.postTranslate(dx, dy)
                        last.set(event.x, event.y)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                    savedMatrix.set(matrix)
                }
            }
            imageMatrix = matrix
            true
        }*/
        setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(matrix)
                    last.set(event.x, event.y)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDistance = spacing(event)
                    if (oldDistance > 10f) {
                        savedMatrix.set(matrix)
                        midPoint(mid, event)
                        mode = ZOOM
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mode == ZOOM) {
                        val newDistance = spacing(event)
                        if (newDistance > 10f) {
                            matrix.set(savedMatrix)
                            val scale = newDistance / oldDistance
                            matrix.postScale(scale, scale, mid.x, mid.y)
                        }
                    } else if (mode == DRAG) {
                        val dx = event.x - last.x
                        val dy = event.y - last.y
                        matrix.postTranslate(dx, dy)
                        last.set(event.x, event.y)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }
            }

            imageMatrix = matrix
            true
        }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    // Add this method to calculate the initial scale
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val viewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val viewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        val drawable = drawable as? BitmapDrawable ?: return
        val bitmap: Bitmap = drawable.bitmap
        val imgWidth = bitmap.width.toFloat()
        val imgHeight = bitmap.height.toFloat()

        val widthRatio = viewWidth / imgWidth
        val heightRatio = viewHeight / imgHeight
        val initialScale = min(widthRatio, heightRatio)

        matrix.setScale(initialScale, initialScale)
        matrix.postTranslate((viewWidth - imgWidth * initialScale) / 2, (viewHeight - imgHeight * initialScale) / 2)
        imageMatrix = matrix
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    private var savedMatrix = Matrix()
    private var mid = PointF()
}
