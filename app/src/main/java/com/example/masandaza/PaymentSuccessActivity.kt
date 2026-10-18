package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PaymentSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        val receiptRef = intent.getStringExtra("receiptRef") ?: "RCP-UNKNOWN"
        val bookingId = intent.getStringExtra("bookingId") ?: "BK-UNKNOWN"
        val amount = intent.getDoubleExtra("amount", 0.0)

        val tvDetails = findViewById<TextView>(R.id.tvSuccessDetails)
        val btnHome = findViewById<Button>(R.id.btnBackToHome)

        tvDetails.text = "Receipt: $receiptRef\nBooking.kt: $bookingId\nPaid: R %.2f".format(amount)

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
