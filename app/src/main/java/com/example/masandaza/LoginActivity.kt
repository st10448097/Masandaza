package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

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

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            showResetPasswordDialog(email)
        }
    }

    private fun showResetPasswordDialog(initialEmail: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")
        builder.setMessage("Enter your account email address to receive a password reset link:")

        val input = EditText(this).apply {
            hint = "name@email.com"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(initialEmail)
            if (text.isNotEmpty()) {
                setSelection(text.length)
            }
        }

        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginPx = (20 * resources.displayMetrics.density).toInt()
            setMargins(marginPx, 0, marginPx, 0)
        }
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Send Reset Link") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isBlank()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setTitle("Reset Link Sent")
                    .setMessage("A password reset email has been sent to $email. Please check your inbox and spam folder.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send reset email: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}