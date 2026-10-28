package com.maria.aqua.ui.register

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
import com.maria.aqua.data.remote.ClienteApi
import com.maria.aqua.R
import com.maria.aqua.ui.LoginActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ClienteApi.initialize(applicationContext)

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
            viewModel.register(
                name.text.toString().trim(),
                email.text.toString().trim(),
                password.text.toString(),
                confirmPassword.text.toString()
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

                    if (state.success) {
                        viewModel.consumeSuccess()

                        Toast.makeText(
                            this@RegisterActivity,
                            "Cadastro realizado! Faça login.",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(
                            Intent(this@RegisterActivity, LoginActivity::class.java)
                        )
                        finish()
                    }
                }
            }
        }
    }
}
