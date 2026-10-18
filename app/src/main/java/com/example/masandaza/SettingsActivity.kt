package com.example.masandaza

import android.content.Context
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

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val tvInitials = findViewById<TextView>(R.id.tvSettingsInitials)
        val tvNameHeader = findViewById<TextView>(R.id.tvSettingsProfileName)
        val tvEmailHeader = findViewById<TextView>(R.id.tvSettingsProfileEmail)
        val etName = findViewById<EditText>(R.id.etSettingsName)
        val etPhone = findViewById<EditText>(R.id.etSettingsPhone)
        val switchDark = findViewById<SwitchMaterial>(R.id.switchDarkMode)

        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnResetPass = findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = findViewById<Button>(R.id.btnSettingsLogout)

        // 1. Dark Mode Persistence & Toggle
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        switchDark.isChecked = isDarkMode

        switchDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // 2. Fetch User Profile Data from Firestore
        val currentUserId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email ?: ""

        if (currentUserId != null) {
            tvEmailHeader.text = userEmail

            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val fullName = doc.getString("fullName") ?: ""
                        val phone = doc.getString("phone") ?: ""

                        etName.setText(fullName)
                        etPhone.setText(phone)
                        tvNameHeader.text = fullName.ifBlank { userEmail }

                        // Calculate visual initials
                        val parts = fullName.trim().split(" ")
                        val initials = when {
                            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}"
                            parts.size == 1 -> "${parts[0].firstOrNull() ?: ""}"
                            else -> "U"
                        }.uppercase()
                        tvInitials.text = initials
                    }
                }
        }

        // 3. Save / Update User Account Profile
        btnSave.setOnClickListener {
            val updatedName = etName.text.toString().trim()
            val updatedPhone = etPhone.text.toString().trim()

            if (currentUserId != null && updatedName.isNotEmpty()) {
                btnSave.isEnabled = false
                btnSave.text = "Saving..."

                db.collection("users").document(currentUserId)
                    .update(mapOf("fullName" to updatedName, "phone" to updatedPhone))
                    .addOnSuccessListener {
                        btnSave.isEnabled = true
                        btnSave.text = "Update Account Info"
                        tvNameHeader.text = updatedName
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        btnSave.isEnabled = true
                        btnSave.text = "Update Account Info"
                        Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 4. Send Firebase Password Reset Email
        btnResetPass.setOnClickListener {
            if (userEmail.isNotEmpty()) {
                auth.sendPasswordResetEmail(userEmail)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password reset link sent to $userEmail", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Reset failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 5. Logout Action
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
