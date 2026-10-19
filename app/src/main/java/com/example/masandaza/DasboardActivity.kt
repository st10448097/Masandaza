package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        val tvName = findViewById<TextView>(R.id.tvCustomerWelcomeName)
        val tvActiveTitle = findViewById<TextView>(R.id.tvDashboardNextService)
        val tvActiveBadge = findViewById<TextView>(R.id.tvDashboardStatusBadge)
        val tvActiveDetails = findViewById<TextView>(R.id.tvDashboardAppointmentDetails)

        val cardBook = findViewById<View>(R.id.cardBookCleaning)
        val cardMyBookings = findViewById<View>(R.id.cardMyBookings)
        val cardTrack = findViewById<View>(R.id.cardTrackBooking)
        val cardPayments = findViewById<View>(R.id.cardPayments)

        val btnNavBook = findViewById<Button>(R.id.navBtnBook)
        val btnNavTrack = findViewById<Button>(R.id.navBtnTrack)
        val btnNavProfile = findViewById<Button>(R.id.navBtnProfile)

        val uid = auth.currentUser?.uid

        if (uid != null) {
            // Fetch logged in user's real full name
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val fullName = doc.getString("fullName") ?: ""
                        tvName.text = if (fullName.isNotBlank()) "Welcome Back, $fullName" else auth.currentUser?.email ?: "Welcome Back"
                    } else {
                        tvName.text = auth.currentUser?.email ?: "Welcome Back"
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

        cardBook?.setOnClickListener { startActivity(Intent(this, ServicesActivity::class.java)) }
        cardMyBookings?.setOnClickListener { startActivity(Intent(this, BookingsActivity::class.java)) }
        cardTrack?.setOnClickListener { startActivity(Intent(this, TrackBookingActivity::class.java)) }
        cardPayments?.setOnClickListener { startActivity(Intent(this, PaymentActivity::class.java)) }

        btnNavBook?.setOnClickListener { startActivity(Intent(this, ServicesActivity::class.java)) }
        btnNavTrack?.setOnClickListener { startActivity(Intent(this, TrackBookingActivity::class.java)) }
        btnNavProfile?.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
}
