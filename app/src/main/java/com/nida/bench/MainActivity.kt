package com.nida.bench

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import kotlin.random.Random
import kotlin.math.sin
import kotlin.math.cos

class MainActivity : Activity() {

    private lateinit var root: BenchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = BenchView(this)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        root.start()
    }

    override fun onPause() {
        root.stop()
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

    class BenchView(context: Context) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val particles = ArrayList<Particle>()

        private var screen = "home"
        private var menuOpen = false
        private var running = false

        private var startTime = 0L
        private var lastFrameTime = 0L
        private var frameCount = 0
        private var fps = 0f
        private var minFps = 999f
        private var testTime = 0f

        init {
            textPaint.typeface = Typeface.DEFAULT_BOLD

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

        fun start() {
            if (screen == "gpu") {
                startBenchmark()
            } else {
                invalidate()
            }
        }

        fun stop() {
            running = false
        }

        fun startBenchmark() {
            screen = "gpu"
            menuOpen = false

            running = true
            startTime = System.currentTimeMillis()
            lastFrameTime = startTime
            frameCount = 0
            fps = 0f
            minFps = 999f
            testTime = 0f

            postInvalidateOnAnimation()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (screen == "home") {
                drawHome(canvas)
            }

            if (screen == "device") {
                drawDeviceInfo(canvas)
            }

            if (screen == "features") {
                drawFeatureChecker(canvas)
            }

            if (screen == "gpu") {
                drawGPU(canvas)
            }

            if (menuOpen) {
                drawMenu(canvas)
            }
        }

        private fun drawHeader(canvas: Canvas, title: String) {

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(18, 18, 22)

            canvas.drawRect(
                0f,
                0f,
                width.toFloat(),
                90f,
                paint
            )

            paint.color = Color.WHITE
            paint.textSize = 34f
            paint.typeface = Typeface.DEFAULT_BOLD

            canvas.drawText(
                "☰",
                25f,
                58f,
                paint
            )

            paint.textSize = 28f

            canvas.drawText(
                title,
                85f,
                57f,
                paint
            )
        }

        private fun drawHome(canvas: Canvas) {

            canvas.drawColor(Color.BLACK)

            drawHeader(canvas, "NIDA BENCH")

            paint.color = Color.WHITE
            paint.textSize = 38f

            canvas.drawText(
                "Smartphone Toolkit",
                30f,
                155f,
                paint
            )

            paint.color = Color.GRAY
            paint.textSize = 22f

            canvas.drawText(
                "Benchmark & Device Checker",
                30f,
                190f,
                paint
            )

            drawCard(
                canvas,
                30f,
                240f,
                width / 2f - 45f,
                180f,
                "GPU",
                "BENCHMARK"
            )

            drawCard(
                canvas,
                width / 2f + 15f,
                240f,
                width / 2f - 45f,
                180f,
                "FEATURE",
                "CHECKER"
            )

            drawCard(
                canvas,
                30f,
                450f,
                width / 2f - 45f,
                180f,
                "DEVICE",
                "INFO"
            )

            drawCard(
                canvas,
                width / 2f + 15f,
                450f,
                width / 2f - 45f,
                180f,
                "RESULT",
                "SOON"
            )
        }

        private fun drawCard(
            canvas: Canvas,
            x: Float,
            y: Float,
            w: Float,
            h: Float,
            title: String,
            subtitle: String
        ) {

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(25, 25, 30)

            canvas.drawRoundRect(
                x,
                y,
                x + w,
                y + h,
                25f,
                25f,
                paint
            )

            paint.color = Color.WHITE
            paint.textSize = 28f
            paint.typeface = Typeface.DEFAULT_BOLD

            canvas.drawText(
                title,
                x + 20f,
                y + 65f,
                paint
            )

            paint.color = Color.LTGRAY
            paint.textSize = 20f

            canvas.drawText(
                subtitle,
                x + 20f,
                y + 100f,
                paint
            )
        }

        private fun drawDeviceInfo(canvas: Canvas) {

            canvas.drawColor(Color.BLACK)

            drawHeader(canvas, "DEVICE INFO")

            paint.color = Color.WHITE
            paint.textSize = 25f

            val info = arrayOf(
                "Manufacturer : ${Build.MANUFACTURER}",
                "Model        : ${Build.MODEL}",
                "Android      : ${Build.VERSION.RELEASE}",
                "SDK          : ${Build.VERSION.SDK_INT}",
                "CPU ABI      : ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}",
                "Board        : ${Build.BOARD}",
                "Hardware     : ${Build.HARDWARE}"
            )

            var y = 145f

            for (line in info) {

                canvas.drawText(
                    line,
                    25f,
                    y,
                    paint
                )

                y += 55f
            }
        }

        private fun drawFeatureChecker(canvas: Canvas) {

            canvas.drawColor(Color.BLACK)

            drawHeader(canvas, "FEATURE CHECKER")

            val features = arrayOf(
                "Camera",
                "Front Camera",
                "NFC",
                "Bluetooth",
                "GPS",
                "Accelerometer",
                "Gyroscope",
                "Fingerprint",
                "Vibrator"
            )

            var y = 135f

            paint.textSize = 25f

            for (feature in features) {

                val available = when (feature) {

                    "Camera" ->
                        hasFeature(PackageManager.FEATURE_CAMERA_ANY)

                    "Front Camera" ->
                        hasFeature(PackageManager.FEATURE_CAMERA_FRONT)

                    "NFC" ->
                        hasFeature(PackageManager.FEATURE_NFC)

                    "Bluetooth" ->
                        hasFeature(PackageManager.FEATURE_BLUETOOTH)

                    "GPS" ->
                        hasFeature(PackageManager.FEATURE_LOCATION_GPS)

                    "Accelerometer" ->
                        hasFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)

                    "Gyroscope" ->
                        hasFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)

                    "Fingerprint" ->
                        Build.VERSION.SDK_INT >= 23


                    else -> false
                }

                paint.color = if (available) {
                    Color.rgb(80, 220, 120)
                } else {
                    Color.rgb(220, 80, 80)
                }

                canvas.drawText(
                    if (available) "✓" else "✕",
                    30f,
                    y,
                    paint
                )

                paint.color = Color.WHITE

                canvas.drawText(
                    feature,
                    80f,
                    y,
                    paint
                )

                y += 50f
            }
        }

        private fun hasFeature(feature: String): Boolean {
            return context.packageManager.hasSystemFeature(feature)
        }

        private fun drawMenu(canvas: Canvas) {

            paint.color = Color.argb(
                150,
                0,
                0,
                0
            )

            canvas.drawRect(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                paint
            )

            paint.color = Color.rgb(22, 22, 27)

            canvas.drawRect(
                0f,
                0f,
                width * 0.78f,
                height.toFloat(),
                paint
            )

            paint.color = Color.WHITE
            paint.textSize = 30f
            paint.typeface = Typeface.DEFAULT_BOLD

            canvas.drawText(
                "NIDA BENCH",
                35f,
                70f,
                paint
            )

            paint.color = Color.GRAY
            paint.textSize = 18f

            canvas.drawText(
                "Smartphone Toolkit",
                35f,
                105f,
                paint
            )

            val items = arrayOf(
                "HOME",
                "GPU BENCHMARK",
                "DEVICE INFO",
                "FEATURE CHECKER",
                "SETTINGS"
            )

            var y = 180f

            paint.textSize = 23f

            for (item in items) {

                paint.color = Color.WHITE

                canvas.drawText(
                    item,
                    35f,
                    y,
                    paint
                )

                y += 65f
            }
        }

        private fun drawGPU(canvas: Canvas) {

            if (!running) {
                drawGPUFinished(canvas)
                return
            }

            val now = System.currentTimeMillis()

            val delta =
                (now - lastFrameTime)
                    .coerceAtMost(50L) / 1000f

            lastFrameTime = now

            testTime =
                (now - startTime) / 1000f

            updateParticles(delta)
            drawBackground(canvas)
            drawParticles(canvas)
            drawEffects(canvas)

            paint.color = Color.WHITE
            paint.textSize = 28f
            paint.typeface = Typeface.DEFAULT_BOLD

            canvas.drawText(
                "NIDA BENCH",
                25f,
                45f,
                paint
            )

            paint.textSize = 24f

            canvas.drawText(
                "GPU TEST",
                25f,
                80f,
                paint
            )

            canvas.drawText(
                "FPS %.1f".format(fps),
                25f,
                120f,
                paint
            )

            canvas.drawText(
                "TIME %.1fs".format(testTime),
                25f,
                155f,
                paint
            )

            canvas.drawText(
                "OBJECTS ${particles.size}",
                25f,
                190f,
                paint
            )

            frameCount++

            if (testTime > 1f) {

                fps =
                    frameCount / testTime

                if (fps < minFps) {
                    minFps = fps
                }
            }

            if (testTime < 30f) {

                postInvalidateOnAnimation()

            } else {

                running = false
                invalidate()
            }
        }

        private fun updateParticles(delta: Float) {

            for (particle in particles) {

                particle.x +=
                    particle.vx * delta * 60f

                particle.y +=
                    particle.vy * delta * 60f

                particle.phase +=
                    delta * 3f

                if (particle.x < 0f)
                    particle.x = 1f

                if (particle.x > 1f)
                    particle.x = 0f

                if (particle.y < 0f)
                    particle.y = 1f

                if (particle.y > 1f)
                    particle.y = 0f
            }
        }

        private fun drawBackground(canvas: Canvas) {

            val cx = width / 2f
            val cy = height / 2f

            for (i in 0 until 25) {

                val angle =
                    testTime * 0.7f +
                    i * 0.25f

                val radius =
                    100f +
                    sin(testTime * 2f + i) * 80f +
                    i * 20f

                val x =
                    cx + cos(angle) * radius

                val y =
                    cy + sin(angle) * radius

                paint.color = Color.rgb(
                    20 + i * 5,
                    50 + i * 6,
                    120 + i * 5
                )

                canvas.drawCircle(
                    x,
                    y,
                    30f + i,
                    paint
                )
            }
        }

        private fun drawParticles(canvas: Canvas) {

            for (particle in particles) {

                val x =
                    particle.x * width

                val y =
                    particle.y * height

                val pulse =
                    sin(particle.phase) *
                    0.5f + 0.5f

                val size =
                    particle.size *
                    (0.6f + pulse)

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

            val cx = width / 2f
            val cy = height / 2f

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f

            for (i in 0 until 12) {

                val radius =
                    100f + i * 45f

                canvas.save()

                canvas.rotate(
                    testTime * 40f,
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
                    cx - radius,
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    paint
                )

                canvas.restore()
            }

            paint.style = Paint.Style.FILL
        }

        private fun drawGPUFinished(canvas: Canvas) {

            canvas.drawColor(Color.BLACK)

            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD

            paint.textSize = 38f

            canvas.drawText(
                "GPU TEST COMPLETE",
                width / 2f,
                height / 2f - 70f,
                paint
            )

            paint.textSize = 35f

            canvas.drawText(
                "AVG %.1f FPS".format(fps),
                width / 2f,
                height / 2f,
                paint
            )

            paint.textSize = 28f

            canvas.drawText(
                "MIN %.1f FPS".format(minFps),
                width / 2f,
                height / 2f + 55f,
                paint
            )

            paint.textAlign = Paint.Align.LEFT
        }

        override fun onTouchEvent(event: android.view.MotionEvent): Boolean {

            if (event.action != MotionEvent.ACTION_UP) {
                return true
            }

            val x = event.x
            val y = event.y

            // Menu button
            if (x < 80f && y < 100f) {

                menuOpen = !menuOpen
                invalidate()

                return true
            }

            if (menuOpen) {

                val menuWidth =
                    width * 0.78f

                if (x > menuWidth) {

                    menuOpen = false
                    invalidate()

                    return true
                }

                when {

                    y in 130f..210f -> {
                        screen = "home"
                        menuOpen = false
                    }

                    y in 210f..275f -> {
                        startBenchmark()
                        return true
                    }

                    y in 275f..340f -> {
                        screen = "device"
                        menuOpen = false
                    }

                    y in 340f..410f -> {
                        screen = "features"
                        menuOpen = false
                    }

                    y in 410f..490f -> {
                        Toast.makeText(
                            context,
                            "Settingsはこれから！",
                            Toast.LENGTH_SHORT
                        ).show()

                        menuOpen = false
                    }
                }

                invalidate()
                return true
            }

            if (screen == "home") {

                if (y in 240f..420f) {

                    if (x < width / 2f) {
                        startBenchmark()
                    } else {
                        screen = "features"
                        invalidate()
                    }

                    return true
                }

                if (y in 450f..630f) {

                    if (x < width / 2f) {
                        screen = "device"
                        invalidate()
                    }

                    return true
                }
            }

            return true
        }
    }
}