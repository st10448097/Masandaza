package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class StaffJobDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_job_details)

        val bookingId = intent.getStringExtra("bookingId") ?: return

        val tvRef = findViewById<TextView>(R.id.tvDetailRef)
        val tvClientName = findViewById<TextView>(R.id.tvDetailClientName)
        val tvService = findViewById<TextView>(R.id.tvDetailService)
        val tvDateTime = findViewById<TextView>(R.id.tvDetailDateTime)
        val tvAddress = findViewById<TextView>(R.id.tvDetailAddress)
        val btnAccept = findViewById<Button>(R.id.btnAcceptJob)
        val btnUpdateStatus = findViewById<Button>(R.id.btnGoUpdateStatus)

        // Fetch live booking details from Firestore
        db.collection("bookings").document(bookingId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                tvRef.text = "BOOKING REF: #BK-${doc.id.take(8).uppercase()}"
                tvClientName.text = doc.getString("fullName") ?: doc.getString("customerName") ?: "Client"
                tvService.text = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Service"
                tvDateTime.text = "📅 ${doc.getString("preferredDate") ?: doc.getString("date") ?: "Today"} at ${doc.getString("preferredTime") ?: doc.getString("time") ?: "Standard Time"}"
                tvAddress.text = doc.getString("serviceAddress") ?: doc.getString("address") ?: "Address on record"
            }
        }

        // Accept Job Button (Updates status to Staff Assigned)
        btnAccept.setOnClickListener {
            db.collection("bookings").document(bookingId)
                .update("status", "Staff Assigned")
                .addOnSuccessListener {
                    Toast.makeText(this, "Job accepted successfully!", Toast.LENGTH_SHORT).show()
                    btnAccept.text = "Accepted"
                    btnAccept.isEnabled = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Navigate to Update Status Screen
        btnUpdateStatus.setOnClickListener {
            val intent = Intent(this, UpdateJobStatusActivity::class.java)
            intent.putExtra("bookingId", bookingId)
            startActivity(intent)
        }
    }
}
