package com.maria.aqua.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.maria.aqua.HistoryActivity
import com.maria.aqua.R
import com.maria.aqua.WaterHistory
import com.maria.aqua.WaterRecord

class MainActivity : AppCompatActivity() {

    private var dailyGoal = 2000
    private var consumedWater = 0
    private val waterHistory = mutableListOf<Int>()

    private lateinit var tvConsumed: TextView
    private lateinit var tvProgress: TextView
    private lateinit var progressWater: ProgressBar
    private lateinit var btnUndo: Button

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
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        requestNotificationPermission()

        tvConsumed = findViewById(R.id.tvConsumed)
        tvProgress = findViewById(R.id.tvProgress)
        progressWater = findViewById(R.id.progressWater)
        btnUndo = findViewById(R.id.btnUndo)

        progressWater.max = dailyGoal

        findViewById<Button>(R.id.btn200).setOnClickListener {
            addWater(200)
        }

        findViewById<Button>(R.id.btn300).setOnClickListener {
            addWater(300)
        }

        findViewById<Button>(R.id.btn500).setOnClickListener {
            addWater(500)
        }

        btnUndo.setOnClickListener {
            undoLastWater()
        }

        findViewById<Button>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        updateScreen()
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

            val manager = getSystemService(NotificationManager::class.java)
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

        val notification = NotificationCompat.Builder(this, "aqua_water")
            .setSmallIcon(R.drawable.ic_notification_water)
            .setContentTitle("Aqua")
            .setContentText("Você adicionou $amount ml de água!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
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
        consumedWater += amount
        waterHistory.add(amount)
        WaterHistory.records.add(WaterRecord(amount))

        updateScreen()
        showWaterNotification(amount)

        Toast.makeText(
            this,
            "$amount ml adicionados!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun undoLastWater() {
        if (waterHistory.isEmpty()) return

        val lastAmount = waterHistory.removeAt(waterHistory.lastIndex)
        consumedWater -= lastAmount

        if (WaterHistory.records.isNotEmpty()) {
            WaterHistory.records.removeAt(WaterHistory.records.lastIndex)
        }

        updateScreen()

        Toast.makeText(
            this,
            "$lastAmount ml removidos!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateScreen() {
        tvConsumed.text = "$consumedWater ml"

        val percentage = consumedWater * 100 / dailyGoal
        progressWater.progress = consumedWater.coerceAtMost(dailyGoal)
        tvProgress.text = "$percentage% da meta atingida"

        btnUndo.isEnabled = waterHistory.isNotEmpty()
        btnUndo.alpha = if (waterHistory.isEmpty()) 0.5f else 1f
    }
}