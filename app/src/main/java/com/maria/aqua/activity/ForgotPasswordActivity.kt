
package com.maria.aqua.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.maria.aqua.ClienteApi
import com.maria.aqua.R
import com.maria.aqua.dto.auth.ForgotPasswordRequest
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        val email = findViewById<EditText>(R.id.etForgotEmail)
        val button = findViewById<Button>(R.id.btnSendRecovery)
        val backToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        backToLogin.setOnClickListener {
            finish()
        }

        button.setOnClickListener {
            val emailValue = email.text.toString().trim()

            if (emailValue.isBlank()) {
                email.error = "Informe seu e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                email.error = "Informe um e-mail válido"
                return@setOnClickListener
            }

            button.isEnabled = false

            lifecycleScope.launch {
                try {
                    val response = ClienteApi.authApi
                        .forgotPassword(ForgotPasswordRequest(emailValue))

                    if (response.isSuccessful) {
                        val intent = Intent(
                            this@ForgotPasswordActivity,
                            ResetPasswordActivity::class.java
                        )
                        intent.putExtra("email", emailValue)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Erro ao enviar: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {
                    android.util.Log.e("AQUA_RECOVERY", "Erro na recuperação", e)

                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Erro: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    button.isEnabled = true
                }

            }
        }
    }
}
