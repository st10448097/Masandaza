package com.example.masandaza

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrackBookingActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_booking)

        val tvRef = findViewById<TextView>(R.id.tvTrackBookingRef)
        val tvBadge = findViewById<TextView>(R.id.tvTrackStatusBadge)
        val tvService = findViewById<TextView>(R.id.tvTrackServiceDetails)
        val btnCall = findViewById<Button>(R.id.btnCallStaff)

        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            // Listen live to Firestore status changes on customer's booking
            db.collection("bookings")
                .whereEqualTo("customerId", currentUserId)
                .addSnapshotListener { query, _ ->
                    if (query != null && !query.isEmpty) {
                        val doc = query.documents.last()
                        val refId = doc.getString("bookingId") ?: doc.id
                        val service = doc.getString("serviceName") ?: "Cleaning Service"
                        val status = doc.getString("status") ?: "Booked"

                        tvRef.text = "#$refId"
                        tvService.text = "Service: $service"
                        tvBadge.text = status

                        updateTimelineVisuals(status)
                    } else {
                        tvRef.text = "No Active Booking.kt"
                        tvBadge.text = "None"
                    }
                }
        }

        btnCall.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0112345678"))
            startActivity(callIntent)
        }
    }

    private fun updateTimelineVisuals(status: String) {
        val s1 = findViewById<TextView>(R.id.step1)
        val s2 = findViewById<TextView>(R.id.step2)
        val s3 = findViewById<TextView>(R.id.step3)
        val s4 = findViewById<TextView>(R.id.step4)

        val activeColor = Color.parseColor("#1769AA")
        val inactiveColor = Color.parseColor("#94A3B8")

        listOf(s1, s2, s3, s4).forEach {
            it.setTextColor(inactiveColor)
            it.setTypeface(null, Typeface.NORMAL)
        }

        when (status) {
            "Booked", "Received" -> {
                s1.setTextColor(activeColor)
                s1.setTypeface(null, Typeface.BOLD)
            }
            "Staff Assigned" -> {
                s1.setTextColor(activeColor)
                s2.setTextColor(activeColor)
                s2.setTypeface(null, Typeface.BOLD)
            }
            "Cleaning" -> {
                s1.setTextColor(activeColor)
                s2.setTextColor(activeColor)
                s3.setTextColor(activeColor)
                s3.setTypeface(null, Typeface.BOLD)
            }
            "Completed" -> {
                s1.setTextColor(activeColor)
                s2.setTextColor(activeColor)
                s3.setTextColor(activeColor)
                s4.setTextColor(activeColor)
                s4.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}