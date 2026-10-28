package com.maria.aqua.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.maria.aqua.R
import com.maria.aqua.SessionManager
import com.maria.aqua.ui.forgot.ForgotPasswordActivity
import com.maria.aqua.ui.main.MainActivity
import com.maria.aqua.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private var handledToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val button = findViewById<Button>(R.id.btnLogin)
        val progress = findViewById<ProgressBar>(R.id.progressLogin)
        val error = findViewById<TextView>(R.id.tvLoginError)

        findViewById<TextView>(R.id.tvForgotPassword)
            .setOnClickListener {
                startActivity(
                    Intent(this, ForgotPasswordActivity::class.java)
                )
            }

        findViewById<TextView>(R.id.tvGoRegister)
            .setOnClickListener {
                startActivity(
                    Intent(this, RegisterActivity::class.java)
                )
            }

        button.setOnClickListener {
            viewModel.login(
                email.text.toString().trim(),
                password.text.toString()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    button.isEnabled = !state.isLoading

                    error.text = state.error.orEmpty()
                    error.visibility =
                        if (state.error != null) View.VISIBLE else View.GONE

                    val token = state.token

                    if (!token.isNullOrBlank() && token != handledToken) {
                        handledToken = token

                        SessionManager.saveToken(this@LoginActivity, token)

                        Toast.makeText(
                            this@LoginActivity,
                            "Login realizado!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(this@LoginActivity, MainActivity::class.java)
                        )
                        finish()
                    }
                }
            }
        }
    }
}
