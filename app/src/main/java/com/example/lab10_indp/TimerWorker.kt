package com.example.lab10_indp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.TimeUnit

class TimerWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "TimerWorker"
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 100
        const val TIMER_DURATION_KEY = "timer_duration"
        const val TIMER_TITLE_KEY = "timer_title"

        fun createTimerWorkRequest(durationMinutes: Long, title: String): androidx.work.WorkRequest {
            val inputData = Data.Builder()
                .putLong(TIMER_DURATION_KEY, durationMinutes)
                .putString(TIMER_TITLE_KEY, title)
                .build()

            return androidx.work.OneTimeWorkRequestBuilder<TimerWorker>()
                .setInputData(inputData)
                .addTag("timer_work")
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val durationMinutes = inputData.getLong(TIMER_DURATION_KEY, 5)
            val timerTitle = inputData.getString(TIMER_TITLE_KEY) ?: "Temporizador Estrés"

            Log.i(TAG, "Temporizador iniciado: $timerTitle por $durationMinutes minutos")

            createNotificationChannel()
            startTimer(durationMinutes, timerTitle)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en temporizador: ${e.message}")
            Result.failure()
        }
    }

    private suspend fun startTimer(durationMinutes: Long, title: String) {
        val totalSeconds = (durationMinutes * 60).toInt()

        for (secondsElapsed in 0..totalSeconds) {
            // Verificar si el trabajo fue cancelado (corrección aquí)
            if (!coroutineContext.isActive) break

            val timeLeft = totalSeconds - secondsElapsed

            updateNotification(
                title = title,
                timeElapsed = formatTime(secondsElapsed.toLong()),
                timeLeft = formatTime(timeLeft.toLong()),
                progress = (secondsElapsed * 100 / totalSeconds)
            )

            if (secondsElapsed % 30 == 0) {
                val minutesLeft = timeLeft / 60
                val secondsLeft = timeLeft % 60
                Log.i(TAG, "Temporizador: $minutesLeft:$secondsLeft restantes")
            }

            delay(1000) // Esperar 1 segundo
        }

        showCompletionNotification(title, durationMinutes)
        Log.i(TAG, "Temporizador completado: $title")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Temporizador de Estrés"
            val description = "Notificaciones del temporizador de monitoreo de estrés"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(title: String, timeElapsed: String, timeLeft: String, progress: Int) {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Temporizador: $title")
            .setContentText("Transcurrido: $timeElapsed | Restante: $timeLeft")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tiempo transcurrido: $timeElapsed\n" +
                        "Tiempo restante: $timeLeft\n" +
                        "Progreso: $progress%\n\n" +
                        "Ejercicio de manejo de estrés en progreso..."))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(title: String, durationMinutes: Long) {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Temporizador Completado: $title")
            .setContentText("Temporizador de $durationMinutes minutos finalizado")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(false)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Excelente! Has completado tu sesión de $durationMinutes minutos.\n\n" +
                        "Recomendación: Toma un descanso, respira profundamente y evalúa tu nivel de estrés actual."))
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun formatTime(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}