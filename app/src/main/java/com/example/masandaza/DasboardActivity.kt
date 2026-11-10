package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
        val cardNextAppointment = findViewById<View>(R.id.cardNextAppointment)

        val cardBook = findViewById<View>(R.id.cardBookCleaning)
        val cardMyBookings = findViewById<View>(R.id.cardMyBookings)
        val cardTrack = findViewById<View>(R.id.cardTrackBooking)
        val cardPayments = findViewById<View>(R.id.cardPayments)

        val btnNavHome = findViewById<Button>(R.id.navBtnHome)
        val btnNavBook = findViewById<Button>(R.id.navBtnBook)
        val btnNavTrack = findViewById<Button>(R.id.navBtnTrack)
        val btnNavProfile = findViewById<Button>(R.id.navBtnProfile)

        val uid = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email

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
                    val docs = query?.documents ?: emptyList()
                    val activeDoc = docs.lastOrNull { d ->
                        val s = d.getString("status") ?: ""
                        !s.equals("Completed", true) && !s.equals("Done", true) && !s.equals("Cancelled", true)
                    }

                    if (activeDoc != null) {
                        displayActiveAppointment(activeDoc, tvActiveTitle, tvActiveBadge, tvActiveDetails, cardNextAppointment)
                    } else if (!userEmail.isNullOrBlank()) {
                        // Fallback check by customerEmail if customerId wasn't matched
                        db.collection("bookings")
                            .whereEqualTo("customerEmail", userEmail)
                            .get()
                            .addOnSuccessListener { emailQuery ->
                                val emailDoc = emailQuery.documents.lastOrNull { d ->
                                    val s = d.getString("status") ?: ""
                                    !s.equals("Completed", true) && !s.equals("Done", true) && !s.equals("Cancelled", true)
                                }
                                if (emailDoc != null) {
                                    displayActiveAppointment(emailDoc, tvActiveTitle, tvActiveBadge, tvActiveDetails, cardNextAppointment)
                                } else {
                                    showNoActiveAppointment(tvActiveTitle, tvActiveBadge, tvActiveDetails, cardNextAppointment)
                                }
                            }
                    } else {
                        showNoActiveAppointment(tvActiveTitle, tvActiveBadge, tvActiveDetails, cardNextAppointment)
                    }
                }
        }

        // Highlight active Home tab in light blue
        btnNavHome?.setTextColor(0xFF93C5FD.toInt())
        btnNavBook?.setTextColor(0xFFFFFFFF.toInt())
        btnNavTrack?.setTextColor(0xFFFFFFFF.toInt())
        btnNavProfile?.setTextColor(0xFFFFFFFF.toInt())

        cardBook?.setOnClickListener { startActivity(Intent(this, ServicesActivity::class.java)) }
        cardMyBookings?.setOnClickListener { startActivity(Intent(this, BookingsActivity::class.java)) }
        cardTrack?.setOnClickListener { startActivity(Intent(this, TrackBookingActivity::class.java)) }
        cardPayments?.setOnClickListener { startActivity(Intent(this, PaymentActivity::class.java)) }

        btnNavHome?.setOnClickListener { /* Already on Home screen */ }
        btnNavBook?.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavTrack?.setOnClickListener {
            startActivity(Intent(this, TrackBookingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavProfile?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    private fun displayActiveAppointment(
        doc: DocumentSnapshot,
        tvTitle: TextView,
        tvBadge: TextView,
        tvDetails: TextView,
        cardView: View?
    ) {
        val service = doc.getString("serviceName") ?: doc.getString("service") ?: "Cleaning Service"
        val status = doc.getString("status") ?: "Booked"
        val date = doc.getString("preferredDate") ?: doc.getString("date") ?: "Scheduled"
        val time = doc.getString("preferredTime") ?: doc.getString("time") ?: "09:00 AM"
        val bookingId = doc.getString("bookingId") ?: doc.id

        tvTitle.text = service
        tvBadge.text = status
        tvDetails.text = "Scheduled for: $date at $time"

        when {
            status.equals("Pending", true) || status.equals("Booked", true) -> {
                tvBadge.setTextColor(0xFFD97706.toInt())
                tvBadge.setBackgroundColor(0xFFFEF3C7.toInt())
            }
            status.equals("Staff Assigned", true) -> {
                tvBadge.setTextColor(0xFF2563EB.toInt())
                tvBadge.setBackgroundColor(0xFFDBEAFE.toInt())
            }
            status.equals("Cleaning In Progress", true) -> {
                tvBadge.setTextColor(0xFF7C3AED.toInt())
                tvBadge.setBackgroundColor(0xFFF3E8FF.toInt())
            }
            else -> {
                tvBadge.setTextColor(0xFFDC2626.toInt())
                tvBadge.setBackgroundColor(0xFFFEE2E2.toInt())
            }
        }

        cardView?.setOnClickListener {
            val intent = Intent(this, TrackBookingActivity::class.java).apply {
                putExtra("bookingId", bookingId)
            }
            startActivity(intent)
        }
    }

    private fun showNoActiveAppointment(
        tvTitle: TextView,
        tvBadge: TextView,
        tvDetails: TextView,
        cardView: View?
    ) {
        tvTitle.text = "No Active Appointment"
        tvBadge.text = "None"
        tvBadge.setTextColor(0xFF475569.toInt())
        tvBadge.setBackgroundColor(0xFFF1F5F9.toInt())
        tvDetails.text = "Tap 'Book Cleaning' to schedule your session."

        cardView?.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
    }
}