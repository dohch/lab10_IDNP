package com.example.lab10_indp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class StressMonitorWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Obtener hora actual
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            // Simular nivel de estrés (1-10)
            val stressLevel = (1..10).random()

            // Registrar en Logcat
            Log.i("LAB10_WORKER", "📊 [$time] Nivel de estrés: $stressLevel/10")

            // También imprimir en consola
            println("=== LAB10 WORKER ===")
            println("Hora: $time")
            println("Nivel estrés: $stressLevel/10")
            println("====================")

            Result.success()

        } catch (e: Exception) {
            Log.e("LAB10_WORKER", "❌ Error: ${e.message}")
            Result.failure()
        }
    }
}