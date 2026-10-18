package com.example.masandaza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.masandaza.models.Payment
import com.example.masandaza.utils.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth

class PaymentActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val bookingId = intent.getStringExtra("bookingId") ?: ""
        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        val tvRef = findViewById<TextView>(R.id.tvPaymentBookingRef)
        val tvAmount = findViewById<TextView>(R.id.tvPaymentAmount)

        val etCardName = findViewById<EditText>(R.id.etCardHolderName)
        val etCardNum = findViewById<EditText>(R.id.etCardNumber)
        val etExpiry = findViewById<EditText>(R.id.etCardExpiry)
        val etCvv = findViewById<EditText>(R.id.etCardCvv)
        val btnPay = findViewById<Button>(R.id.btnSubmitPayment)

        tvRef.text = if (bookingId.isNotBlank()) "#$bookingId" else "#BK-PENDING"
        tvAmount.text = String.format("R %.2f", totalAmount)
        btnPay.text = String.format("Pay Now — R %.2f", totalAmount)

        btnPay.setOnClickListener {
            val cardName = etCardName.text.toString().trim()
            val cardNum = etCardNum.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val cvv = etCvv.text.toString().trim()

            when {
                cardName.isBlank() -> {
                    etCardName.error = "Enter cardholder name"
                    etCardName.requestFocus()
                }
                cardNum.length < 12 -> {
                    etCardNum.error = "Enter valid card number"
                    etCardNum.requestFocus()
                }
                expiry.isBlank() -> {
                    etExpiry.error = "MM/YY"
                    etExpiry.requestFocus()
                }
                cvv.length < 3 -> {
                    etCvv.error = "CVV"
                    etCvv.requestFocus()
                }
                else -> {
                    btnPay.isEnabled = false
                    btnPay.text = "Processing..."

                    val userId = auth.currentUser?.uid ?: ""
                    val receiptRef = "RCP-2025-" + (1000..9999).random()

                    val newPayment = Payment(
                        paymentId = receiptRef,
                        bookingId = bookingId,
                        customerId = userId,
                        amount = totalAmount,
                        paymentMethod = "Card Payment",
                        status = "Paid"
                    )

                    // Using a general success callback to avoid context/intent collision
                    firebaseRepo.recordPayment(newPayment,
                        onSuccess = {
                            Toast.makeText(this@PaymentActivity, "Payment Successful!", Toast.LENGTH_SHORT).show()

                            val successIntent = Intent(this@PaymentActivity, PaymentSuccessActivity::class.java)
                            successIntent.putExtra("receiptRef", receiptRef)
                            successIntent.putExtra("bookingId", bookingId)
                            successIntent.putExtra("amount", totalAmount)

                            startActivity(successIntent)
                            finish()
                        },
                        onFailure = { e ->
                            btnPay.isEnabled = true
                            btnPay.text = String.format("Pay Now — R %.2f", totalAmount)
                            Toast.makeText(this@PaymentActivity, "Error: ${e?.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                        }
                    )


                }
            }
        }
    }
}
