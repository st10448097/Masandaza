package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ServicesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        fun openBooking(service: String) {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("service", service)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCouch).setOnClickListener {
            openBooking("Couch Cleaning")
        }

        findViewById<Button>(R.id.btnSneakers).setOnClickListener {
            openBooking("Sneaker Cleaning")
        }

        findViewById<Button>(R.id.btnCarpet).setOnClickListener {
            openBooking("Carpet Cleaning")
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}