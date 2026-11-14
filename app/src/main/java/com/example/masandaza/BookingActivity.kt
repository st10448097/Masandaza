package com.example.masandaza

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.masandaza.models.Booking
import com.example.masandaza.utils.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
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

        val btnNavHome = findViewById<Button>(R.id.navBtnHome)
        val btnNavBook = findViewById<Button>(R.id.navBtnBook)
        val btnNavTrack = findViewById<Button>(R.id.navBtnTrack)
        val btnNavProfile = findViewById<Button>(R.id.navBtnProfile)

        etService.setText(serviceName)

        val calendar = Calendar.getInstance()

        // Built-in Calendar DatePicker
        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val formattedDate = String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                    )
                    etDate.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // Prevent choosing past dates
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        // Built-in Clock TimePicker
        etTime.isFocusable = false
        etTime.isClickable = true
        etTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    val amPm = if (hourOfDay < 12) "AM" else "PM"
                    val hourIn12Format = when {
                        hourOfDay == 0 -> 12
                        hourOfDay > 12 -> hourOfDay - 12
                        else -> hourOfDay
                    }
                    val formattedTime = String.format(
                        Locale.US,
                        "%02d:%02d %s",
                        hourIn12Format,
                        minute,
                        amPm
                    )
                    etTime.setText(formattedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

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

            val user = auth.currentUser
            val userId = user?.uid ?: "guest_user"
            val userEmail = user?.email ?: ""
            val bookingRef = "BK-" + (1000..9999).random()

            db.collection("users").document(userId).get().addOnCompleteListener { task ->
                val fullName = if (task.isSuccessful && task.result?.exists() == true) {
                    task.result?.getString("fullName") ?: userEmail
                } else {
                    userEmail
                }

                val newBooking = Booking(
                    bookingId = bookingRef,
                    customerId = userId,
                    customerEmail = userEmail,
                    fullName = fullName,
                    serviceName = serviceName,
                    serviceAddress = "$address (Notes: $notes)",
                    preferredDate = date,
                    preferredTime = time,
                    totalAmount = servicePrice,
                    status = "Pending"
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
        btnNavHome?.setOnClickListener { startActivity(Intent(this, DasboardActivity::class.java)) }
        btnNavBook?.setOnClickListener { startActivity(Intent(this, ServicesActivity::class.java)) }
        btnNavTrack?.setOnClickListener { startActivity(Intent(this, TrackBookingActivity::class.java)) }
        btnNavProfile?.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
}