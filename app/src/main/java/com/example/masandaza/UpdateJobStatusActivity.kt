package com.example.masandaza

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class UpdateJobStatusActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_job_status)

        val bookingId = intent.getStringExtra("bookingId") ?: return

        val rbPending = findViewById<RadioButton>(R.id.rbStatusPending)
        val rbAssigned = findViewById<RadioButton>(R.id.rbStatusAssigned)
        val rbInProgress = findViewById<RadioButton>(R.id.rbStatusInProgress)
        val rbReview = findViewById<RadioButton>(R.id.rbStatusReview)
        val rbCompleted = findViewById<RadioButton>(R.id.rbStatusCompleted)
        val btnSave = findViewById<Button>(R.id.btnSubmitStatusUpdate)

        fun selectRadio(currentStatus: String) {
            when {
                currentStatus.equals("Pending", true) || currentStatus.equals("Booked", true) -> rbPending.isChecked = true
                currentStatus.equals("Staff Assigned", true) -> rbAssigned.isChecked = true
                currentStatus.equals("Cleaning In Progress", true) -> rbInProgress.isChecked = true
                currentStatus.equals("Ready for Review", true) -> rbReview.isChecked = true
                currentStatus.equals("Completed", true) -> rbCompleted.isChecked = true
            }
        }

        // Fetch current status to pre-select radio button
        db.collection("bookings").document(bookingId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                selectRadio(doc.getString("status") ?: "Pending")
            } else {
                db.collection("bookings").whereEqualTo("bookingId", bookingId).get().addOnSuccessListener { q ->
                    if (!q.isEmpty) {
                        selectRadio(q.documents.first().getString("status") ?: "Pending")
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            val newStatus = when {
                rbPending.isChecked -> "Pending"
                rbAssigned.isChecked -> "Staff Assigned"
                rbInProgress.isChecked -> "Cleaning In Progress"
                rbReview.isChecked -> "Ready for Review"
                rbCompleted.isChecked -> "Completed"
                else -> "Pending"
            }

            db.collection("bookings").document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Status updated to $newStatus!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    db.collection("bookings").whereEqualTo("bookingId", bookingId).get().addOnSuccessListener { q ->
                        if (!q.isEmpty) {
                            q.documents.first().reference.update("status", newStatus)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Status updated to $newStatus!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                        } else {
                            Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
}
