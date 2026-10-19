package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_profile_activty)

        val etName = findViewById<EditText>(R.id.etStaffProfileName)
        val etPhone = findViewById<EditText>(R.id.etStaffProfilePhone)
        val btnSave = findViewById<Button>(R.id.btnSaveStaffProfile)
        val btnLogout = findViewById<Button>(R.id.btnStaffLogoutAction)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)

        // Bottom Nav setup
        findViewById<Button>(R.id.navBtnDashboard).setOnClickListener {
            val intent = Intent(this, StaffDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.navBtnJobs).setOnClickListener {
            val intent = Intent(this, StaffDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.navBtnProfile).setTextColor(0xFF2563EB.toInt())

        // Dark Mode Toggle
        val isDarkModeOn = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        switchDarkMode.isChecked = isDarkModeOn

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etName.setText(doc.getString("fullName") ?: "")
                    etPhone.setText(doc.getString("phoneNumber") ?: "")
                }
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (userId != null) {
                db.collection("users").document(userId).update(mapOf("fullName" to name, "phoneNumber" to phone))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}