package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val registerLink = findViewById<TextView>(R.id.tvRegister)
        
        loginButton.setOnClickListener { 
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            
            when {
                email.isEmpty() -> {
                    emailInput.error = "Please enter your email"
                    emailInput.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Enter a valid email address"
                    emailInput.requestFocus()
                }
                
                password.isBlank() -> {
                    passwordInput.error = "Please enter your password"
                    passwordInput.requestFocus()
                }
                else -> {
                    Toast.makeText(
                        this, 
                        "Login form validated",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    startActivity(
                        Intent(this, DasboardActivity::class.java)
                    )
                    finish()
                }
            }
        }
        registerLink.setOnClickListener { 
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }
    }
}