package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DasboardActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        BottomNavigationHelper.attach(this,"Home")
        
        // Open the service screen
        findViewById<Button>(R.id.btnBookNow)?.setOnClickListener { 
            startActivity(
                Intent(this, ServicesActivity::class.java)
            )
        }
        
        // Open booking with selected service
        findViewById<Button>(R.id.btnCouch)?.setOnClickListener { 
            openBooking("Couch Cleaning")
        }
        findViewById<Button>(R.id.btnSneakers)?.setOnClickListener { 
            openBooking("Sneaker Cleaning")
        }
        findViewById<Button>(R.id.btnCarpet)?.setOnClickListener { 
            openBooking("Carpet Cleaning")
        }
        findViewById<Button>(R.id.btnMyBookings)?.setOnClickListener { 
            startActivity(
                Intent(this, BookingsActivity::class.java)
            )
        }
        findViewById<Button>(R.id.btnTrackBooking)?.setOnClickListener { 
            startActivity(
                Intent(this, TrackBookingActivity::class.java)
            )
        }
        findViewById<Button>(R.id.btnPayments)?.setOnClickListener { 
            showComingSoon("Payments and Receipts")
        }
        
        // Settings screen
        findViewById<Button>(R.id.btnSettings)?.setOnClickListener { 
            startActivity(
                Intent(this, SettingsActivity::class.java)
            )
        }
        
        // Return to login and clear the dashboard from the back stack
        findViewById<Button>(R.id.btnLogout)?.setOnClickListener { 
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun openBooking(service: String) {
        val intent = Intent(this, BookingActivity::class.java)
        intent.putExtra("service", service)
        startActivity(intent)
    }

    private fun showComingSoon(section: String) {
        Toast.makeText(
            this,
            "$section screen has not been connected yet.",
            Toast.LENGTH_SHORT
        ).show()
    }
}