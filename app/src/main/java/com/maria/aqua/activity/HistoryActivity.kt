package com.maria.aqua

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.maria.aqua.activity.MainActivity
class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

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
            showHistory()
        }
    }

    override fun onResume() {
        super.onResume()
        showHistory()
    }


    private fun showHistory() {
        val allRecords = WaterHistory.records.toList()
        val dayFormatter = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val labelFormatter = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
        val todayKey = dayFormatter.format(Date())

        val todayRecords = allRecords
            .filter { dayFormatter.format(Date(it.timestamp)) == todayKey }
            .sortedByDescending { it.timestamp }

        val emptyMessage = findViewById<TextView>(R.id.tvHistoryEmpty)
        val historyList = findViewById<LinearLayout>(R.id.historyList)
        historyList.removeAllViews()

        emptyMessage.visibility =
            if (todayRecords.isEmpty()) View.VISIBLE else View.GONE

        val calendar = java.util.Calendar.getInstance()
        val businessDays = mutableListOf<Date>()

        while (businessDays.size < 5) {
            val date = calendar.time
            val weekday = calendar.get(java.util.Calendar.DAY_OF_WEEK)

            if (weekday != java.util.Calendar.SATURDAY &&
                weekday != java.util.Calendar.SUNDAY
            ) {
                businessDays.add(0, date)
            }

            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        val days = mutableListOf<Pair<String, String>>()
        val dailyTotals = mutableMapOf<String, Int>()

        calendar.add(java.util.Calendar.DAY_OF_YEAR, -4)

        repeat(5) {
            val date = calendar.time
            val key = dayFormatter.format(date)
            val weekday = java.text.SimpleDateFormat(
                "EEE",
                Locale("pt", "BR")
            ).format(date).first().uppercase()

            days.add(key to weekday)
            dailyTotals[key] = 0

            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        allRecords.forEach { record ->
            val key = dayFormatter.format(Date(record.timestamp))
            if (dailyTotals.containsKey(key)) {
                dailyTotals[key] = (dailyTotals[key] ?: 0) + record.amount
            }
        }

        val weeklyTotal = dailyTotals.values.sum()
        val dailyAverage = weeklyTotal / 5
        val maxValue = (dailyTotals.values.maxOrNull() ?: 0).coerceAtLeast(1)

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
            text = "Consumo dos últimos 5 dias úteis"
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
            gravity = android.view.Gravity.BOTTOM
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        days.forEach { (key, label) ->
            val amount = dailyTotals[key] ?: 0
            val barHeight = if (amount == 0) {
                4
            } else {
                (amount.toFloat() / maxValue * 110).toInt().coerceAtLeast(8)
            }

            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or
                        android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    dp(155),
                    1f
                )
            }

            column.addView(TextView(this).apply {
                text = if (amount >= 1000) {
                    String.format(Locale.getDefault(), "%.1fL", amount / 1000f)
                } else {
                    amount.toString()
                }
                textSize = 10f
                gravity = android.view.Gravity.CENTER
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
                    dp(30),
                    dp(barHeight)
                ).apply {
                    topMargin = dp(5)
                    bottomMargin = dp(8)
                }
            })

            column.addView(TextView(this).apply {
                text = label.substringBefore("\n")
                textSize = 11f
                gravity = android.view.Gravity.CENTER
                setTextColor(Color.parseColor("#526777"))
                maxLines = 2
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

        val timeFormatter = SimpleDateFormat("HH:mm", Locale("pt", "BR")).apply {
            timeZone = java.util.TimeZone.getTimeZone("America/Sao_Paulo")
        }

        todayRecords.forEach { record ->
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
