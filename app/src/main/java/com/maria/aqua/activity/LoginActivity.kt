package com.maria.aqua.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.maria.aqua.ClienteApi
import com.maria.aqua.dto.auth.LoginRequest
import com.maria.aqua.R
import com.maria.aqua.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val forgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        forgotPassword.setOnClickListener {
            startActivity(
                Intent(this, ForgotPasswordActivity::class.java)
            )
        }
        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val button = findViewById<Button>(R.id.btnLogin)
        val progress = findViewById<ProgressBar>(R.id.progressLogin)
        val error = findViewById<TextView>(R.id.tvLoginError)
        val goRegister = findViewById<TextView>(R.id.tvGoRegister)

        goRegister.setOnClickListener {
            val intent = Intent(
                this@LoginActivity,
                RegisterActivity::class.java
            )
            startActivity(intent)
        }

        button.setOnClickListener {
            val emailValue = email.text.toString().trim()
            val passwordValue = password.text.toString()

            if (emailValue.isBlank() || passwordValue.isBlank()) {
                error.text = "Preencha o e-mail e a senha."
                error.visibility = View.VISIBLE
                return@setOnClickListener
            }

            error.visibility = View.GONE
            button.isEnabled = false
            progress.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = ClienteApi.authApi.login(
                        LoginRequest(
                            email = emailValue,
                            password = passwordValue
                        )
                    )

                    if (response.isSuccessful) {
                        val body = response.body()

                        if (body == null || body.token.isBlank()) {
                            error.text =
                                "A API não retornou um token válido."
                            error.visibility = View.VISIBLE
                        } else {
                            SessionManager.saveToken(
                                this@LoginActivity,
                                body.token
                            )

                            Toast.makeText(
                                this@LoginActivity,
                                "Login realizado!",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(
                                this@LoginActivity,
                                MainActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        error.text = when (response.code()) {
                            400, 401 -> "E-mail ou senha inválidos."
                            else -> "Não foi possível entrar. Erro ${response.code()}."
                        }
                        error.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                    Log.e(
                        "AQUA_AUTH",
                        "Erro de login: ${e.message}",
                        e
                    )

                    error.text = "Erro: ${e.javaClass.simpleName}\n${e.message}"
                    error.visibility = View.VISIBLE
                } finally {
                    button.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }
}
