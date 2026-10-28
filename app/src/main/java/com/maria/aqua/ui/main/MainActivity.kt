package com.maria.aqua.ui.main

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.maria.aqua.R
import com.maria.aqua.databinding.ActivityMainBinding
import com.maria.aqua.databinding.ViewBottomNavBinding
import com.maria.aqua.ui.history.HistoryActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavBinding: ViewBottomNavBinding

    private val viewModel: MainViewModel by viewModel()

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

        binding.progressWater.max = 2000

        binding.btn200.setOnClickListener {
            viewModel.addWater(200)
        }

        binding.btn300.setOnClickListener {
            viewModel.addWater(300)
        }

        binding.btn500.setOnClickListener {
            viewModel.addWater(500)
        }

        binding.btnUndo.setOnClickListener {
            viewModel.undoLastWater()
        }

        bottomNavBinding.navHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.tvConsumed.text = "${state.consumedWater} ml"

                        binding.progressWater.max = state.dailyGoal
                        binding.progressWater.progress =
                            state.consumedWater.coerceAtMost(state.dailyGoal)

                        val percentage =
                            state.consumedWater * 100 / state.dailyGoal

                        binding.tvProgress.text =
                            "$percentage% da meta atingida"

                        binding.btnUndo.isEnabled =
                            state.canUndo && !state.isLoading

                        binding.btnUndo.alpha =
                            if (state.canUndo) 1f else 0.5f

                        binding.btn200.isEnabled = !state.isLoading
                        binding.btn300.isEnabled = !state.isLoading
                        binding.btn500.isEnabled = !state.isLoading
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is MainUiEvent.Message -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    event.text,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            is MainUiEvent.WaterAdded -> {
                                showWaterNotification(event.amount)

                                Toast.makeText(
                                    this@MainActivity,
                                    "${event.amount} ml salvos!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
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

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
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

    private fun showWaterNotification(amount: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
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
}
