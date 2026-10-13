package com.example.masandaza

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
        BottomNavigationHelper.attach(this,"Bookings")


        val serviceText = findViewById<TextView>(R.id.tvSelectedService)
        val dateInput = findViewById<EditText>(R.id.etBookingDate)
        val timeInput = findViewById<EditText>(R.id.etBookingTime)
        val detailsInput = findViewById<EditText>(R.id.etItemDetails)
        val confirmButton = findViewById<Button>(R.id.btnConfirmBooking)

        serviceText.text = intent.getStringExtra("service")
            ?: "Please select a service"

        dateInput.setOnClickListener {
            val today = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dateInput.setText(
                        String.format(
                            Locale.US, "%04d-%02d-%02d",
                            year, month + 1, day
                        )
                    )
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        timeInput.setOnClickListener {
            val now = Calendar.getInstance()

            TimePickerDialog(
                this,
                { _, hour, minute ->
                    timeInput.setText(
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d", hour, minute
                        )
                    )
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        }

        confirmButton.setOnClickListener {
            when {
                dateInput.text.isBlank() -> {
                    dateInput.error = "Please select a date"
                    dateInput.requestFocus()
                }

                timeInput.text.isBlank() -> {
                    timeInput.error = "Please select a time"
                    timeInput.requestFocus()
                }

                detailsInput.text.isBlank() -> {
                    detailsInput.error = "Describe your item"
                    detailsInput.requestFocus()
                }

                else -> Toast.makeText(
                    this,
                    "Booking details validated. Connect booking storage next.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}