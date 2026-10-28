package com.maria.aqua

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.maria.aqua.data.remote.ClienteApi
import com.maria.aqua.ui.ForgotPasswordViewModel
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private val viewModel: ForgotPasswordViewModel by viewModels()
    private var successHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        ClienteApi.initialize(applicationContext)

        val email = findViewById<TextInputEditText>(R.id.etForgotEmail)
        val button = findViewById<Button>(R.id.btnSendRecovery)
        val progress = findViewById<ProgressBar>(R.id.progressForgotPassword)
        val error = findViewById<TextView>(R.id.tvForgotError)
        val backToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        button.setOnClickListener {
            viewModel.sendRecoveryEmail(email.text?.toString()?.trim().orEmpty())
        }

        backToLogin.setOnClickListener {
            finish()
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

                    if (state.success && !successHandled) {
                        successHandled = true
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Se o e-mail estiver cadastrado, você receberá as instruções de recuperação.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }
    }
}
