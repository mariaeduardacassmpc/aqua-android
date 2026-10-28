package com.maria.aqua.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.maria.aqua.ClienteApi
import com.maria.aqua.dto.auth.PasswordResetRequest
import com.maria.aqua.R
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

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
            resetPassword()
        }

        if (email.isBlank()) {
            mostrarMensagem("E-mail não informado. Volte à recuperação de senha.")
        }
    }

    private fun resetPassword() {
        val code = etResetCode.text.toString().trim()
        val password = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        tvMessage.visibility = View.GONE

        if (email.isBlank()) {
            mostrarMensagem("Informe seu e-mail na etapa anterior.")
            return
        }

        if (code.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            mostrarMensagem("Preencha todos os campos.")
            return
        }

        if (password.length < 8) {
            mostrarMensagem("A senha deve ter pelo menos 8 caracteres.")
            return
        }

        if (password != confirmPassword) {
            mostrarMensagem("As senhas não coincidem.")
            return
        }

        lifecycleScope.launch {
            setLoading(true)

            try {
                val response = ClienteApi.authApi.resetPassword(
                    PasswordResetRequest(
                        token = code,
                        newPassword = password
                    )
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Senha redefinida com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()

                    abrirLogin()
                } else {
                    mostrarMensagem(
                        when (response.code()) {
                            400 -> "Código inválido ou expirado."
                            404 -> "Conta ou código não encontrado."
                            else -> "Não foi possível redefinir a senha. Tente novamente."
                        }
                    )
                }
            } catch (e: Exception) {
                mostrarMensagem(
                    "Erro de conexão com o servidor. Tente novamente."
                )
            } finally {
                setLoading(false)
            }
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
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
