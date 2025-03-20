package com.ingenieriajhr.pascalandroid


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PressureMeterView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var currentPressure: Float = 0f // Valor de presión (de 0 a 2000 Pascales)
    private val maxPressure: Float = 2000f

    init {
        paint.color = Color.RED
        paint.strokeWidth = 10f
        paint.textSize = 50f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Fondo de la vista
        canvas.drawColor(Color.WHITE)

        // Dibujar la escala de presiones (en una línea)
        val width = width.toFloat()
        val height = height.toFloat()

        // Línea base (representa la escala de presión)
        val scaleStartX = 100f
        val scaleEndX = width - 100f
        val scaleY = height / 2
        paint.color = Color.BLACK
        paint.strokeWidth = 5f
        canvas.drawLine(scaleStartX, scaleY, scaleEndX, scaleY, paint)

        // Dibujar el indicador de presión
        val pressurePosition = scaleStartX + (scaleEndX - scaleStartX) * (currentPressure / maxPressure)
        paint.color = Color.RED
        canvas.drawCircle(pressurePosition, scaleY, 20f, paint)

        // Mostrar el valor de la presión
        paint.color = Color.BLACK
        canvas.drawText("${currentPressure.toInt()} Pa", width / 2 - 50, scaleY - 50, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val width = width.toFloat()
                val scaleStartX = 100f
                val scaleEndX = width - 100f

                // Obtener la posición del toque y convertirla a un valor de presión
                val touchX = event.x.coerceIn(scaleStartX, scaleEndX)
                currentPressure = ((touchX - scaleStartX) / (scaleEndX - scaleStartX)) * maxPressure
                invalidate() // Redibujar la vista
            }
        }
        return true
    }

    fun setPressure(pressure: Float) {
        currentPressure = pressure.coerceIn(0f, maxPressure)
        invalidate() // Redibujar la vista
    }
}
