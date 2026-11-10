package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ServicesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val btnNavHome = findViewById<Button>(R.id.navBtnHome)
        val btnNavBook = findViewById<Button>(R.id.navBtnBook)
        val btnNavTrack = findViewById<Button>(R.id.navBtnTrack)
        val btnNavProfile = findViewById<Button>(R.id.navBtnProfile)

        fun openBooking(serviceName: String, basePrice: Double) {
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("service", serviceName)
                putExtra("price", basePrice)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCouch)?.setOnClickListener {
            openBooking("Couch Cleaning", 450.0)
        }

        findViewById<Button>(R.id.btnSneakers)?.setOnClickListener {
            openBooking("Sneaker Cleaning", 150.0)
        }

        findViewById<Button>(R.id.btnCarpet)?.setOnClickListener {
            openBooking("Carpet Cleaning", 350.0)
        }
        // Highlight active Book tab in light blue
        btnNavHome?.setTextColor(0xFFFFFFFF.toInt())
        btnNavBook?.setTextColor(0xFF93C5FD.toInt())
        btnNavTrack?.setTextColor(0xFFFFFFFF.toInt())
        btnNavProfile?.setTextColor(0xFFFFFFFF.toInt())

        btnNavHome?.setOnClickListener {
            startActivity(Intent(this, DasboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        btnNavBook?.setOnClickListener { /* Already on Services screen */ }
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
}
