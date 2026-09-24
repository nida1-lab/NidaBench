package com.nida.bench

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.*
import android.content.Context
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(BenchmarkView(this))
    }
}

class BenchmarkView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var running = false

    private var startTime = 0L
    private var frameCount = 0L

    private var fps = 0.0
    private var score = 0L

    private var rotation = 0f

    private val particles = ArrayList<Particle>()

    init {

        paint.typeface = Typeface.create(
            Typeface.DEFAULT,
            Typeface.NORMAL
        )

        repeat(500) {

            particles.add(
                Particle(
                    Random.nextFloat(),
                    Random.nextFloat(),
                    Random.nextFloat() * 2f + 1f,
                    Random.nextFloat() * 360f
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        if (!running) {

            drawHome(canvas)

            return
        }

        drawBenchmark(canvas)

        frameCount++

        val elapsed = System.currentTimeMillis() - startTime

        if (elapsed > 1000) {

            fps =
                frameCount.toDouble() /
                (elapsed.toDouble() / 1000.0)

            score += fps.toLong()

            invalidate()
        } else {

            invalidate()
        }
    }

    private fun drawHome(canvas: Canvas) {

        canvas.drawColor(Color.WHITE)

        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER

        paint.textSize = 72f

        canvas.drawText(
            "NIDA BENCH",
            width / 2f,
            height / 3f,
            paint
        )

        paint.textSize = 42f

        canvas.drawText(
            "GPU TEST",
            width / 2f,
            height / 3f + 90f,
            paint
        )

        paint.color = Color.rgb(0, 128, 255)

        canvas.drawRoundRect(
            width / 2f - 260f,
            height * 0.65f,
            width / 2f + 260f,
            height * 0.65f + 120f,
            30f,
            30f,
            paint
        )

        paint.color = Color.WHITE
        paint.textSize = 36f

        canvas.drawText(
            "START",
            width / 2f,
            height * 0.65f + 76f,
            paint
        )

        paint.color = Color.DKGRAY
        paint.textSize = 24f

        canvas.drawText(
            "GPU描画性能を測定します",
            width / 2f,
            height * 0.65f + 180f,
            paint
        )
    }

    private fun drawBenchmark(canvas: Canvas) {

        canvas.drawColor(Color.rgb(5, 5, 15))

        val centerX = width / 2f
        val centerY = height / 2f

        rotation += 2f

        // Large rotating objects

        for (i in 0 until 80) {

            val angle =
                Math.toRadians(
                    rotation.toDouble() +
                    i * 4.5
                )

            val radius =
                100f + (i % 10) * 45f

            val x =
                centerX +
                cos(angle).toFloat() * radius

            val y =
                centerY +
                sin(angle).toFloat() * radius

            paint.color = Color.rgb(
                (50 + i * 2) % 255,
                (100 + i * 3) % 255,
                (150 + i * 5) % 255
            )

            paint.style = Paint.Style.FILL

            canvas.drawCircle(
                x,
                y,
                18f + (i % 8) * 3f,
                paint
            )
        }

        // Particle system

        for (particle in particles) {

            particle.angle += particle.speed

            val angle =
                Math.toRadians(particle.angle.toDouble())

            val distance =
                100f +
                particle.distance * 500f

            val x =
                centerX +
                cos(angle).toFloat() * distance

            val y =
                centerY +
                sin(angle).toFloat() * distance

            paint.color = Color.WHITE

            canvas.drawCircle(
                x,
                y,
                particle.size * 3f,
                paint
            )
        }

        // UI

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.WHITE

        paint.textSize = 34f

        canvas.drawText(
            "GPU TEST",
            30f,
            55f,
            paint
        )

        paint.textSize = 26f

        canvas.drawText(
            "FPS  %.1f".format(fps),
            30f,
            100f,
            paint
        )

        canvas.drawText(
            "FRAMES  $frameCount",
            30f,
            140f,
            paint
        )

        paint.textAlign = Paint.Align.RIGHT

        canvas.drawText(
            "RUNNING",
            width - 30f,
            55f,
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action != MotionEvent.ACTION_UP) {
            return true
        }

        if (!running) {

            val buttonTop = height * 0.65f
            val buttonBottom = buttonTop + 120f

            if (
                event.x > width / 2f - 260f &&
                event.x < width / 2f + 260f &&
                event.y > buttonTop &&
                event.y < buttonBottom
            ) {

                startBenchmark()
            }

        } else {

            stopBenchmark()
        }

        return true
    }

    private fun startBenchmark() {

        running = true

        startTime = System.currentTimeMillis()

        frameCount = 0

        score = 0

        fps = 0.0

        invalidate()
    }

    private fun stopBenchmark() {

        running = false

        invalidate()
    }

    data class Particle(
        var distance: Float,
        var size: Float,
        var speed: Float,
        var angle: Float
    )
}
