package com.maria.aqua

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.maria.aqua.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HistoryActivity : AppCompatActivity() {

    private val locale = Locale("pt", "BR")
    private val dayFormatter = SimpleDateFormat("yyyyMMdd", locale)
    private val labelFormatter = SimpleDateFormat("EEE", locale)
    private val timeFormatter = SimpleDateFormat("HH:mm", locale).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        ClienteApi.initialize(applicationContext)

        val navHome = findViewById<Button>(R.id.navHome)
        val navHistory = findViewById<Button>(R.id.navHistory)

        navHome.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#F4F8FB"))
        navHome.setTextColor(Color.parseColor("#183B4E"))

        navHistory.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#087E8B"))
        navHistory.setTextColor(Color.WHITE)

        navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        navHistory.setOnClickListener {
            loadHistory()
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val today = java.time.LocalDate.now()
                val from = today.minusDays(4).toString()
                val to = today.toString()

                val dayResponse: retrofit2.Response<DayHistoryResponse>
                val historyResponse: retrofit2.Response<List<DailyTotalResponse>>

                withContext(Dispatchers.IO) {
                    dayResponse = ClienteApi.waterApi.getDay()
                    historyResponse = ClienteApi.waterApi.getHistory(from, to)
                }

                if (!dayResponse.isSuccessful || !historyResponse.isSuccessful) {
                    Toast.makeText(
                        this@HistoryActivity,
                        "Erro ao carregar histórico: dia ${dayResponse.code()}, gráfico ${historyResponse.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val dayData = dayResponse.body() ?: return@launch
                val historyData = historyResponse.body().orEmpty()

                val todayRecords = dayData.logs.map { log ->
                    WaterRecord(
                        amount = log.amountMl,
                        timestamp = parseTimestamp(log.consumedAt)
                    )
                }

                WaterHistory.records.clear()
                WaterHistory.records.addAll(todayRecords)

                val dailyTotals = historyData.associate { item ->
                    normalizeDay(item.day) to item.totalMl
                }.toMutableMap()

                dailyTotals[normalizeDay(today.toString())] = dayData.totalMl

                renderHistory(
                    todayRecords = todayRecords,
                    dailyTotals = dailyTotals
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@HistoryActivity,
                    "Erro ao carregar histórico: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun parseTimestamp(value: String): Long {
        return try {
            val normalized = value.replace(" ", "T")
            val patterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss"
            )

            patterns.firstNotNullOfOrNull { pattern ->
                try {
                    val formatter = SimpleDateFormat(pattern, Locale.US)
                    formatter.isLenient = false
                    if (pattern.endsWith("'Z'")) {
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    formatter.parse(normalized)?.time
                } catch (_: Exception) {
                    null
                }
            } ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun normalizeDay(value: String): String {
        return try {
            val datePart = value.take(10)
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .parse(datePart)?.let { dayFormatter.format(it) }
                ?: value
        } catch (_: Exception) {
            value
        }
    }

    private fun renderHistory(
        todayRecords: List<WaterRecord>,
        dailyTotals: Map<String, Int>
    ) {
        val todayKey = dayFormatter.format(Date())

        val sortedToday = todayRecords
            .filter {
                dayFormatter.format(Date(it.timestamp)) == todayKey
            }
            .sortedByDescending { it.timestamp }

        val emptyMessage = findViewById<TextView>(R.id.tvHistoryEmpty)
        val historyList = findViewById<LinearLayout>(R.id.historyList)
        historyList.removeAllViews()

        emptyMessage.visibility =
            if (sortedToday.isEmpty()) View.VISIBLE else View.GONE

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -4)

        val days = mutableListOf<Pair<String, String>>()
        repeat(5) {
            val date = calendar.time
            val key = dayFormatter.format(date)
            val label = labelFormatter.format(date)
                .replaceFirstChar { it.uppercase(locale) }

            days.add(key to label)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val totals = days.associate { (key, _) ->
            key to (dailyTotals[key] ?: 0)
        }

        val weeklyTotal = totals.values.sum()
        val dailyAverage = weeklyTotal / 5
        val maxValue = (totals.values.maxOrNull() ?: 0).coerceAtLeast(1)

        val chartCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), Color.parseColor("#E4EDF2"))
            }
            elevation = dp(2).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
        }

        chartCard.addView(TextView(this).apply {
            text = "Consumo recente"
            textSize = 18f
            setTextColor(Color.parseColor("#183B4E"))
            setTypeface(null, Typeface.BOLD)
        })

        chartCard.addView(TextView(this).apply {
            text = "Total: ${weeklyTotal} ml  •  Média: ${dailyAverage} ml/dia"
            textSize = 13f
            setTextColor(Color.parseColor("#718096"))
            setPadding(0, dp(6), 0, dp(16))
        })

        val chartRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        days.forEach { (key, label) ->
            val amount = totals[key] ?: 0
            val barHeight = if (amount == 0) {
                4
            } else {
                (amount.toFloat() / maxValue * 110)
                    .toInt().coerceAtLeast(8)
            }

            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0, dp(155), 1f
                )
            }

            column.addView(TextView(this).apply {
                text = if (amount >= 1000) {
                    String.format(locale, "%.1fL", amount / 1000f)
                } else {
                    amount.toString()
                }
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#526777"))
                maxLines = 1
            })

            column.addView(View(this).apply {
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(
                        if (key == todayKey) {
                            Color.parseColor("#087E8B")
                        } else {
                            Color.parseColor("#69C7E5")
                        }
                    )
                    cornerRadius = dp(6).toFloat()
                }
                layoutParams = LinearLayout.LayoutParams(
                    dp(30), dp(barHeight)
                ).apply {
                    topMargin = dp(5)
                    bottomMargin = dp(8)
                }
            })

            column.addView(TextView(this).apply {
                text = label
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#526777"))
                maxLines = 1
            })

            chartRow.addView(column)
        }

        chartCard.addView(chartRow)
        historyList.addView(chartCard)

        historyList.addView(TextView(this).apply {
            text = "Registros de hoje"
            textSize = 18f
            setTextColor(Color.parseColor("#183B4E"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        })

        sortedToday.forEach { record ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(18), dp(20), dp(18))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(12).toFloat()
                }
                elevation = dp(2).toFloat()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
            }

            card.addView(TextView(this).apply {
                text = "+${record.amount} ml"
                textSize = 20f
                setTextColor(Color.rgb(8, 126, 139))
                setTypeface(null, Typeface.BOLD)
            })

            card.addView(TextView(this).apply {
                text = "Registrado às ${timeFormatter.format(Date(record.timestamp))}"
                textSize = 14f
                setTextColor(Color.rgb(113, 128, 150))
            })

            historyList.addView(card)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
