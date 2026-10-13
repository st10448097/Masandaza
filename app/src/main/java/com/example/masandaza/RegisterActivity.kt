package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name = findViewById<EditText>(R.id.etFullName)
        val email = findViewById<EditText>(R.id.etRegisterEmail)
        val phone = findViewById<EditText>(R.id.etPhone)
        val password = findViewById<EditText>(R.id.etRegisterPassword)
        val confirm = findViewById<EditText>(R.id.etConfirmPassword)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val fullName = name.text.toString().trim()
            val emailText = email.text.toString().trim()
            val phoneText = phone.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmText = confirm.text.toString().trim()

            when {
                fullName.isBlank() -> {
                    name.error = "Enter your full name"
                    name.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(emailText).matches() -> {
                    email.error = "Enter a valid email address"
                    email.requestFocus()
                }

                phoneText.filter { it.isDigit() }.length < 10 -> {
                    phone.error = "Enter a valid phone number"
                    phone.requestFocus()
                }

                passwordText.length < 8 -> {
                    password.error = "Use at least 8 characters"
                    password.requestFocus()
                }

                passwordText != confirmText -> {
                    confirm.error = "Passwords do not match"
                    confirm.requestFocus()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Registration details validated.",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        findViewById<TextView>(R.id.tvGoToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}