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

        val btnNavHome = findViewById<Button>(R.id.navBtnHome)
        val btnNavBook = findViewById<Button>(R.id.navBtnBook)
        val btnNavTrack = findViewById<Button>(R.id.navBtnTrack)
        val btnNavProfile = findViewById<Button>(R.id.navBtnProfile)

        // Functional Support Call Action
        btnCallSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+27110000000") // Masandaza support line
            }
            startActivity(intent)
        }

        val targetBookingId = intent.getStringExtra("bookingId")
        val currentUserId = auth.currentUser?.uid
        val currentUserEmail = auth.currentUser?.email

        if (!targetBookingId.isNullOrBlank()) {
            // Priority 1: Track specific booking by ID (passed from Bookings screen)
            db.collection("bookings").document(targetBookingId).addSnapshotListener { doc, e ->
                if (e != null || doc == null || !doc.exists()) {
                    // Fallback to query by bookingId field
                    db.collection("bookings").whereEqualTo("bookingId", targetBookingId)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null && !snapshot.isEmpty) {
                                val d = snapshot.documents.last()
                                val status = d.getString("status") ?: "Pending"
                                val serviceName = d.getString("serviceName") ?: d.getString("service") ?: "Cleaning Session"
                                val refId = d.getString("bookingId") ?: d.id
                                tvRef.text = "REF: #${refId.uppercase()}"
                                tvService.text = "Service: $serviceName"
                                tvStatusBadge.text = status
                                updateTimelineSteps(status, tvStep1, tvStep2, tvStep3, tvStep4, tvStatusBadge)
                            }
                        }
                    return@addSnapshotListener
                }
                val status = doc.getString("status") ?: "Pending"
                val serviceName = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Session"
                val refId = doc.getString("bookingId") ?: doc.id

                tvRef.text = "REF: #${refId.uppercase()}"
                tvService.text = "Service: $serviceName"
                tvStatusBadge.text = status

                updateTimelineSteps(status, tvStep1, tvStep2, tvStep3, tvStep4, tvStatusBadge)
            }
        } else if (currentUserId != null) {
            // Priority 2: Query by logged-in customerId
            db.collection("bookings")
                .whereEqualTo("customerId", currentUserId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener

                    if (snapshot != null && !snapshot.isEmpty) {
                        val doc = snapshot.documents.last()
                        val status = doc.getString("status") ?: "Pending"
                        val serviceName = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Session"
                        val refId = doc.getString("bookingId") ?: doc.id

                        tvRef.text = "REF: #${refId.uppercase()}"
                        tvService.text = "Service: $serviceName"
                        tvStatusBadge.text = status

                        updateTimelineSteps(status, tvStep1, tvStep2, tvStep3, tvStep4, tvStatusBadge)
                    } else if (currentUserEmail != null) {
                        // Priority 3: Fallback query by customerEmail
                        db.collection("bookings")
                            .whereEqualTo("customerEmail", currentUserEmail)
                            .addSnapshotListener { snapshotEmail, _ ->
                                if (snapshotEmail != null && !snapshotEmail.isEmpty) {
                                    val doc = snapshotEmail.documents.last()
                                    val status = doc.getString("status") ?: "Pending"
                                    val serviceName = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Session"
                                    val refId = doc.getString("bookingId") ?: doc.id

                                    tvRef.text = "REF: #${refId.uppercase()}"
                                    tvService.text = "Service: $serviceName"
                                    tvStatusBadge.text = status

                                    updateTimelineSteps(status, tvStep1, tvStep2, tvStep3, tvStep4, tvStatusBadge)
                                } else {
                                    tvRef.text = "REF: NO ACTIVE BOOKING"
                                    tvService.text = "Service: None scheduled"
                                    tvStatusBadge.text = "Inactive"
                                }
                            }
                    } else {
                        tvRef.text = "REF: NO ACTIVE BOOKING"
                        tvService.text = "Service: None scheduled"
                        tvStatusBadge.text = "Inactive"
                    }
                }
        }
        btnNavTrack?.setTextColor(0xFF93C5FD.toInt())

        btnNavHome?.setOnClickListener {
            startActivity(Intent(this, DasboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavBook?.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavTrack?.setOnClickListener { /* Already on Track screen */ }
        btnNavProfile?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
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
            status.equals("Pending", true) || status.equals("Booked", true) -> {
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

