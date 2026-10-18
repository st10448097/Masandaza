package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ServicesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

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
    }
}
