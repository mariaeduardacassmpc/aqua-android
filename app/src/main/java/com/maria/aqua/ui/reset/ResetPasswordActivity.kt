package com.maria.aqua.ui.reset

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
import com.maria.aqua.data.remote.ClienteApi
import com.maria.aqua.ui.login.LoginActivity
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private val viewModel: ResetPasswordViewModel by viewModels()

    private lateinit var etResetCode: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var progressResetPassword: ProgressBar
    private lateinit var tvMessage: TextView

    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpassoword)

        ClienteApi.initialize(applicationContext)

        etResetCode = findViewById(R.id.etResetCode)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        progressResetPassword = findViewById(R.id.progressResetPassword)
        tvMessage = findViewById(R.id.tvResetPasswordMessage)

        email = intent.getStringExtra("email").orEmpty()

        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener {
            abrirLogin()
        }

        btnResetPassword.setOnClickListener {
            viewModel.resetPassword(
                email = email,
                code = etResetCode.text.toString().trim(),
                password = etNewPassword.text.toString(),
                confirmPassword = etConfirmPassword.text.toString()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    setLoading(state.isLoading)

                    tvMessage.text = state.error.orEmpty()
                    tvMessage.visibility =
                        if (state.error != null) View.VISIBLE else View.GONE

                    if (state.success) {
                        viewModel.consumeSuccess()

                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Senha redefinida com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()

                        abrirLogin()
                    }
                }
            }
        }

        if (email.isBlank()) {
            mostrarMensagem(
                "E-mail não informado. Volte à recuperação de senha."
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        progressResetPassword.visibility =
            if (loading) View.VISIBLE else View.GONE

        btnResetPassword.isEnabled = !loading
        etResetCode.isEnabled = !loading
        etNewPassword.isEnabled = !loading
        etConfirmPassword.isEnabled = !loading
    }

    private fun mostrarMensagem(message: String) {
        tvMessage.text = message
        tvMessage.visibility = View.VISIBLE
    }

    private fun abrirLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}
