package com.example.masandaza

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrackBookingActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_booking)

        val tvRef = findViewById<TextView>(R.id.tvTrackBookingRef)
        val tvService = findViewById<TextView>(R.id.tvTrackServiceTitle)
        val tvStatusBadge = findViewById<TextView>(R.id.tvTrackCurrentStatusBadge)

        val tvStep1 = findViewById<TextView>(R.id.tvTimelineStep1)
        val tvStep2 = findViewById<TextView>(R.id.tvTimelineStep2)
        val tvStep3 = findViewById<TextView>(R.id.tvTimelineStep3)
        val tvStep4 = findViewById<TextView>(R.id.tvTimelineStep4)
        val btnCallSupport = findViewById<Button>(R.id.btnCallStudioSupport)

        // Functional Support Call Action
        btnCallSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+27110000000") // Masandaza support line
            }
            startActivity(intent)
        }

        val currentUserEmail = auth.currentUser?.email

        // Fetch live booking data from Firestore matching the current user
        if (currentUserEmail != null) {
            db.collection("bookings")
                .whereEqualTo("customerEmail", currentUserEmail)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener

                    if (snapshot != null && !snapshot.isEmpty) {
                        // Grab the most recent booking
                        val doc = snapshot.documents.last()
                        val status = doc.getString("status") ?: "Pending"
                        val serviceName = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Session"
                        val bookingId = doc.id

                        tvRef.text = "REF: #BK-${bookingId.take(8).uppercase()}"
                        tvService.text = "Service: $serviceName"
                        tvStatusBadge.text = status

                        // Update timeline dynamically based on live status
                        updateTimelineSteps(status, tvStep1, tvStep2, tvStep3, tvStep4, tvStatusBadge)
                    } else {
                        tvRef.text = "REF: NO ACTIVE BOOKING"
                        tvService.text = "Service: None scheduled"
                        tvStatusBadge.text = "Inactive"
                    }
                }
        }
    }

    private fun updateTimelineSteps(
        status: String,
        step1: TextView,
        step2: TextView,
        step3: TextView,
        step4: TextView,
        badge: TextView
    ) {
        // Reset all steps to default grey
        val inactiveColor = 0xFF94A3B8.toInt()
        val activeGreen = 0xFF16A34A.toInt()
        val activeBlue = 0xFF2563EB.toInt()

        step1.setTextColor(inactiveColor)
        step2.setTextColor(inactiveColor)
        step3.setTextColor(inactiveColor)
        step4.setTextColor(inactiveColor)

        step1.text = "○  1. Booking Received & Logged"
        step2.text = "○  2. Staff Member Assigned"
        step3.text = "○  3. Cleaning In Progress"
        step4.text = "○  4. Service Completed & Reviewed"

        when {
            status.equals("Pending", true) -> {
                badge.setTextColor(0xFFD97706.toInt())
                badge.setBackgroundColor(0xFFFEF3C7.toInt())
                step1.text = "✓  1. Booking Received & Logged"
                step1.setTextColor(activeGreen)
            }
            status.equals("Staff Assigned", true) -> {
                badge.setTextColor(0xFF2563EB.toInt())
                badge.setBackgroundColor(0xFFDBEAFE.toInt())
                step1.text = "✓  1. Booking Received & Logged"
                step1.setTextColor(activeGreen)
                step2.text = "✓  2. Staff Member Assigned"
                step2.setTextColor(activeGreen)
            }
            status.equals("Cleaning In Progress", true) -> {
                badge.setTextColor(0xFF7C3AED.toInt())
                badge.setBackgroundColor(0xFFF3E8FF.toInt())
                step1.text = "✓  1. Booking Received & Logged"
                step1.setTextColor(activeGreen)
                step2.text = "✓  2. Staff Member Assigned"
                step2.setTextColor(activeGreen)
                step3.text = "⏳  3. Cleaning In Progress"
                step3.setTextColor(activeBlue)
            }
            status.equals("Completed", true) || status.equals("Done", true) -> {
                badge.setTextColor(0xFF16A34A.toInt())
                badge.setBackgroundColor(0xFFDCFCE7.toInt())
                step1.text = "✓  1. Booking Received & Logged"
                step1.setTextColor(activeGreen)
                step2.text = "✓  2. Staff Member Assigned"
                step2.setTextColor(activeGreen)
                step3.text = "✓  3. Cleaning In Progress"
                step3.setTextColor(activeGreen)
                step4.text = "✓  4. Service Completed & Reviewed"
                step4.setTextColor(activeGreen)
            }
        }
    }
}

