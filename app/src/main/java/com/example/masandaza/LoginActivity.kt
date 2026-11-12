package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.masandaza.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnSubmit = findViewById<Button>(R.id.btnLoginSubmit)
        val tvRegister = findViewById<TextView>(R.id.tvGoToRegister)

        btnSubmit.setOnClickListener {
            val rawEmail = etEmail.text.toString()
            val password = etPassword.text.toString().trim()

            val emailValidation = ValidationUtils.validateEmailDetailed(rawEmail)
            val normalizedEmail = ValidationUtils.normalizeEmail(rawEmail)

            if (rawEmail.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (emailValidation is ValidationUtils.EmailValidationResult.Invalid) {
                etEmail.error = emailValidation.reason
                etEmail.requestFocus()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Authenticating..."

            auth.signInWithEmailAndPassword(normalizedEmail, password)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    val userEmail = firebaseUser?.email ?: email

                    // EMERGENCY OVERRIDE: Force staff dashboard for admin email immediately
                    if (userEmail.lowercase() == "admin@masandaza.com") {
                        val intent = Intent(this, StaffDashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                        return@addOnSuccessListener
                    }

                    // Standard Firestore check for other users
                    val uid = firebaseUser?.uid ?: ""
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val role = doc.getString("role")?.lowercase() ?: "customer"

                                if (role == "staff" || role == "admin") {
                                    startActivity(Intent(this, StaffDashboardActivity::class.java))
                                } else {
                                    startActivity(Intent(this, DasboardActivity::class.java))
                                }
                                finish()
                            } else {
                                startActivity(Intent(this, DasboardActivity::class.java))
                                finish()
                            }
                        }
                        .addOnFailureListener {
                            startActivity(Intent(this, DasboardActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Login"
                    Toast.makeText(this, "Login Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}