
package com.maria.aqua

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ForgotPasswordActivity : Activity() {

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

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(emailValue).matches()) {
                email.error = "Informe um e-mail válido"
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Tela pronta! Falta conectar à API de recuperação.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
