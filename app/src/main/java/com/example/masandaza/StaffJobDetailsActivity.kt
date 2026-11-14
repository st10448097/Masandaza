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

        fun bindDocData(docId: String, status: String, fullName: String, service: String, date: String, time: String, address: String) {
            tvRef.text = "BOOKING REF: #${docId.uppercase()}"
            tvClientName.text = fullName
            tvService.text = service
            tvDateTime.text = "📅 $date at $time"
            tvAddress.text = address

            if (!status.equals("Pending", true) && !status.equals("Booked", true)) {
                btnAccept.text = "Accepted"
                btnAccept.isEnabled = false
            } else {
                btnAccept.text = "Accept Job"
                btnAccept.isEnabled = true
            }
        }

        // Fetch live booking details from Firestore
        db.collection("bookings").document(bookingId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val status = doc.getString("status") ?: "Pending"
                val fullName = doc.getString("fullName") ?: doc.getString("customerName") ?: doc.getString("customerEmail") ?: "Client"
                val service = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Service"
                val date = doc.getString("preferredDate") ?: doc.getString("date") ?: "Today"
                val time = doc.getString("preferredTime") ?: doc.getString("time") ?: "Standard Time"
                val address = doc.getString("serviceAddress") ?: doc.getString("address") ?: "Address on record"
                bindDocData(doc.id, status, fullName, service, date, time, address)
            } else {
                db.collection("bookings").whereEqualTo("bookingId", bookingId).get().addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        val d = query.documents.first()
                        val status = d.getString("status") ?: "Pending"
                        val fullName = d.getString("fullName") ?: d.getString("customerName") ?: d.getString("customerEmail") ?: "Client"
                        val service = d.getString("serviceName") ?: d.getString("service") ?: "Cleaning Service"
                        val date = d.getString("preferredDate") ?: d.getString("date") ?: "Today"
                        val time = d.getString("preferredTime") ?: d.getString("time") ?: "Standard Time"
                        val address = d.getString("serviceAddress") ?: d.getString("address") ?: "Address on record"
                        bindDocData(d.getString("bookingId") ?: d.id, status, fullName, service, date, time, address)
                    }
                }
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
                .addOnFailureListener {
                    // Try fallback by bookingId field
                    db.collection("bookings").whereEqualTo("bookingId", bookingId).get().addOnSuccessListener { q ->
                        if (!q.isEmpty) {
                            q.documents.first().reference.update("status", "Staff Assigned")
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Job accepted successfully!", Toast.LENGTH_SHORT).show()
                                    btnAccept.text = "Accepted"
                                    btnAccept.isEnabled = false
                                }
                        }
                    }
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
