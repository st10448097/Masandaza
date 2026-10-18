package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Authenticating..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: ""

                    // Direct to Customer vs Staff depending on role in Firestore
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role") ?: "customer"
                            if (role == "staff") {
                                startActivity(Intent(this, StaffDashboardActivity::class.java))
                            } else {
                                startActivity(Intent(this, DasboardActivity::class.java))
                            }
                            finish()
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

