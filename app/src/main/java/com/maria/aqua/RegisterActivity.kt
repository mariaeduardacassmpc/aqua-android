
package com.maria.aqua

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name = findViewById<EditText>(R.id.etName)
        val email = findViewById<EditText>(R.id.etRegisterEmail)
        val password = findViewById<EditText>(R.id.etRegisterPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val button = findViewById<Button>(R.id.btnRegister)
        val progress = findViewById<ProgressBar>(R.id.progressRegister)
        val error = findViewById<TextView>(R.id.tvRegisterError)
        val goLogin = findViewById<TextView>(R.id.tvGoLogin)

        goLogin.setOnClickListener {
            finish()
        }

        button.setOnClickListener {
            val nameValue = name.text.toString().trim()
            val emailValue = email.text.toString().trim()
            val passwordValue = password.text.toString()
            val confirmValue = confirmPassword.text.toString()

            if (nameValue.isBlank() || emailValue.isBlank() ||
                passwordValue.isBlank() || confirmValue.isBlank()
            ) {
                error.text = "Preencha todos os campos."
                error.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (passwordValue != confirmValue) {
                error.text = "As senhas não coincidem."
                error.visibility = View.VISIBLE
                return@setOnClickListener
            }

            error.visibility = View.GONE
            button.isEnabled = false
            progress.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = ApiClient.authApi.register(
                        RegisterRequest(
                            name = nameValue,
                            email = emailValue,
                            password = passwordValue
                        )
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Cadastro realizado! Faça login.",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(
                            Intent(
                                this@RegisterActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        error.text = "Não foi possível cadastrar. Erro ${response.code()}."
                        error.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    error.text = "Falha de conexão com a API."
                    error.visibility = View.VISIBLE
                } finally {
                    button.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }
}
