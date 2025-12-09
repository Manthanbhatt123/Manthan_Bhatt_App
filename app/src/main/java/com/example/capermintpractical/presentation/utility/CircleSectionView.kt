package com.example.dpadkeryboard

import android.Manifest
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.core.graphics.toColorInt
import com.example.capermintpractical.R

class CircleSectionView(context: Context, attrs: AttributeSet?) : View(context, attrs) {


    private var shadowColor = "#1F1F1F".toColorInt()
    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val sliceStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = shadowColor
        style = Paint.Style.STROKE
        strokeWidth = 10f

        setShadowLayer(12f, 0f, 0f, shadowColor)
    }

    // arrow size in dp -> will convert in init
    private val arrowSizeDp = 25f
    private var arrowSizePx = 0
    val startAngles = floatArrayOf(270f, 0f, 90f, 180f)

    private val arrowResIds = intArrayOf(
        R.drawable.ic_arrow_up,  // replace with real ids
        R.drawable.ic_arrow_left,
        R.drawable.ic_arrow_down,
        R.drawable.ic_arrow_right
    )

    // loaded drawables cached
    private val arrowDrawables: Array<Drawable?> = arrayOfNulls(4)

    private val circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = shadowColor
        style = Paint.Style.STROKE
        strokeWidth = 15f
        setShadowLayer(20f, 0f, 8f, shadowColor)
    }

    private val slicePaths = Array(4) { Path() }
    private val centerPath = Path()
    private val gapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#2D2D2D".toColorInt()
    }

    // ⭐ UPDATED ⭐ highlight colors
    private val sectionNormalColor = "#2D2D2D".toColorInt()
    private val sectionPressedColor = "#444444".toColorInt()

    private val centerNormalColor = "#2D2D2D".toColorInt()
    private val centerPressedColor = "#555555".toColorInt()

    // ⭐ UPDATED ⭐ Tracking pressed item
    private var pressedIndex: Int = -1   // 0..3 slices, 4 center, -1 none

    private var sectionClickListener: ((Int) -> Unit)? = null

    fun setOnSectionClickListener(listener: (section: Int) -> Unit) {
        sectionClickListener = listener
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        slicePaint.color = sectionNormalColor
        gapPaint.color = Color.RED
        gapPaint.style = Paint.Style.FILL

        // convert dp to px
        arrowSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            arrowSizeDp,
            resources.displayMetrics
        ).toInt()

        // load drawables (ContextCompat) and optionally mutate/tint
        for (i in arrowResIds.indices) {
            arrowDrawables[i] = ContextCompat.getDrawable(context, arrowResIds[i])?.mutate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, old: Int, old2: Int) {
        super.onSizeChanged(w, h, old, old2)
        buildPaths()
    }

    private fun buildPaths() {
        val w = width.toFloat()
        val h = height.toFloat()
        val rOuter = min(w, h) / 2f
        val rInner = rOuter * 0.50f

        slicePaths.forEach { it.reset() }
        centerPath.reset()

        val centerX = w / 2f
        val centerY = h / 2f

        centerPath.addCircle(centerX, centerY, rInner - 20f, Path.Direction.CW)

        for (i in 0..3) {
            val p = Path()

            val a1 = Math.toRadians((startAngles[i] - 45).toDouble())
            val a2 = Math.toRadians((startAngles[i] + 45).toDouble())

            val x1 = centerX + rInner * cos(a1).toFloat()
            val y1 = centerY + rInner * sin(a1).toFloat()

            val x2 = centerX + rInner * cos(a2).toFloat()
            val y2 = centerY + rInner * sin(a2).toFloat()

            p.moveTo(centerX, centerY)
            p.lineTo(x1, y1)

            p.arcTo(
                centerX - rOuter, centerY - rOuter,
                centerX + rOuter, centerY + rOuter,
                startAngles[i] - 45,
                90f, false
            )

            p.lineTo(x2, y2)
            p.close()
            slicePaths[i] = p
        }
    }

    override fun onDraw(canvas: Canvas) {

        // ⭐ UPDATED ⭐ Draw slices with pressed/normal color
        for (i in 0..3) {
            slicePaint.color = if (pressedIndex == i) sectionPressedColor else sectionNormalColor
            canvas.drawPath(slicePaths[i], slicePaint)
            canvas.drawPath(slicePaths[i], sliceStrokePaint)
        }

        // ⬇️⬇️⬇️⬇️ ADD ARROW DRAWING BLOCK HERE (AFTER SLICE DRAW, BEFORE CLEAR GAP)
        // -------------------------------------------------------------------------

        val w = width.toFloat()
        val h = height.toFloat()
        val rOuter = min(w, h) / 2f
        val rInner = rOuter * 0.40f

        for (i in 0..3) {

            val midAngle = startAngles[i]             // 270,0,90,180
            val rad = Math.toRadians(midAngle.toDouble())

            val posRadius = (rInner + rOuter) / 1.9f    // midway between inner/outer

            val ax = centerX + posRadius * cos(rad).toFloat()
            val ay = centerY + posRadius * sin(rad).toFloat()

            val drawable = arrowDrawables[0] ?: continue

            val half = arrowSizePx / 2
            drawable.setBounds(-half, -half, half, half)

            val rotation = midAngle + 90f             // rotates UP arrow to outward

            canvas.save()
            canvas.translate(ax, ay)
            canvas.rotate(rotation)
            drawable.draw(canvas)
            canvas.restore()
        }

        // -------------------------------------------------------------------------
        // ⬆️⬆️⬆️⬆️ END OF ARROW DRAWING BLOCK

        // Clear gap
        gapPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawPath(centerPath, gapPaint)
        gapPaint.xfermode = null

        // ⭐ UPDATED ⭐ draw center with pressed color
        centerPaint.color = if (pressedIndex == 4) centerPressedColor else centerNormalColor

        canvas.drawPath(centerPath, centerPaint)
        canvas.drawPath(centerPath, circleStrokePaint)

        // Draw OK text
        val txt = "OK"
        val bounds = Rect()
        val tPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tPaint.textSize = 60f
        tPaint.color = Color.WHITE
        tPaint.getTextBounds(txt, 0, txt.length, bounds)

        canvas.drawText(
            txt,
            centerX - bounds.exactCenterX(),
            centerY - bounds.exactCenterY(),
            tPaint
        )
    }

    private val centerX get() = width / 2f
    private val centerY get() = height / 2f

    // ⭐ FULLY UPDATED TOUCH SYSTEM ⭐
    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        val clip = Region(0, 0, width, height)

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                // Check center first
                val centerRegion = Region().apply { setPath(centerPath, clip) }
                if (centerRegion.contains(x, y)) {
                    vibrate()
                    pressedIndex = 4
                    invalidate()
                    return true
                }

                // Check slices
                for (i in 0..3) {
                    val reg = Region().apply { setPath(slicePaths[i], clip) }
                    if (reg.contains(x, y)) {
                        vibrate()
                        pressedIndex = i

                        invalidate()
                        return true
                    }
                }

                pressedIndex = -1
                invalidate()

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (pressedIndex == -1) return true

                val region = when (pressedIndex) {
                    4 -> Region().apply { setPath(centerPath, clip) }
                    else -> Region().apply { setPath(slicePaths[pressedIndex], clip) }
                }

                if (!region.contains(x, y)) {
                    pressedIndex = -1
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (pressedIndex == -1) {
                    invalidate()
                    return true
                }

                val region = when (pressedIndex) {
                    4 -> Region().apply { setPath(centerPath, clip) }
                    else -> Region().apply { setPath(slicePaths[pressedIndex], clip) }
                }

                val finalIndex = pressedIndex
                pressedIndex = -1
                invalidate()

                if (region.contains(x, y)) {
                    sectionClickListener?.invoke(finalIndex)
                }

                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                pressedIndex = -1
                invalidate()
                return true
            }
        }

        return true
    }
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate(){

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    50,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(50)
        }
    }
}
