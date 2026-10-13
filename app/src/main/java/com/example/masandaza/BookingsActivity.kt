package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BookingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        val trackButton = findViewById<Button>(R.id.btnViewTracking)

        trackButton?.setOnClickListener {
            val intent = Intent(this, TrackBookingActivity::class.java)
            startActivity(intent)
        }
        BottomNavigationHelper.attach(this, "Bookings")
    }
}