
package com.maria.aqua

import android.content.Intent
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

data class WaterRecord(
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

object WaterHistory {
    val records = mutableListOf<WaterRecord>()
}

class HistoryActivity : AppCompatActivity() {

    data class ConsumoDiario(val data: String, val quantidadeMl: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val navHome = findViewById<Button>(R.id.navHome)
        val navHistory = findViewById<Button>(R.id.navHistory)

        navHome.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#F4F8FB")
            )
        navHome.setTextColor(android.graphics.Color.parseColor("#183B4E"))

        navHistory.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#087E8B")
            )
        navHistory.setTextColor(android.graphics.Color.WHITE)

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
        val records = WaterHistory.records.toList().reversed()
        val total = records.sumOf { it.amount }

        findViewById<TextView>(R.id.tvHistoryTotal).text =
            "$total ml"

        findViewById<TextView>(R.id.tvHistoryCount).text =
            if (records.size == 1) "1 registro"
            else "${records.size} registros"

        val emptyMessage =
            findViewById<TextView>(R.id.tvHistoryEmpty)

        val historyList =
            findViewById<LinearLayout>(R.id.historyList)

        historyList.removeAllViews()

        emptyMessage.visibility =
            if (records.isEmpty()) View.VISIBLE else View.GONE

        val formatter = SimpleDateFormat(
            "HH:mm",
            Locale("pt", "BR")
        )

        records.forEach { record ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 18, 20, 18)
                setBackgroundColor(Color.WHITE)
                elevation = 2f

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 12
                }
            }

            val amountText = TextView(this).apply {
                text = "+${record.amount} ml"
                textSize = 20f
                setTextColor(Color.rgb(8, 126, 139))
                setTypeface(null, Typeface.BOLD)
            }

            val timeText = TextView(this).apply {
                text = "Registrado às ${
                    formatter.format(Date(record.timestamp))
                }"
                textSize = 14f
                setTextColor(Color.rgb(113, 128, 150))
            }

            card.addView(amountText)
            card.addView(timeText)
            historyList.addView(card)
        }
    }
}
