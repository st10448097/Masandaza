package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val nameInput = findViewById<EditText>(R.id.etProfileName)
        val emailInput = findViewById<EditText>(R.id.etProfileEmail)

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()

            when {
                name.isBlank() -> {
                    nameInput.error = "Enter your name"
                    nameInput.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Enter a valid email"
                    emailInput.requestFocus()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Profile settings saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}