
package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DasboardActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val tvName = findViewById<TextView>(R.id.tvCustomerName)
        val tvActiveTitle = findViewById<TextView>(R.id.tvDashActiveTitle)
        val tvActiveBadge = findViewById<TextView>(R.id.tvDashActiveBadge)
        val tvActiveDetails = findViewById<TextView>(R.id.tvDashActiveDetails)

        val btnBook = findViewById<Button>(R.id.btnNavBook)
        val btnMyBookings = findViewById<Button>(R.id.btnNavMyBookings)
        val btnTrack = findViewById<Button>(R.id.btnNavTrack)
        val btnPayments = findViewById<Button>(R.id.btnNavPayments)
        val btnSettings = findViewById<Button>(R.id.btnDashSettingsNav)

        val uid = auth.currentUser?.uid

        if (uid != null) {
            // Fetch logged in user's real full name
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val fullName = doc.getString("fullName") ?: ""
                        tvName.text = fullName.ifBlank { auth.currentUser?.email ?: "Valued Client" }
                    } else {
                        tvName.text = auth.currentUser?.email ?: "Valued Client"
                    }
                }

            // Query THIS user's live active booking from Firestore
            db.collection("bookings")
                .whereEqualTo("customerId", uid)
                .addSnapshotListener { query, _ ->
                    if (query != null && !query.isEmpty) {
                        val doc = query.documents.last()
                        val service = doc.getString("serviceName") ?: "Cleaning Service"
                        val status = doc.getString("status") ?: "Booked"
                        val date = doc.getString("preferredDate") ?: "Scheduled"
                        val time = doc.getString("preferredTime") ?: "09:00 AM"

                        tvActiveTitle.text = service
                        tvActiveBadge.text = status
                        tvActiveDetails.text = "Scheduled for: $date at $time"
                    } else {
                        tvActiveTitle.text = "No Active Appointment"
                        tvActiveBadge.text = "None"
                        tvActiveDetails.text = "Tap 'Book Cleaning' to schedule your session."
                    }
                }
        }

        btnBook.setOnClickListener { startActivity(Intent(this, ServicesActivity::class.java)) }
        btnMyBookings.setOnClickListener { startActivity(Intent(this, BookingsActivity::class.java)) }
        btnTrack.setOnClickListener { startActivity(Intent(this, TrackBookingActivity::class.java)) }
        btnPayments.setOnClickListener { startActivity(Intent(this, PaymentActivity::class.java)) }
        btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
}