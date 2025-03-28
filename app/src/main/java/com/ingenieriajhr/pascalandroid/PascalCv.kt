package com.ingenieriajhr.pascalandroid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class PressureMeterView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var currentPressure: Float = 0f // Valor de presión (de 0 a 2000 Pascales)
    private val maxPressure: Float = 1292f
    private val startAngle = 180f // Ángulo de inicio del arco (180 grados para empezar en la parte inferior)
    private val sweepAngle = 180f // Ángulo de barrido del arco (180 grados para un semicírculo)

    init {
        paint.strokeWidth = 10f
        paint.textSize = 50f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Fondo de la vista
        canvas.drawColor(Color.WHITE)

        // Dibujar el arco de presiones
        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height - 95f // Centro ligeramente por encima del fondo
        val radius = (width * 0.8f) / 2 - 50f // Radio ajustado para ocupar el 80% del ancho

        // Crear un gradiente de tres colores: verde, amarillo y rojo
        val shader = SweepGradient(
            centerX, centerY,
            intArrayOf(Color.GREEN, Color.YELLOW, Color.RED),
            floatArrayOf(0f, 0.75f, 1f) // Ajustamos las posiciones de los colores para que se distribuyan uniformemente
        )
        paint.shader = shader
        paint.style = Paint.Style.FILL

        // Dibujar el arco
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius,
            centerY + radius, startAngle, sweepAngle, true, paint)

        // Cerrar el arco por debajo
        paint.shader = null
        paint.color = Color.BLACK
        paint.strokeWidth = 5f
        canvas.drawLine(centerX - radius, centerY, centerX + radius, centerY, paint)

        // Calcular la posición del indicador de presión
        val angle = startAngle + (sweepAngle * (currentPressure / maxPressure))
        val indicatorX = centerX + (radius * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val indicatorY = centerY + (radius * sin(Math.toRadians(angle.toDouble()))).toFloat()

        // Dibujar el indicador de presión
        paint.color = Color.RED
        canvas.drawCircle(indicatorX, indicatorY, 20f, paint)

        // Dibujar la línea desde el centro hasta el indicador
        paint.color = Color.RED
        paint.strokeWidth = 5f
        canvas.drawLine(centerX, centerY, indicatorX, indicatorY, paint)

        // Mostrar el valor de la presión
        paint.color = Color.BLACK
        canvas.drawText("${currentPressure.toInt()} Pa", centerX - 50, centerY + 70, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val width = width.toFloat()
                val centerX = width / 2
                val centerY = height - 95f // Centro ligeramente por encima del fondo
                val radius = (width * 0.8f) / 2 - 50f // Radio ajustado para ocupar el 80% del ancho

                // Calcular el ángulo del toque
                val touchX = event.x
                val touchY = event.y
                val dx = touchX - centerX
                val dy = touchY - centerY
                val touchAngle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()

                // Convertir el ángulo a un valor de presión
                val normalizedAngle = (touchAngle - startAngle + 360) % 360
                if (normalizedAngle >= 0 && normalizedAngle <= sweepAngle) {
                    currentPressure = (normalizedAngle / sweepAngle) * maxPressure
                    invalidate() // Redibujar la vista
                }
            }
        }
        return true
    }

    fun setPressure(pressure: Float) {
        currentPressure = pressure.coerceIn(0f, maxPressure)
        Log.d("PRESSURE", "Pressure: $currentPressure")
        invalidate() // Redibujar la vista
    }
}
