package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.masandaza.utils.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.masandaza.models.Booking

class BookingActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val serviceName = intent.getStringExtra("service") ?: "Cleaning Service"
        val servicePrice = intent.getDoubleExtra("price", 450.0)

        val etService = findViewById<EditText>(R.id.etBookingService)
        val etAddress = findViewById<EditText>(R.id.etBookingAddress)
        val etDate = findViewById<EditText>(R.id.etBookingDate)
        val etTime = findViewById<EditText>(R.id.etBookingTime)
        val etNotes = findViewById<EditText>(R.id.etBookingNotes)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmBooking)

        etService.setText(serviceName)

        btnConfirm.setOnClickListener {
            val address = etAddress.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (address.isBlank() || date.isBlank() || time.isBlank()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent double-clicking
            btnConfirm.isEnabled = false
            btnConfirm.text = "Processing..."

            val userId = auth.currentUser?.uid ?: "guest_user"
            val bookingRef = "BK-" + (1000..9999).random()

            val newBooking = Booking(
                bookingId = bookingRef,
                customerId = userId,
                serviceName = serviceName,
                serviceAddress = "$address (Notes: $notes)",
                preferredDate = date,
                preferredTime = time,
                totalAmount = servicePrice,
                status = "Booked"
            )

            // Save to Firebase
            firebaseRepo.createBooking(newBooking,
                onSuccess = {
                    Toast.makeText(this, "Booking Successful!", Toast.LENGTH_SHORT).show()

                    // Move to Payment Screen
                    val intent = Intent(this, PaymentActivity::class.java).apply {
                        putExtra("bookingId", bookingRef)
                        putExtra("totalAmount", servicePrice)
                    }
                    startActivity(intent)
                    finish() // Closes BookingActivity so user can't press back to it
                },
                onFailure = { e ->
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Confirm & Pay"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}