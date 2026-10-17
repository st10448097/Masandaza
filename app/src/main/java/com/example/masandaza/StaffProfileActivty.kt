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

class StaffProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_profile)

        val tvInitials = findViewById<TextView>(R.id.tvStaffProfileInitials)
        val tvNameHeader = findViewById<TextView>(R.id.tvStaffProfileName)
        val tvIdSub = findViewById<TextView>(R.id.tvStaffProfileIdSub)

        val etName = findViewById<EditText>(R.id.etStaffProfileName)
        val etEmail = findViewById<EditText>(R.id.etStaffProfileEmail)
        val etPhone = findViewById<EditText>(R.id.etStaffProfilePhone)

        val btnSave = findViewById<Button>(R.id.btnSaveStaffProfile)
        val btnLogout = findViewById<Button>(R.id.btnStaffLogoutAction)

        val currentUid = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email ?: ""

        if (currentUid != null) {
            etEmail.setText(userEmail)

            db.collection("users").document(currentUid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val fullName = doc.getString("fullName") ?: "Staff Member"
                        val phone = doc.getString("phone") ?: ""

                        etName.setText(fullName)
                        etPhone.setText(phone)
                        tvNameHeader.text = fullName
                        tvIdSub.text = "Staff ID: STF-" + currentUid.take(5).uppercase()

                        val parts = fullName.trim().split(" ")
                        val initials = when {
                            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}"
                            parts.size == 1 -> "${parts[0].firstOrNull() ?: ""}"
                            else -> "ST"
                        }.uppercase()
                        tvInitials.text = initials
                    }
                }
        }

        btnSave.setOnClickListener {
            val updatedName = etName.text.toString().trim()
            val updatedPhone = etPhone.text.toString().trim()

            if (currentUid != null && updatedName.isNotEmpty()) {
                btnSave.isEnabled = false
                btnSave.text = "Saving..."

                db.collection("users").document(currentUid)
                    .update(mapOf("fullName" to updatedName, "phone" to updatedPhone))
                    .addOnSuccessListener {
                        btnSave.isEnabled = true
                        btnSave.text = "Save Changes"
                        tvNameHeader.text = updatedName
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        btnSave.isEnabled = true
                        btnSave.text = "Save Changes"
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
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