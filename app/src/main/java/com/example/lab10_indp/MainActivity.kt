package com.example.lab10_indp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.lab10_indp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i("LAB10", "✅ App LAB10_INDP iniciada")

        setupUI()
    }

    private fun setupUI() {
        // Configurar botones (SOLO los que existen en el XML)
        binding.btnStart.setOnClickListener {
            startPeriodicWork()
        }

        binding.btnStop.setOnClickListener {
            stopPeriodicWork()
        }

        binding.btnRunOnce.setOnClickListener {
            runOneTimeWork()
        }

        // Estado inicial
        binding.tvStatus.text = "Estado: INACTIVO"
        binding.tvWorkerStatus.text = "Worker: No iniciado"
        binding.tvLastAction.text = "Última acción: Ninguna"
        binding.tvLogs.text = "Esperando ejecuciones...\n"
    }

    private fun startPeriodicWork() {
        try {
            val workManager = WorkManager.getInstance(this)

            // Crear trabajo periódico (cada 2 minutos)
            val periodicWorkRequest = PeriodicWorkRequestBuilder<StressMonitorWorker>(
                2, TimeUnit.MINUTES  // Cada 2 minutos
            ).build()

            // Programar el trabajo
            workManager.enqueueUniquePeriodicWork(
                "lab10_stress_monitor",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )

            // Actualizar UI
            binding.tvStatus.text = "Estado: 🟢 ACTIVO"
            binding.tvWorkerStatus.text = "Worker: Programado (cada 2 min)"
            binding.tvLastAction.text = "Última acción: Monitoreo iniciado"

            addLog("🔄 Monitoreo periódico iniciado")
            Log.i("LAB10", "WorkManager iniciado")

        } catch (e: Exception) {
            Log.e("LAB10", "Error: ${e.message}")
            binding.tvStatus.text = "Estado: ❌ Error"
        }
    }

    private fun stopPeriodicWork() {
        try {
            WorkManager.getInstance(this).cancelUniqueWork("lab10_stress_monitor")

            binding.tvStatus.text = "Estado: 🔴 DETENIDO"
            binding.tvWorkerStatus.text = "Worker: Cancelado"
            binding.tvLastAction.text = "Última acción: Monitoreo detenido"

            addLog("⏹ Monitoreo detenido")
            Log.i("LAB10", "WorkManager detenido")

        } catch (e: Exception) {
            Log.e("LAB10", "Error: ${e.message}")
        }
    }

    private fun runOneTimeWork() {
        try {
            val workManager = WorkManager.getInstance(this)
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<StressMonitorWorker>().build()

            workManager.enqueue(oneTimeWorkRequest)

            binding.tvLastAction.text = "Última acción: Chequeo manual"

            addLog("🔍 Chequeo manual ejecutado")
            Log.i("LAB10", "Tarea única ejecutada")

        } catch (e: Exception) {
            Log.e("LAB10", "Error: ${e.message}")
        }
    }

    private fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "$time - $message\n"

        // Agregar al TextView de logs
        val currentLogs = binding.tvLogs.text.toString()
        binding.tvLogs.text = logEntry + currentLogs
    }
}