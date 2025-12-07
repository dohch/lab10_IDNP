package com.example.lab10_indp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.lab10_indp.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workManager = WorkManager.getInstance(this)

        setupUI()

        Log.i("LAB10_APP", "Aplicación iniciada correctamente")
    }

    private fun setupUI() {
        // Configurar botones de monitoreo de estrés
        binding.btnStart.setOnClickListener {
            startPeriodicWork()
        }

        binding.btnStop.setOnClickListener {
            stopPeriodicWork()
        }

        binding.btnRunOnce.setOnClickListener {
            runOneTimeWork()
        }

        // Configurar botones de temporizador
        binding.btnStartTimer.setOnClickListener {
            startStressTimer()
        }

        binding.btnStopTimer.setOnClickListener {
            stopAllTimers()
        }

        binding.btnQuickTimer.setOnClickListener {
            startQuickStressRelief()
        }

        // Configurar textos iniciales
        binding.tvStatus.text = "Estado: INACTIVO"
        binding.tvWorkerStatus.text = "Worker: No iniciado"
        binding.tvLastAction.text = "Última acción: Ninguna"
        binding.tvTimerStatus.text = "Temporizador: Inactivo"
        binding.tvLogs.text = "Bienvenido al Monitor de Estrés LAB10\n\nSelecciona una opción para comenzar..."
    }

    // ========== MÉTODOS EXISTENTES ==========

    private fun startPeriodicWork() {
        try {
            val periodicWorkRequest = PeriodicWorkRequestBuilder<StressMonitorWorker>(
                2, TimeUnit.MINUTES
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "lab10_stress_monitor",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )

            binding.tvStatus.text = "Estado: ACTIVO"
            binding.tvWorkerStatus.text = "Worker: Programado (cada 2 min)"
            binding.tvLastAction.text = "Última acción: Monitoreo iniciado"

            addLog("Monitoreo periódico iniciado")
            Log.i("LAB10_APP", "WorkManager iniciado")

        } catch (e: Exception) {
            Log.e("LAB10_APP", "Error al iniciar WorkManager: ${e.message}")
            binding.tvStatus.text = "Estado: Error"
        }
    }

    private fun stopPeriodicWork() {
        try {
            workManager.cancelUniqueWork("lab10_stress_monitor")

            binding.tvStatus.text = "Estado: DETENIDO"
            binding.tvWorkerStatus.text = "Worker: Cancelado"
            binding.tvLastAction.text = "Última acción: Monitoreo detenido"

            addLog("Monitoreo detenido")
            Log.i("LAB10_APP", "WorkManager detenido")

        } catch (e: Exception) {
            Log.e("LAB10_APP", "Error al detener WorkManager: ${e.message}")
        }
    }

    private fun runOneTimeWork() {
        try {
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<StressMonitorWorker>()
                .addTag("lab10_onetime")
                .build()

            workManager.enqueue(oneTimeWorkRequest)

            binding.tvLastAction.text = "Última acción: Chequeo manual"

            addLog("Chequeo manual ejecutado")
            Log.i("LAB10_APP", "Chequeo manual ejecutado")

        } catch (e: Exception) {
            Log.e("LAB10_APP", "Error en chequeo manual: ${e.message}")
        }
    }

    // ========== NUEVOS MÉTODOS TEMPORIZADOR ==========

    private fun startStressTimer() {
        try {
            val durationMinutes = getTimerDuration()
            val timerTitle = "Ejercicio Anti-Estrés"

            val timerWorkRequest = TimerWorker.createTimerWorkRequest(durationMinutes, timerTitle)
            workManager.enqueue(timerWorkRequest)

            binding.tvTimerStatus.text = "Temporizador: Activo ($durationMinutes min)"
            binding.tvLastAction.text = "Última acción: Temporizador iniciado"

            addLog("Temporizador iniciado ($durationMinutes minutos)")
            Log.i("LAB10_TIMER", "Temporizador iniciado por $durationMinutes minutos")

            showTimerInfo(durationMinutes)

        } catch (e: Exception) {
            Log.e("LAB10_TIMER", "Error al iniciar temporizador: ${e.message}")
            binding.tvTimerStatus.text = "Temporizador: Error"
        }
    }

    private fun stopAllTimers() {
        try {
            workManager.cancelAllWorkByTag("timer_work")

            binding.tvTimerStatus.text = "Temporizador: Detenido"
            binding.tvLastAction.text = "Última acción: Temporizador detenido"

            addLog("Temporizador detenido manualmente")
            Log.i("LAB10_TIMER", "Temporizador detenido")

        } catch (e: Exception) {
            Log.e("LAB10_TIMER", "Error al detener temporizador: ${e.message}")
        }
    }

    private fun startQuickStressRelief() {
        try {
            val timerTitle = "Alivio Rápido de Estrés"

            val quickTimerRequest = TimerWorker.createTimerWorkRequest(1, timerTitle)
            workManager.enqueue(quickTimerRequest)

            binding.tvTimerStatus.text = "Temporizador: Alivio rápido (1 min)"
            binding.tvLastAction.text = "Última acción: Alivio rápido iniciado"

            addLog("Alivio rápido de estrés iniciado (1 minuto)")
            Log.i("LAB10_TIMER", "Alivio rápido iniciado")

            binding.tvLogs.text = "Ejercicio de respiración:\n" +
                    "1. Inspira profundamente (4 segundos)\n" +
                    "2. Aguanta la respiración (4 segundos)\n" +
                    "3. Exhala lentamente (6 segundos)\n" +
                    "Repite durante 1 minuto..."

        } catch (e: Exception) {
            Log.e("LAB10_TIMER", "Error en alivio rápido: ${e.message}")
        }
    }

    private fun getTimerDuration(): Long {
        return when {
            binding.rb5min.isChecked -> 5
            binding.rb10min.isChecked -> 10
            binding.rb15min.isChecked -> 15
            else -> 5
        }
    }

    private fun showTimerInfo(duration: Long) {
        val tips = when (duration) {
            5L -> "Sugerencia: Practica respiración profunda durante 5 minutos."
            10L -> "Sugerencia: Escucha música relajante o haz estiramientos."
            15L -> "Sugerencia: Realiza una breve meditación o lectura."
            else -> "Tomate este tiempo para despejar tu mente."
        }

        binding.tvLogs.text = "Temporizador configurado: $duration minutos\n\n" +
                "$tips\n\n" +
                "La notificación mostrará el progreso en tiempo real."
    }

    private fun addLog(message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logEntry = "$time - $message\n"

        val currentLogs = binding.tvLogs.text.toString()
        val lines = (logEntry + currentLogs).lines().take(10)
        binding.tvLogs.text = lines.joinToString("\n")
    }
}