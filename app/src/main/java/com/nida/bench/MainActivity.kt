package com.nida.bench

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.*
import android.content.Context
import kotlin.random.Random
import kotlin.math.sin
import kotlin.math.cos

class MainActivity : Activity() {

    private lateinit var benchView: GPUView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        benchView = GPUView(this)
        setContentView(benchView)
    }

    override fun onResume() {
        super.onResume()
        benchView.startBenchmark()
    }

    override fun onPause() {
        benchView.stopBenchmark()
        super.onPause()
    }

    class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var phase: Float
    )

    class GPUView(context: Context) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val particles = ArrayList<Particle>()

        private var running = false
        private var startTime = 0L
        private var lastFrameTime = 0L

        private var frameCount = 0
        private var fps = 0f
        private var minFps = 999f

        private var testTime = 0f

        init {
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textSize = 42f

            for (i in 0 until 700) {
                particles.add(
                    Particle(
                        Random.nextFloat(),
                        Random.nextFloat(),
                        Random.nextFloat() * 0.004f - 0.002f,
                        Random.nextFloat() * 0.004f - 0.002f,
                        Random.nextFloat() * 18f + 4f,
                        Random.nextFloat() * 6.28f
                    )
                )
            }

            setBackgroundColor(Color.BLACK)
        }

        fun startBenchmark() {
            running = true
            startTime = System.currentTimeMillis()
            lastFrameTime = startTime
            frameCount = 0
            fps = 0f
            minFps = 999f
            testTime = 0f

            postInvalidateOnAnimation()
        }

        fun stopBenchmark() {
            running = false
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (!running) return

            val now = System.currentTimeMillis()
            val delta = (now - lastFrameTime).coerceAtMost(50L) / 1000f
            lastFrameTime = now

            testTime = (now - startTime) / 1000f

            updateParticles(delta)
            drawBackground(canvas)
            drawParticles(canvas)
            drawEffects(canvas)
            drawInformation(canvas)

            frameCount++

            if (testTime > 1f) {
                fps = frameCount / testTime

                if (fps < minFps) {
                    minFps = fps
                }
            }

            if (testTime < 30f) {
                postInvalidateOnAnimation()
            } else {
                running = false
                drawFinished(canvas)
            }
        }

        private fun updateParticles(delta: Float) {

            for (particle in particles) {

                particle.x += particle.vx * delta * 60f
                particle.y += particle.vy * delta * 60f

                particle.phase += delta * 3f

                if (particle.x < 0f) particle.x = 1f
                if (particle.x > 1f) particle.x = 0f

                if (particle.y < 0f) particle.y = 1f
                if (particle.y > 1f) particle.y = 0f
            }
        }

        private fun drawBackground(canvas: Canvas) {

            val width = width.toFloat()
            val height = height.toFloat()

            val centerX = width / 2f
            val centerY = height / 2f

            for (i in 0 until 25) {

                val angle =
                    testTime * 0.7f +
                    i * 0.25f

                val radius =
                    100f +
                    sin(testTime * 2f + i) * 80f +
                    i * 20f

                val x =
                    centerX +
                    cos(angle) * radius

                val y =
                    centerY +
                    sin(angle) * radius

                paint.color = Color.rgb(
                    20 + i * 5,
                    50 + i * 6,
                    120 + i * 5
                )

                paint.style = Paint.Style.FILL

                canvas.drawCircle(
                    x,
                    y,
                    30f + i,
                    paint
                )
            }
        }

        private fun drawParticles(canvas: Canvas) {

            val width = width.toFloat()
            val height = height.toFloat()

            for (particle in particles) {

                val x = particle.x * width
                val y = particle.y * height

                val pulse =
                    sin(particle.phase) * 0.5f + 0.5f

                val size =
                    particle.size * (0.6f + pulse)

                paint.color = Color.rgb(
                    30,
                    (120 + pulse * 100).toInt(),
                    255
                )

                canvas.drawCircle(
                    x,
                    y,
                    size,
                    paint
                )
            }
        }

        private fun drawEffects(canvas: Canvas) {

            val width = width.toFloat()
            val height = height.toFloat()

            val cx = width / 2f
            val cy = height / 2f

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f

            for (i in 0 until 12) {

                val rotation =
                    testTime * (0.5f + i * 0.05f)

                val radius =
                    100f + i * 45f

                val left =
                    cx - radius

                val top =
                    cy - radius

                val right =
                    cx + radius

                val bottom =
                    cy + radius

                canvas.save()

                canvas.rotate(
                    rotation * 40f,
                    cx,
                    cy
                )

                paint.color = Color.argb(
                    100,
                    50 + i * 10,
                    100 + i * 8,
                    255
                )

                canvas.drawOval(
                    left,
                    top,
                    right,
                    bottom,
                    paint
                )

                canvas.restore()
            }

            paint.style = Paint.Style.FILL
        }

        private fun drawInformation(canvas: Canvas) {

            paint.color = Color.WHITE
            paint.textSize = 36f
            paint.typeface = Typeface.DEFAULT_BOLD

            canvas.drawText(
                "NIDA BENCH",
                30f,
                55f,
                paint
            )

            paint.textSize = 28f

            canvas.drawText(
                "GPU TEST",
                30f,
                95f,
                paint
            )

            canvas.drawText(
                "FPS  %.1f".format(fps),
                30f,
                145f,
                paint
            )

            canvas.drawText(
                "TIME  %.1fs".format(testTime),
                30f,
                185f,
                paint
            )

            canvas.drawText(
                "OBJECTS  %d".format(particles.size),
                30f,
                225f,
                paint
            )
        }

        private fun drawFinished(canvas: Canvas) {

            paint.color = Color.BLACK

            canvas.drawRect(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                paint
            )

            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD

            paint.textSize = 52f

            canvas.drawText(
                "GPU TEST COMPLETE",
                width / 2f,
                height / 2f - 80f,
                paint
            )

            paint.textSize = 44f

            canvas.drawText(
                "AVG  %.1f FPS".format(fps),
                width / 2f,
                height / 2f,
                paint
            )

            paint.textSize = 32f

            canvas.drawText(
                "MIN  %.1f FPS".format(minFps),
                width / 2f,
                height / 2f + 60f,
                paint
            )

            paint.textAlign = Paint.Align.LEFT
        }
    }
}