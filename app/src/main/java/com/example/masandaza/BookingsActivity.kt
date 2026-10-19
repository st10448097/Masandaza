package com.example.masandaza

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)
        BottomNavigationHelper.attach(this, "Bookings")

        container = findViewById(R.id.containerCustomerBookings)
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            loadCustomerBookings(currentUserId)
        } else {
            showEmpty("Please log in to view your bookings.")
        }
    }

    private fun loadCustomerBookings(userId: String) {
        db.collection("bookings")
            .whereEqualTo("customerId", userId)
            .get()
            .addOnSuccessListener { query ->
                container.removeAllViews()

                if (query.isEmpty) {
                    showEmpty("You have no active bookings yet.")
                } else {
                    for (doc in query.documents) {
                        val bookingId = doc.getString("bookingId") ?: doc.id
                        val service = doc.getString("serviceName") ?: "Cleaning Service"
                        val status = doc.getString("status") ?: "Booked"
                        val date = doc.getString("preferredDate") ?: "Scheduled"
                        val time = doc.getString("preferredTime") ?: "09:00 AM"

                        val card = LayoutInflater.from(this).inflate(R.layout.item_customer_booking_card, container, false)

                        val tvService = card.findViewById<TextView>(R.id.tvBookingServiceTitle)
                        val tvRef = card.findViewById<TextView>(R.id.tvBookingReference)
                        val tvStatus = card.findViewById<TextView>(R.id.tvBookingStatusBadge)
                        val tvDate = card.findViewById<TextView>(R.id.tvBookingDateTime)
                        val btnTrack = card.findViewById<Button>(R.id.btnTrackBooking)

                        tvService.text = service
                        tvRef.text = "#$bookingId"
                        tvStatus.text = status
                        tvDate.text = "$date • $time"

                        btnTrack.setOnClickListener {
                            val intent = Intent(this, TrackBookingActivity::class.java)
                            intent.putExtra("bookingId", bookingId)
                            startActivity(intent)
                        }

                        container.addView(card)
                    }
                }
            }
            .addOnFailureListener {
                showEmpty("Failed to load bookings.")
            }
    }

    private fun showEmpty(message: String) {
        container.removeAllViews()
        val tv = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 40, 0, 0)
        }
        container.addView(tv)
    }
}
