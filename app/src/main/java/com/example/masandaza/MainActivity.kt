package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If user is already authenticated, check role and jump to appropriate Dashboard
        if (auth.currentUser != null) {
            val user = auth.currentUser
            val userEmail = user?.email ?: ""
            if (userEmail.lowercase() == "admin@masandaza.com") {
                startActivity(Intent(this, StaffDashboardActivity::class.java))
                finish()
                return
            }
            db.collection("users").document(user?.uid ?: "").get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val role = doc.getString("role")?.lowercase() ?: "customer"
                        if (role == "staff" || role == "admin") {
                            startActivity(Intent(this, StaffDashboardActivity::class.java))
                        } else {
                            startActivity(Intent(this, DasboardActivity::class.java))
                        }
                    } else {
                        startActivity(Intent(this, DasboardActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    startActivity(Intent(this, DasboardActivity::class.java))
                    finish()
                }
            return
        }

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnMainViewServices).setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }

        findViewById<Button>(R.id.btnMainLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btnMainRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}