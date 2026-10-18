package com.example.masandaza

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class UpdateJobStatusActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_job_status)

        val bookingId = intent.getStringExtra("bookingId") ?: ""
        val rgGroup = findViewById<RadioGroup>(R.id.rgStaffStatusGroup)
        val btnSave = findViewById<Button>(R.id.btnSaveStaffStatus)

        btnSave.setOnClickListener {
            val selectedId = rgGroup.checkedRadioButtonId
            val newStatus = when (selectedId) {
                R.id.rbStsReceived -> "Received"
                R.id.rbStsAssigned -> "Staff Assigned"
                R.id.rbStsCleaning -> "Cleaning"
                R.id.rbStsReview -> "Quality Check"
                R.id.rbStsCompleted -> "Completed"
                else -> "Booked"
            }

            if (bookingId.isNotBlank()) {
                btnSave.isEnabled = false
                db.collection("bookings").document(bookingId)
                    .update("status", newStatus)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Status updated to $newStatus!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        btnSave.isEnabled = true
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}