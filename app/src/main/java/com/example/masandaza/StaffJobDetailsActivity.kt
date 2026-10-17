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

        val bookingId = intent.getStringExtra("bookingId") ?: ""

        val tvRef = findViewById<TextView>(R.id.tvDetailRef)
        val tvService = findViewById<TextView>(R.id.tvDetailService)
        val tvDateTime = findViewById<TextView>(R.id.tvDetailDateTime)
        val tvAddress = findViewById<TextView>(R.id.tvDetailAddress)

        val btnAccept = findViewById<Button>(R.id.btnAcceptJob)
        val btnUpdate = findViewById<Button>(R.id.btnGoUpdateStatus)

        if (bookingId.isNotBlank()) {
            db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val ref = doc.getString("bookingId") ?: doc.id
                        tvRef.text = "BOOKING REF: #$ref"
                        tvService.text = doc.getString("serviceName") ?: "Cleaning"
                        tvDateTime.text = "Scheduled: " + (doc.getString("preferredDate") ?: "") + " at " + (doc.getString("preferredTime") ?: "")
                        tvAddress.text = doc.getString("serviceAddress") ?: "No address provided"
                    }
                }
        }

        btnAccept.setOnClickListener {
            if (bookingId.isNotBlank()) {
                db.collection("bookings").document(bookingId)
                    .update("status", "Staff Assigned")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Job Accepted & Status Updated!", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnUpdate.setOnClickListener {
            val intent = Intent(this, UpdateJobStatusActivity::class.java).apply {
                putExtra("bookingId", bookingId)
            }
            startActivity(intent)
        }
    }
}
