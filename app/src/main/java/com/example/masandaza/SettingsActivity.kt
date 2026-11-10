package com.example.masandaza

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        val tvInitials = findViewById<TextView>(R.id.tvSettingsInitials)
        val tvNameHeader = findViewById<TextView>(R.id.tvSettingsProfileName)
        val tvEmailHeader = findViewById<TextView>(R.id.tvSettingsProfileEmail)
        val etName = findViewById<EditText>(R.id.etSettingsName)
        val etPhone = findViewById<EditText>(R.id.etSettingsPhone)
        val switchDark = findViewById<SwitchMaterial>(R.id.switchDarkMode)

        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnResetPass = findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = findViewById<Button>(R.id.btnSettingsLogout)

        val btnNavHome = findViewById<Button>(R.id.navBtnHome)
        val btnNavBook = findViewById<Button>(R.id.navBtnBook)
        val btnNavTrack = findViewById<Button>(R.id.navBtnTrack)
        val btnNavProfile = findViewById<Button>(R.id.navBtnProfile)

        // Highlight active Profile tab in light blue
        btnNavHome?.setTextColor(0xFFFFFFFF.toInt())
        btnNavBook?.setTextColor(0xFFFFFFFF.toInt())
        btnNavTrack?.setTextColor(0xFFFFFFFF.toInt())
        btnNavProfile?.setTextColor(0xFF93C5FD.toInt())

        // Set switch state matching saved preference
        switchDark.isChecked = isDarkMode

        switchDark.setOnCheckedChangeListener { _, isChecked ->
            val currentMode = prefs.getBoolean("dark_mode", false)
            if (isChecked != currentMode) {
                prefs.edit().putBoolean("dark_mode", isChecked).apply()
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        // Fetch User Profile Data from Firestore
        val currentUserId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email ?: ""

        if (userEmail.isNotEmpty()) {
            tvEmailHeader.text = userEmail
            tvInitials.text = getInitials("", userEmail)
        }

        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val fullName = doc.getString("fullName") ?: ""
                        val phone = doc.getString("phone") ?: doc.getString("phoneNumber") ?: ""

                        etName.setText(fullName)
                        etPhone.setText(phone)
                        tvNameHeader.text = fullName.ifBlank { userEmail }
                        tvInitials.text = getInitials(fullName, userEmail)
                    }
                }
        }

        // Save / Update User Account Profile
        btnSave.setOnClickListener {
            val updatedName = etName.text.toString().trim()
            val updatedPhone = etPhone.text.toString().trim()

            if (updatedName.isEmpty()) {
                etName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (currentUserId != null) {
                btnSave.isEnabled = false
                btnSave.text = "Saving..."

                db.collection("users").document(currentUserId)
                    .update(mapOf("fullName" to updatedName, "phone" to updatedPhone, "phoneNumber" to updatedPhone))
                    .addOnSuccessListener {
                        btnSave.isEnabled = true
                        btnSave.text = "Update Account Info"
                        tvNameHeader.text = updatedName
                        tvInitials.text = getInitials(updatedName, userEmail)
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        btnSave.isEnabled = true
                        btnSave.text = "Update Account Info"
                        Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Send Firebase Password Reset Email
        btnResetPass.setOnClickListener {
            if (userEmail.isNotEmpty()) {
                auth.sendPasswordResetEmail(userEmail)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password reset link sent to $userEmail", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Reset failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "No email address found for this account.", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout Action
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnNavHome?.setOnClickListener {
            startActivity(Intent(this, DasboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavBook?.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavTrack?.setOnClickListener {
            startActivity(Intent(this, TrackBookingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavProfile?.setOnClickListener {
            // Already on Profile / Settings screen
        }
    }

    private fun getInitials(name: String, email: String): String {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
            parts.isNotEmpty() -> "${parts[0].first()}"
            email.isNotBlank() -> "${email.first()}"
            else -> "U"
        }.uppercase()
    }
}