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

        val amount = intent.getDoubleExtra("amount", 0.0)
        val receiptRef = intent.getStringExtra("receiptRef") ?: "REC-${System.currentTimeMillis()}"

        val tvDetails = findViewById<TextView>(R.id.tvSuccessDetails)
        val btnHome = findViewById<Button>(R.id.btnBackToHome)

        tvDetails?.text = "Receipt: $receiptRef\nAmount Paid: R$amount\nBooking Confirmed Successfully!"

        btnHome?.setOnClickListener {
            val intent = Intent(this, DasboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}