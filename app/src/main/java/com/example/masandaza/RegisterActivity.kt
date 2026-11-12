package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.masandaza.models.User
import com.example.masandaza.utils.FirebaseRepository
import com.example.masandaza.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val name = findViewById<EditText>(R.id.etFullName)
        val email = findViewById<EditText>(R.id.etRegisterEmail)
        val phone = findViewById<EditText>(R.id.etPhone)
        val password = findViewById<EditText>(R.id.etRegisterPassword)
        val confirm = findViewById<EditText>(R.id.etConfirmPassword)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val fullName = name.text.toString().trim()
            val rawEmail = email.text.toString()
            val phoneText = phone.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmText = confirm.text.toString().trim()

            val emailValidation = ValidationUtils.validateEmailDetailed(rawEmail)
            val normalizedEmail = ValidationUtils.normalizeEmail(rawEmail)

            when {
                fullName.isBlank() -> {
                    name.error = "Enter your full name"
                    name.requestFocus()
                }
                emailValidation is ValidationUtils.EmailValidationResult.Invalid -> {
                    email.error = emailValidation.reason
                    email.requestFocus()
                }
                phoneText.length < 10 -> {
                    phone.error = "Enter valid phone number"
                    phone.requestFocus()
                }
                passwordText.length < 6 -> {
                    password.error = "Minimum 6 characters"
                    password.requestFocus()
                }
                passwordText != confirmText -> {
                    confirm.error = "Passwords do not match"
                    confirm.requestFocus()
                }
                else -> {
                    auth.createUserWithEmailAndPassword(normalizedEmail, passwordText)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val currentUserId = auth.currentUser?.uid ?: ""
                                val newUser = User(
                                    userId = currentUserId,
                                    fullName = fullName,
                                    email = normalizedEmail,
                                    phoneNumber = phoneText,
                                    role = "customer"
                                )

                                firebaseRepo.saveUser(newUser,
                                    onSuccess = {
                                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    },
                                    onFailure = { e ->
                                        Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }

        findViewById<TextView>(R.id.tvGoToLogin).setOnClickListener {
            finish()
        }
    }
}

