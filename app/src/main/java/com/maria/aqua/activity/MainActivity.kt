package com.maria.aqua.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.maria.aqua.CreateWaterLogRequest
import com.maria.aqua.HistoryActivity
import com.maria.aqua.R
import com.maria.aqua.data.remote.WaterApi
import com.maria.aqua.WaterHistory
import com.maria.aqua.WaterLogResponse
import com.maria.aqua.WaterRecord
import com.maria.aqua.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import com.maria.aqua.databinding.ViewBottomNavBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val waterApi: WaterApi by inject()
    private var dailyGoal = 2000
    private var consumedWater = 0
    private val waterLogs = mutableListOf<WaterLogResponse>()
    private val tvConsumed get() = binding.tvConsumed
    private val tvProgress get() = binding.tvProgress
    private val progressWater get() = binding.progressWater
    private val btnUndo get() = binding.btnUndo

    private lateinit var bottomNavBinding: ViewBottomNavBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                Toast.makeText(
                    this,
                    "Notificações ativadas!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        bottomNavBinding = ViewBottomNavBinding.bind(
            findViewById(R.id.bottomNav)
        )

        createNotificationChannel()
        requestNotificationPermission()

        progressWater.max = dailyGoal

        binding.btn200.setOnClickListener {
            addWater(200)
        }

        binding.btn300.setOnClickListener {
            addWater(300)
        }

        binding.btn500.setOnClickListener {
            addWater(500)
        }

        btnUndo.setOnClickListener {
            undoLastWater()
        }

        binding.navHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        updateScreen()
        loadTodayWater()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "aqua_water",
                "Registro de hidratação",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de consumo de água do Aqua"
            }

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun showWaterNotification(amount: Int) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(
            this,
            "aqua_water"
        )
            .setSmallIcon(R.drawable.ic_notification_water)
            .setContentTitle("Aqua")
            .setContentText("Você adicionou $amount ml de água!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Você adicionou $amount ml de água. " +
                            "Continue cuidando da sua hidratação!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun addWater(amount: Int) {
        btnUndo.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    waterApi.create(
                        CreateWaterLogRequest(amountMl = amount)
                    )
                }

                if (response.isSuccessful) {
                    val log = response.body()

                    if (log == null) {
                        Toast.makeText(
                            this@MainActivity,
                            "A API não retornou o registro.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    waterLogs.add(log)
                    consumedWater += log.amountMl

                    WaterHistory.records.add(
                        WaterRecord(
                            amount = log.amountMl,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    updateScreen()
                    showWaterNotification(log.amountMl)

                    Toast.makeText(
                        this@MainActivity,
                        "${log.amountMl} ml salvos!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Não foi possível salvar (${response.code()}).",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Erro ao salvar o consumo. Verifique a conexão com a API.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                updateScreen()
            }
        }
    }

    private fun undoLastWater() {
        val lastLog = waterLogs.lastOrNull()

        if (lastLog == null) {
            Toast.makeText(
                this,
                "Não há registros adicionados nesta sessão para desfazer.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        btnUndo.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    waterApi.delete(lastLog.id)
                }

                if (response.isSuccessful) {
                    waterLogs.removeAt(waterLogs.lastIndex)

                    consumedWater =
                        (consumedWater - lastLog.amountMl).coerceAtLeast(0)

                    val historyIndex = WaterHistory.records.indexOfLast {
                        it.amount == lastLog.amountMl
                    }

                    if (historyIndex >= 0) {
                        WaterHistory.records.removeAt(historyIndex)
                    }

                    updateScreen()

                    Toast.makeText(
                        this@MainActivity,
                        "${lastLog.amountMl} ml removidos!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Não foi possível remover (${response.code()}).",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Erro ao remover o consumo. Verifique a conexão com a API.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                updateScreen()
            }
        }
    }

    private fun updateScreen() {
        tvConsumed.text = "$consumedWater ml"

        val percentage = consumedWater * 100 / dailyGoal
        progressWater.progress = consumedWater.coerceAtMost(dailyGoal)
        tvProgress.text = "$percentage% da meta atingida"

        btnUndo.isEnabled = waterLogs.isNotEmpty()
        btnUndo.alpha = if (waterLogs.isEmpty()) 0.5f else 1f
    }

    private fun loadTodayWater() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    waterApi.getDay()
                }

                if (response.isSuccessful) {
                    val dayData = response.body() ?: return@launch

                    consumedWater = dayData.totalMl

                    waterLogs.clear()
                    waterLogs.addAll(dayData.logs)

                    WaterHistory.records.clear()
                    WaterHistory.records.addAll(
                        dayData.logs.map { log ->
                            WaterRecord(
                                amount = log.amountMl,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    )

                    updateScreen()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao carregar consumo: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Não foi possível carregar o consumo salvo.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
